package com.ucucs.minio.service;

import com.ucucs.minio.entity.AmazonFileModel;
import com.ucucs.minio.model.ObjectDoc;
import com.ucucs.minio.repository.ObjectDocRepository;
import com.ucucs.minio.util.BeanUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DocumentServiceImpl implements DocumentService {

  @Autowired private ObjectDocRepository objectDocRepository;

  @Override
  public ObjectDoc save(AmazonFileModel fileModel) {
    ObjectDoc objectDoc = BeanUtil.copyProperties(fileModel, ObjectDoc.class);
    objectDoc.setId(UUID.randomUUID().toString());
    objectDocRepository.save(objectDoc);
    return objectDoc;
  }
}
