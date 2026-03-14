package com.luo.share.controller;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.luo.share.common.api.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@RestController
@RequestMapping("/api/file")
@Slf4j
public class FileController {

    // 从 application.yml 中自动注入阿里云配置
    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    @Value("${aliyun.oss.access-key-id}")
    private String accessKeyId;

    @Value("${aliyun.oss.access-key-secret}")
    private String accessKeySecret;

    @Value("${aliyun.oss.bucket-name}")
    private String bucketName;

    // 图片保存到项目的静态资源目录下 (实际生产环境建议用阿里云OSS或MinIO)
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "C:\\code\\backend\\lease\\share\\src\\main\\resources\\static\\images";

    @PostMapping("/upload")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.failed("上传文件为空");
        }

        OSS ossClient = null;
        try {
            // 1. 获取原文件名和后缀
            String originalFilename = file.getOriginalFilename();
            String ext = originalFilename.substring(originalFilename.lastIndexOf("."));

            // 2. 生成新的随机文件名 (加上日期文件夹，方便在 OSS 后台管理)
            String datePath = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
            String newFileName = datePath + "/" + UUID.randomUUID().toString().replace("-", "") + ext;

            // 3. 创建 OSSClient 实例
            ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

            // 4. 获取文件输入流并上传到 OSS
            InputStream inputStream = file.getInputStream();
            ossClient.putObject(bucketName, newFileName, inputStream);

            // 5. 拼接返回图片在公网的访问绝对 URL
            // 格式: https://<bucket-name>.<endpoint>/<file-name>
            String url = "https://" + bucketName + "." + endpoint + "/" + newFileName;

            log.info("图片成功上传至阿里云 OSS: {}", url);
            return Result.success(url, "上传成功");

        } catch (Exception e) {
            log.error("OSS 图片上传失败", e);
            return Result.failed("图片上传失败: " + e.getMessage());
        } finally {
            // 6. 务必关闭 OSSClient，释放资源
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }
}