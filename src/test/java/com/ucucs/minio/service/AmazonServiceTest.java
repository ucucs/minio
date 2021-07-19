package com.ucucs.minio.service;

import com.ucucs.minio.entity.AmazonFileModel;
import com.ucucs.minio.entity.UploadModel;
import com.ucucs.minio.model.ObjectDoc;
import com.ucucs.minio.util.Md5Util;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.Assert;
import org.springframework.util.DigestUtils;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.util.Collections;
import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class AmazonServiceTest {

  @Autowired private AmazonService amazonService;
  @Autowired private DocumentService documentService;

  @DisplayName("普通文件上传")
  @Test
  void upload() throws Exception {
    File itemFile = ResourceUtils.getFile("classpath:item.json");

    MockMultipartFile multipartFile =
        new MockMultipartFile(
            "files", "item.json", MediaType.APPLICATION_JSON_VALUE, new FileInputStream(itemFile));

    AmazonFileModel fileModel = amazonService.upload(multipartFile, "0");
    System.out.println(fileModel);

    String md5 = DigestUtils.md5DigestAsHex(new FileInputStream(itemFile));
    System.out.println(md5);
    Assert.isTrue(fileModel.getMd5().equals(md5), "两者md5不一致");

    ObjectDoc objectDoc = documentService.save(fileModel);
    System.out.println(objectDoc);
  }

  @DisplayName("分片上传")
  @Test
  void uploadChunk() throws Exception {
    File itemFile = ResourceUtils.getFile("classpath:item.json");

    MockMultipartFile multipartFile =
        new MockMultipartFile(
            "files", "item.json", MediaType.APPLICATION_JSON_VALUE, new FileInputStream(itemFile));

    UploadModel uploadModel = new UploadModel();
    uploadModel.setIdentifier(UUID.randomUUID().toString());
    uploadModel.setChunkNumber(1);
    uploadModel.setMultipartFile(multipartFile);
    uploadModel.setTotalChunks(1);

    AmazonFileModel fileModel = amazonService.uploadChunk(uploadModel, "0");
    System.out.println(fileModel);

    String md5 = DigestUtils.md5DigestAsHex(new FileInputStream(itemFile));
    String md5Sum = Md5Util.md5Sum(Collections.singletonList(md5));
    System.out.println(md5Sum);
    Assert.isTrue(fileModel.getMd5().equals(md5Sum), "两者md5 sum不一致");
  }
}
