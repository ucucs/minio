package com.ucucs.minio.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
public class UploadModel {

  /** 总块数 */
  private int totalChunks;

  /** 当前块数，从1开始 */
  private int chunkNumber;

  /** 上传id，每个文件唯一 */
  private String identifier;

  /** 上传文件信息,需手动赋值 */
  private MultipartFile multipartFile;
}
