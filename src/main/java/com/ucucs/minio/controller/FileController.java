package com.ucucs.minio.controller;

import com.ucucs.minio.entity.AmazonFileModel;
import com.ucucs.minio.entity.UploadModel;
import com.ucucs.minio.model.ObjectDoc;
import com.ucucs.minio.service.AmazonService;
import com.ucucs.minio.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RequestMapping("/oss")
@RestController
public class FileController {

  @Autowired private AmazonService amazonService;
  @Autowired private DocumentService documentService;

  @PostMapping("/upload")
  public ObjectDoc upload(@RequestParam(name = "file", required = false) MultipartFile file)
      throws Exception {
    if (file == null || file.getSize() == 0) {
      throw new Exception("上传文件不能为空");
    }

    AmazonFileModel fileModel = amazonService.upload(file, "");
    ObjectDoc objectDoc = documentService.save(fileModel);
    return objectDoc;
  }

  @PostMapping("/uploadChunk")
  public AmazonFileModel uploadChunk(
      @RequestParam(name = "file", required = false) MultipartFile file,
      @RequestParam UploadModel uploadModel)
      throws Exception {
    if (file == null || file.getSize() == 0) {
      throw new Exception("上传文件不能为空");
    }

    uploadModel.setMultipartFile(file);
    return amazonService.uploadChunk(uploadModel, "");
  }

  @GetMapping("/download")
  public void download(HttpServletRequest request, HttpServletResponse response) {
    AmazonFileModel fileModel = new AmazonFileModel();
    fileModel.setFileKey("2021-06-29/664fa9a97e92dd968c45052fc7efff05.json");
    fileModel.setFileName("item.json");
    fileModel.setFileSize(56L);
    fileModel.setFileType("application/json");
    fileModel.setMd5("3f8a317ecc6be26b222b910f4e080432");
    amazonService.download(fileModel, request, response);
  }
}
