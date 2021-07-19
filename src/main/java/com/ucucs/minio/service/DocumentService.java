package com.ucucs.minio.service;

import com.ucucs.minio.entity.AmazonFileModel;
import com.ucucs.minio.model.ObjectDoc;

public interface DocumentService {

  ObjectDoc save(AmazonFileModel fileModel);
}
