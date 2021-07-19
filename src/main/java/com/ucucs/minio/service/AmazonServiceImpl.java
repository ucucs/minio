package com.ucucs.minio.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.internal.Constants;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;
import com.amazonaws.services.s3.transfer.model.UploadResult;
import com.ucucs.minio.entity.AmazonFileModel;
import com.ucucs.minio.entity.ChunkPart;
import com.ucucs.minio.entity.ChunkResult;
import com.ucucs.minio.entity.UploadModel;
import com.ucucs.minio.util.AwsUtil;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AmazonServiceImpl implements AmazonService {

  private static final Logger logger = LoggerFactory.getLogger(AmazonServiceImpl.class);

  @Autowired private AmazonS3 s3;

  @Autowired private RedisTemplate<String, Object> redisTemplate;

  @Value("${amazon.bucket-name:}")
  private String bucketName;

  @Override
  @SneakyThrows
  public AmazonFileModel upload(MultipartFile file, String uid) {
    String originalFileName = file.getOriginalFilename();
    String contentType = file.getContentType();
    long fileSize = file.getSize();
    String fileKey = AwsUtil.getFileKey(originalFileName, uid);

    // bucket不存在会抛异常
    // bucket name 不应该包括/这些特别符号
    makeBucket(bucketName);

    ObjectMetadata objectMetadata = new ObjectMetadata();
    objectMetadata.setContentType(contentType);
    objectMetadata.setContentLength(fileSize);

    // 用户自定义元数据不支持中文
    objectMetadata.addUserMetadata("storage-type", "Amazon");

    // 超过100MB使用分片上传
    // 可在单个PUT中上传的最大数据元为5GB
    String etag;
    if (fileSize > Constants.MB * 16) {
      UploadResult uploadResult = uploadAuto(file, fileKey, objectMetadata);
      etag = uploadResult.getETag();
    } else {
      PutObjectResult uploadResult =
          s3.putObject(bucketName, fileKey, file.getInputStream(), objectMetadata);
      etag = uploadResult.getETag();
    }

    AmazonFileModel amazonFileModel = new AmazonFileModel();
    amazonFileModel.setFileName(originalFileName);
    amazonFileModel.setFileSize(fileSize);
    amazonFileModel.setFileType(contentType);
    amazonFileModel.setFileKey(fileKey);
    amazonFileModel.setMd5(etag);
    return amazonFileModel;
  }

  @SneakyThrows
  @Override
  public AmazonFileModel uploadChunk(UploadModel uploadModel, String uid) {
    String chunkCacheKey = String.format("upload.chunk:%s", uploadModel.getIdentifier());
    String etagCacheKey = String.format("upload.etag:%s", uploadModel.getIdentifier());
    ChunkResult chunkResult = (ChunkResult) redisTemplate.opsForValue().get(chunkCacheKey);

    String fileName = uploadModel.getMultipartFile().getOriginalFilename();
    String fileKey;
    String uploadId;
    if (chunkResult == null) {
      fileKey = AwsUtil.getFileKey(fileName, uid);

      InitiateMultipartUploadResult initResult = AwsUtil.initChunkUpload(s3, bucketName, fileKey);
      uploadId = initResult.getUploadId();

      chunkResult = new ChunkResult();
      chunkResult.setUploadId(uploadId);
      chunkResult.setFileKey(fileKey);
    } else {
      uploadId = chunkResult.getUploadId();
      fileKey = chunkResult.getFileKey();
    }

    // 分段上传
    int chunkNumber = uploadModel.getChunkNumber();
    long fileSize = uploadModel.getMultipartFile().getSize();
    try (InputStream inStream = uploadModel.getMultipartFile().getInputStream()) {
      UploadPartResult partResult =
          AwsUtil.multiChunkUpload(
              s3, bucketName, fileKey, uploadId, chunkNumber, inStream, fileSize);
      redisTemplate.opsForList().rightPush(etagCacheKey, ChunkPart.from(partResult.getPartETag()));
    }

    AmazonFileModel amazonFileModel = new AmazonFileModel();

    if (uploadModel.getTotalChunks() == uploadModel.getChunkNumber()) {
      List<PartETag> partETags =
          redisTemplate.opsForList().range(etagCacheKey, 0, -1).stream()
              .map(o -> ((ChunkPart) o).asPart())
              .collect(Collectors.toList());
      CompleteMultipartUploadResult mergeResult =
          AwsUtil.mergeChunkUpload(s3, bucketName, fileKey, uploadId, partETags);
      amazonFileModel.setMd5(mergeResult.getETag());

      // clean resource
      redisTemplate.delete(Arrays.asList(chunkCacheKey, etagCacheKey));
    }

    String contentType = uploadModel.getMultipartFile().getContentType();
    amazonFileModel.setFileName(fileName);
    amazonFileModel.setFileSize(fileSize);
    amazonFileModel.setFileType(contentType);
    amazonFileModel.setFileKey(fileKey);
    return amazonFileModel;
  }

  @SneakyThrows
  @Override
  public void download(
      AmazonFileModel fileModel, HttpServletRequest request, HttpServletResponse response) {
    ObjectMetadata metadata = AwsUtil.getObjectMetadata(s3, bucketName, fileModel.getFileKey());
    downloadWithResum(fileModel, metadata, request, response);
  }

  @SneakyThrows
  public UploadResult uploadAuto(
      MultipartFile file, String keyName, ObjectMetadata objectMetadata) {
    // 分片最小5MB
    TransferManager tm = TransferManagerBuilder.standard().withS3Client(s3).build();

    // TransferManager processes all transfers asynchronously,
    // so this call returns immediately.
    Upload upload = tm.upload(bucketName, keyName, file.getInputStream(), objectMetadata);
    logger.info("Object upload started");

    // Optionally, wait for the upload to finish before continuing.
    UploadResult uploadResult = upload.waitForUploadResult();

    // upload.waitForCompletion();
    logger.info("Object upload complete");

    return uploadResult;
  }

  public void makeBucket(String bucketName) {
    boolean exists = s3.doesBucketExistV2(bucketName);
    if (!exists) {
      // 不存在就创建一个新的BUCKET
      s3.createBucket(bucketName);
    }
  }

  @SneakyThrows
  private void downloadWithResum(
      AmazonFileModel fileModel,
      ObjectMetadata metadata,
      HttpServletRequest request,
      HttpServletResponse response) {
    InputStream in = null;
    OutputStream out = null;
    try {
      long fileSize = metadata.getContentLength();
      String fileEtag = metadata.getETag();
      Date lastModified = metadata.getLastModified();

      // 0,从头开始的全文下载
      // 1,从某字节开始的下载（bytes=27000-）
      // 2,从某字节开始到某字节结束的下载（bytes=27000-39000）
      long startByte = 0L;
      long endByte = fileSize - 1;

      String range = request.getHeader(HttpHeaders.RANGE);
      if (StringUtils.hasText(range) && range.startsWith("bytes=") && range.contains("-")) {
        response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
        range = range.replaceAll("bytes=", "");
        String[] ranges = range.split("-");
        if (ranges.length == 1) {
          if (range.startsWith("-")) {
            // bytes=-320000
            endByte = Long.parseLong(ranges[0]);
          } else if (range.endsWith("-")) {
            // bytes=270000-
            startByte = Long.parseLong(ranges[0]);
          }
        } else {
          // bytes=270000-320000
          startByte = Long.parseLong(ranges[0]);
          endByte = Long.parseLong(ranges[1]);
        }
      }

      // 范围越界
      if (startByte > endByte || startByte < 0 || endByte >= fileSize) {
        response.setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
        return;
      }

      // ETag header
      response.setHeader(HttpHeaders.ETAG, fileEtag);
      // Last-Modified header
      response.setHeader(HttpHeaders.LAST_MODIFIED, lastModified.toString());

      // 资源发生了变化，下载完整数据
      // 借助Etag判断断点续传前后资源是否发生变化
      String oldEtag = request.getHeader(HttpHeaders.IF_NONE_MATCH);
      if (StringUtils.hasText(oldEtag) && !fileEtag.equals(oldEtag)) {
        startByte = 0L;
        endByte = fileSize - 1;
      }

      // 要下载的长度
      long contentLength = endByte - startByte + 1;

      // 设置响应头
      response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
      response.setHeader(HttpHeaders.ACCEPT_RANGES, "bytes");
      response.setContentLengthLong(contentLength);
      response.setHeader(
          HttpHeaders.CONTENT_RANGE, String.format("bytes %s-%s/%s", startByte, endByte, fileSize));

      // 文件类型
      String fileName = fileModel.getFileName();
      Optional<MediaType> mediaTypeOption = MediaTypeFactory.getMediaType(fileName);
      MediaType mediaType = mediaTypeOption.orElse(MediaType.APPLICATION_OCTET_STREAM);
      response.setContentType(mediaType.toString());
      response.addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + fileName);

      // 返回目标内容
      String fileKey = fileModel.getFileKey();
      S3Object s3Object = AwsUtil.getObject(s3, bucketName, fileKey, startByte, endByte);
      in = s3Object.getObjectContent();
      out = response.getOutputStream();
      IOUtils.copy(in, out);
    } catch (IOException e) {
      // 浏览器加载音视频时，为获取总数据大小，
      // 第一次会发"bytes=0-"的请求且收到响应头后立马关闭连接，
      // 导致服务端写数据出现Broken pipe，故忽略之，其他抛到上层
      // ignore
    } finally {
      IOUtils.closeQuietly(in);
      IOUtils.closeQuietly(out);
    }
  }
}
