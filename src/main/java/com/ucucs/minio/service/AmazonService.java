package com.ucucs.minio.service;

import com.ucucs.minio.entity.AmazonFileModel;
import com.ucucs.minio.entity.UploadModel;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface AmazonService {

  AmazonFileModel upload(MultipartFile file, String uid);

  AmazonFileModel uploadChunk(UploadModel uploadModel, String uid);

  void download(
      AmazonFileModel fileModel, HttpServletRequest request, HttpServletResponse response);
}
