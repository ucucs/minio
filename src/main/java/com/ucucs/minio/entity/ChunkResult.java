package com.ucucs.minio.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChunkResult {

    private String uploadId;
    private String fileKey;
}
