package com.ucucs.minio.util;

import lombok.SneakyThrows;
import org.apache.commons.codec.binary.Hex;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class Md5Util {

  public static String md5(String content) {
    return DigestUtils.md5DigestAsHex(content.getBytes(StandardCharsets.UTF_8));
  }

  @SneakyThrows
  public static String md5Sum(List<String> md5s) {
    if (CollectionUtils.isEmpty(md5s)) {
      return null;
    }

    StringBuilder stringBuilder = new StringBuilder();
    for (String md5 : md5s) {
      stringBuilder.append(md5);
    }
    String hex = stringBuilder.toString();
    byte[] raw = Hex.decodeHex(hex);
    String digest = DigestUtils.md5DigestAsHex(raw);
    return digest + "-" + md5s.size();
  }
}
