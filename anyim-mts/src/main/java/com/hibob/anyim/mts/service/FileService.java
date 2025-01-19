package com.hibob.anyim.mts.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hibob.anyim.common.enums.ServiceErrorCode;
import com.hibob.anyim.common.model.IMHttpResponse;
import com.hibob.anyim.common.utils.BeanUtil;
import com.hibob.anyim.common.utils.ResultUtil;
import com.hibob.anyim.mts.dto.request.*;
import com.hibob.anyim.mts.dto.vo.ImageVO;
import com.hibob.anyim.mts.entity.MtsObject;
import com.hibob.anyim.mts.enums.FileType;
import com.hibob.anyim.mts.mapper.MtsObjectMapper;
import com.hibob.anyim.mts.minio.MinioConfig;
import com.hibob.anyim.mts.minio.MinioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private final MinioConfig minioConfig;
    private final MinioService minioService;
    private final MtsObjectMapper mtsObjectMapper;

    public ResponseEntity<IMHttpResponse> upload(UploadReq dto) {
        log.info("FileService::upload");
        ResponseEntity<IMHttpResponse> res = ResultUtil.success();
        FileType fileType = FileType.determineFileType(dto.getFile().getOriginalFilename());
        switch (fileType) {
            case IMAGE:
                return uploadImage(dto);
            case DOCUMENT:
//                return uploadFile(dto);
                break;
            default:
                break;
        }

        return ResultUtil.success();
    }

    ResponseEntity<IMHttpResponse> uploadImage(UploadReq dto) {
        MultipartFile file = dto.getFile();
        String fileName = file.getOriginalFilename();
        // 大小校验
        if (file.getSize() > minioConfig.getImageMaxLimit() * 1024 * 1024) {
            return ResultUtil.error(ServiceErrorCode.ERROR_IMAGE_TOO_BIG);
        }

        // 文件后缀校验
        if (!FileType.isImageFile(fileName)) {
            return ResultUtil.error(ServiceErrorCode.ERROR_IMAGE_FORMAT_ERROR);
        }


        ImageVO vo = new ImageVO();
        String objectId = getMd5(file);
        MtsObject mtsObject = mtsObjectMapper.selectById(objectId);
        if (mtsObject != null) {
            vo.setObjectId(objectId);
            vo.setOriginUrl(mtsObject.getOriginUrl());
            vo.setThumbUrl(mtsObject.getThumbUrl());
            return ResultUtil.success(vo);
        }

        String originUrl = minioService.uploadFile(file);
        if (!StringUtils.hasLength(originUrl)) {
            return ResultUtil.error(ServiceErrorCode.ERROR_FILE_UPLOAD_ERROR);
        }

        String thumbUrl = originUrl;
        if (file.getSize() > minioConfig.getImageThumbSize()) {
            try {
                byte[] imageThumb = getImageThumb(file.getBytes());
                int dotIndex = fileName.lastIndexOf('.'); // 文件名在前面已经校验过了，这里肯定合法
                fileName = fileName.substring(0, dotIndex) + "-thumb" + fileName.substring(dotIndex);
                thumbUrl = minioService.uploadFile(imageThumb, file.getContentType(), fileName);
            } catch (IOException e) {
                log.error("file.getBytes() error, exception is {}", e);
            }
        }

        mtsObject = new MtsObject();
        mtsObject.setObjectId(objectId);
        mtsObject.setObjectName(file.getOriginalFilename());
        mtsObject.setObjectType(FileType.determineFileType(file.getOriginalFilename()).name());
        mtsObject.setObjectSize(file.getSize());
        mtsObject.setOriginUrl(originUrl);
        mtsObject.setThumbUrl(thumbUrl);
        mtsObject.setExpire(minioConfig.getTtl() * 86400);
        mtsObjectMapper.insert(mtsObject);

        vo.setObjectId(objectId);
        vo.setOriginUrl(originUrl);
        vo.setThumbUrl(thumbUrl);
        return ResultUtil.success(vo);
    }

    public ResponseEntity<IMHttpResponse> image(ImageReq dto) {
        LambdaQueryWrapper<MtsObject> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(MtsObject::getObjectId, dto.getObjectIds());
        List<MtsObject> mtsObjects = mtsObjectMapper.selectList(queryWrapper);
        List<ImageVO> voList = new ArrayList<>();
        mtsObjects.forEach(item -> {
            voList.add(BeanUtil.copyProperties(item, ImageVO.class));
        });
        return ResultUtil.success(voList);
    }

    /**
     * 生成图片缩略图
     *
     * @param oriImageBytes 原图byte[]
     * @return 缩略图byte[]
     */
    private byte[] getImageThumb(byte[] oriImageBytes) {
        int srcSize = oriImageBytes.length;
        byte[] destImageBytes = oriImageBytes;
        double accuracy = getAccuracy(srcSize);
        try {
            while (destImageBytes.length > minioConfig.getImageThumbSize()) {
                ByteArrayInputStream inputStream = new ByteArrayInputStream(destImageBytes);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream(destImageBytes.length);
                Thumbnails.of(inputStream)
                        .scale(accuracy)
                        .outputQuality(accuracy)
                        .toOutputStream(outputStream);
                destImageBytes = outputStream.toByteArray();
            }
            log.info("图片原大小={}kb | 压缩后大小={}kb", srcSize / 1024, destImageBytes.length / 1024);
        } catch (Exception e) {
            log.error("【图片压缩】msg=图片压缩失败!", e);
        }
        return destImageBytes;
    }

    /**
     * 自动调节精度(经验数值)
     *
     * @param size 源图片大小
     * @return 图片压缩质量比
     */
    private double getAccuracy(int size) {
        double accuracy;
        if (size < 900 * 1024) {
            accuracy = 0.85;
        } else if (size < 2047 * 1024) {
            accuracy = 0.6;
        } else if (size < 3275 * 1024) {
            accuracy = 0.44;
        } else {
            accuracy = 0.4;
        }
        return accuracy;
    }

    /**
     * 获取上传文件的md5
     * @param file
     * @return
     * @throws IOException
     */
    public String getMd5(MultipartFile file) {
        try {
            //获取文件的byte信息
            byte[] uploadBytes = file.getBytes();
            // 拿到一个MD5转换器
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] digest = md5.digest(uploadBytes);
            //转换为16进制
            return new BigInteger(1, digest).toString(16);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }
}
