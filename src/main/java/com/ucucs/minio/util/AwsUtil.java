package com.ucucs.minio.util;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class AwsUtil {

  public static ObjectMetadata getObjectMetadata(AmazonS3 s3, String bucketName, String fileKey) {
    return s3.getObjectMetadata(bucketName, fileKey);
  }

  public static S3Object getObject(AmazonS3 s3, String bucketName, String fileKey) {
    return s3.getObject(bucketName, fileKey);
  }

  public static S3Object getObject(
      AmazonS3 s3, String bucketName, String fileKey, long start, long end) {
    GetObjectRequest request = new GetObjectRequest(bucketName, fileKey);
    request.setRange(start, end);
    return s3.getObject(request);
  }

  public static InitiateMultipartUploadResult initChunkUpload(
      AmazonS3 s3, String bucketName, String fileKey) {
    InitiateMultipartUploadRequest request =
        new InitiateMultipartUploadRequest(bucketName, fileKey);
    return s3.initiateMultipartUpload(request);
  }

  public static UploadPartResult multiChunkUpload(
      AmazonS3 s3,
      String bucketName,
      String fileKey,
      String uploadId,
      Integer chunkNumber,
      InputStream inputStream,
      long fileSize) {
    UploadPartRequest request =
        new UploadPartRequest()
            .withBucketName(bucketName)
            .withKey(fileKey)
            .withUploadId(uploadId)
            .withPartNumber(chunkNumber)
            .withInputStream(inputStream)
            .withPartSize(fileSize);
    return s3.uploadPart(request);
  }

  public static CompleteMultipartUploadResult mergeChunkUpload(
      AmazonS3 s3, String bucketName, String fileKey, String uploadId, List<PartETag> partETags) {
    CompleteMultipartUploadRequest request =
        new CompleteMultipartUploadRequest(bucketName, fileKey, uploadId, partETags);
    return s3.completeMultipartUpload(request);
  }

  public static String getFileKey(String fileName, String uid) {
    // String uuid = UUID.randomUUID().toString().replaceAll("-", "");
    String tempFileName =
        Md5Util.md5(uid + fileName + System.currentTimeMillis())
            + "."
            + FileUtil.getExtendsion(fileName);

    String dateDir = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    return dateDir + "/" + tempFileName;
  }
}
