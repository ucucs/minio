package com.ucucs.minio.config;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@NoArgsConstructor
@Configuration
@ConfigurationProperties(prefix = "amazon")
public class AmazonConfig {

  private String endpoint;

  private String accessKey;

  private String secretKey;

  private String bucketName;

  @Bean
  public AmazonS3 amazonS3() {
    ClientConfiguration config = new ClientConfiguration();

    AwsClientBuilder.EndpointConfiguration endpointConfig =
        new AwsClientBuilder.EndpointConfiguration(endpoint, null);

    AWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
    AWSCredentialsProvider awsCredentialsProvider =
        new AWSStaticCredentialsProvider(awsCredentials);

    return AmazonS3Client.builder()
        .withEndpointConfiguration(endpointConfig)
        .withClientConfiguration(config)
        .withCredentials(awsCredentialsProvider)
        .withPathStyleAccessEnabled(true)
        .build();
  }
}
