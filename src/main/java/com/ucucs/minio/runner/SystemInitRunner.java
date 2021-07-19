package com.ucucs.minio.runner;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(value = 1)
@Slf4j
public class SystemInitRunner implements ApplicationRunner {

  @Override
  public void run(ApplicationArguments args) {
    // 租户端不进行服务调用
    log.info("==服务启动后，初始化数据源==");

    log.info("初始化多租户数据库配置完成...");
  }
}
