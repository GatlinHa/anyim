package com.hibob.anyim.mts.minio;

import io.minio.MinioClient;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Data
public class MinioConfig {

    @Value("${minio.url}")
    private String url;

    @Value("${minio.username}")
    private String username;

    @Value("${minio.password}")
    private String password;

    @Value("${minio.bucketName}")
    private String bucketName;

    @Value("${minio.file.max-limit}")
    private int fileMaxLimit;

    @Value("${minio.image.max-limit}")
    private int imageMaxLimit;

    @Value("${minio.image.thumb-size}")
    private int imageThumbSize;

    @Value("${minio.ttl}")
    private int ttl;

    @Bean
    public MinioClient minioClient(){
        return MinioClient.builder()
                .endpoint(url)
                .credentials(username, password)
                .build();
    }

}
