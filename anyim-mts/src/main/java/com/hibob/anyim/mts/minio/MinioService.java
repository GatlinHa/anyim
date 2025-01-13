package com.hibob.anyim.mts.minio;

import com.hibob.anyim.mts.enums.FileType;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class MinioService {
    private final MinioConfig minioConfig;
    private final MinioClient minioClient;

    public String uploadFile(MultipartFile file) {

        try {
            boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(minioConfig.getBucketName()).build());
            if (!bucketExists){
                // 如果不存在，就创建桶
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(minioConfig.getBucketName()).build());
            }
            String fileName = file.getOriginalFilename();
            String prefixPath = FileType.determineFileType(fileName).name();
            String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String uuidPath = UUID.randomUUID().toString();
            String fullName = prefixPath + "/" + datePath + "/" + uuidPath + "/" + fileName;

            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(fullName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
            return  minioConfig.getUrl() + "/" + minioConfig.getBucketName() + "/" + fullName;
        }
        catch (Exception e) {
            log.error("upload file error: {}", e.getMessage());
            return "";
        }
    }

    public String uploadFile(byte[] fileByte, String contentType, String fileName) {

        try {
            boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(minioConfig.getBucketName()).build());
            if (!bucketExists){
                // 如果不存在，就创建桶
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(minioConfig.getBucketName()).build());
            }

            String prefixPath = FileType.determineFileType(fileName).name();
            String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String uuidPath = UUID.randomUUID().toString();
            String fullName = prefixPath + "/" + datePath + "/" + uuidPath + "/" + fileName;

            InputStream stream = new ByteArrayInputStream(fileByte);
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(fullName)
                    .stream(stream, fileByte.length, -1)
                    .contentType(contentType)
                    .build());
            return  minioConfig.getUrl() + "/" + minioConfig.getBucketName() + "/" + fullName;
        }
        catch (Exception e) {
            log.error("upload file error: {}", e.getMessage());
            return "";
        }
    }
}
