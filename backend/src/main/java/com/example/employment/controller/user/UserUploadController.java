package com.example.employment.controller.user;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.example.employment.common.Result;
import com.example.employment.utils.TokenUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/user/upload")
public class UserUploadController {

    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024;
    private static final long MAX_MATERIAL_SIZE = 10 * 1024 * 1024;

    @Value("${file.upload-dir:E:/keshe/uploads}")
    private String uploadDir;

    @Value("${aliyun.oss.enabled:false}")
    private Boolean ossEnabled;

    @Value("${aliyun.oss.endpoint:}")
    private String ossEndpoint;

    @Value("${aliyun.oss.bucket-name:}")
    private String ossBucketName;

    @Value("${aliyun.oss.access-key-id:}")
    private String ossAccessKeyId;

    @Value("${aliyun.oss.access-key-secret:}")
    private String ossAccessKeySecret;

    @PostMapping("/avatar")
    public Result<Map<String, String>> uploadAvatar(HttpServletRequest request,
                                                    @RequestParam("file") MultipartFile file) throws IOException {
        Long userId = TokenUtils.getUserId(request);
        validateImage(file);

        String extension = getExtension(file.getOriginalFilename());
        String fileName = "user-" + userId + "-" + System.currentTimeMillis() + "-"
                + UUID.randomUUID().toString().substring(0, 8) + extension;

        String url = uploadFile(file, "avatar", fileName);

        Map<String, String> data = new HashMap<>();
        data.put("url", url);
        return Result.success("头像上传成功", data);
    }

    @PostMapping("/materials")
    public Result<Map<String, String>> uploadMaterial(HttpServletRequest request,
                                                      @RequestParam("file") MultipartFile file) throws IOException {
        Long userId = TokenUtils.getUserId(request);
        validateMaterial(file);

        String extension = getMaterialExtension(file.getOriginalFilename());
        String fileName = "material-" + userId + "-" + System.currentTimeMillis() + "-"
                + UUID.randomUUID().toString().substring(0, 8) + extension;

        String url = uploadFile(file, "materials", fileName);

        Map<String, String> data = new HashMap<>();
        data.put("url", url);
        data.put("name", file.getOriginalFilename());
        return Result.success("材料上传成功", data);
    }

    private String uploadFile(MultipartFile file, String folder, String fileName) throws IOException {
        String objectName = folder + "/" + fileName;
        if (isOssConfigured()) {
            try {
                return uploadToOss(file, objectName);
            } catch (Exception ignored) {
                // OSS 配置或网络异常时回退到本地存储，保证课程演示流程可用。
            }
        }
        return uploadToLocal(file, folder, fileName);
    }

    private boolean isOssConfigured() {
        return Boolean.TRUE.equals(ossEnabled)
                && StringUtils.hasText(ossEndpoint)
                && StringUtils.hasText(ossBucketName)
                && StringUtils.hasText(ossAccessKeyId)
                && StringUtils.hasText(ossAccessKeySecret);
    }

    private String uploadToOss(MultipartFile file, String objectName) throws IOException {
        OSS ossClient = new OSSClientBuilder().build(ossEndpoint, ossAccessKeyId, ossAccessKeySecret);
        try {
            ossClient.putObject(ossBucketName, objectName, file.getInputStream());
        } finally {
            ossClient.shutdown();
        }
        return "https://" + ossBucketName + "." + ossEndpoint + "/" + objectName;
    }

    private String uploadToLocal(MultipartFile file, String folder, String fileName) throws IOException {
        Path targetDir = Paths.get(uploadDir, folder).toAbsolutePath().normalize();
        Files.createDirectories(targetDir);
        Path target = targetDir.resolve(fileName).normalize();
        file.transferTo(target.toFile());
        return "/upload/" + folder + "/" + fileName;
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("请选择要上传的图片");
        }
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new IllegalArgumentException("图片大小不能超过 5MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new IllegalArgumentException("只能上传图片文件");
        }
    }

    private void validateMaterial(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("请选择要上传的材料");
        }
        if (file.getSize() > MAX_MATERIAL_SIZE) {
            throw new IllegalArgumentException("材料大小不能超过 10MB");
        }
        String extension = getMaterialExtension(file.getOriginalFilename());
        if (extension.isEmpty()) {
            throw new IllegalArgumentException("仅支持 PDF、Word 和图片材料");
        }
    }

    private String getExtension(String originalFilename) {
        if (originalFilename == null) {
            return ".png";
        }
        int index = originalFilename.lastIndexOf('.');
        if (index < 0 || index == originalFilename.length() - 1) {
            return ".png";
        }
        String extension = originalFilename.substring(index).toLowerCase(Locale.ROOT);
        if (!extension.matches("\\.(jpg|jpeg|png|gif|webp)$")) {
            return ".png";
        }
        return extension;
    }

    private String getMaterialExtension(String originalFilename) {
        if (originalFilename == null) {
            return "";
        }
        int index = originalFilename.lastIndexOf('.');
        if (index < 0 || index == originalFilename.length() - 1) {
            return "";
        }
        String extension = originalFilename.substring(index).toLowerCase(Locale.ROOT);
        if (!extension.matches("\\.(pdf|doc|docx|jpg|jpeg|png|webp)$")) {
            return "";
        }
        return extension;
    }
}
