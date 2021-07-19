package com.ucucs.minio.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@NoArgsConstructor
@Document("object_doc")
public class ObjectDoc {

    @Id
    private String id;

    /** 文件大小 */
    private Long fileSize;

    /** 文件名称 */
    private String fileName;

    /** 云存储中的路径(存储key) */
    @Indexed(name = "fileKey")
    private String fileKey;

    /** 文件类型 */
    private String fileType;

    /** 文件MD5 */
    @Indexed(name = "md5")
    private String md5;

    @CreatedDate
    private Instant createTime;

    @LastModifiedDate
    private Instant updateTime;
}
