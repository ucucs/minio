package com.ucucs.minio.entity;

import com.amazonaws.services.s3.model.PartETag;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChunkPart {

  private int partNumber;

  private String eTag;

  public static ChunkPart from(PartETag partETag) {
    return new ChunkPart(partETag.getPartNumber(), partETag.getETag());
  }

  public PartETag asPart() {
    return new PartETag(partNumber, eTag);
  }
}
