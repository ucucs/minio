package com.ucucs.minio.util;

public class FileUtil {

  public static String getExtendsion(String fileName) {
    return fileName.substring(fileName.lastIndexOf(".") + 1);
  }
}
