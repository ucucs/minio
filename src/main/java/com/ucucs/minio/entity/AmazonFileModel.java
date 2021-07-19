package com.ucucs.minio.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AmazonFileModel {

  /** 文件大小 */
  private long fileSize;

  /** 文件名称 */
  private String fileName;

  /** 云存储中的路径(存储key) */
  private String fileKey;

  /** 文件类型 */
  private String fileType;

  /** 文件MD5 */
  private String md5;
}
