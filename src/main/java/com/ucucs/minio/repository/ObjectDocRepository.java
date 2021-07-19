package com.ucucs.minio.repository;

import com.ucucs.minio.model.ObjectDoc;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ObjectDocRepository extends MongoRepository<ObjectDoc,String> {
}
