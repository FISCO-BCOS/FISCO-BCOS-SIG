package com.trace.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.trace.model.Result;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

@RestController
@RequestMapping("/api/file")
public class FileController {

    private static final Logger log = LoggerFactory.getLogger(FileController.class);

    /** 允许上传的图片类型白名单 */
    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = new HashSet<>(Arrays.asList(
            ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp"
    ));

    /** 允许上传的文档类型白名单 */
    private static final Set<String> ALLOWED_DOC_EXTENSIONS = new HashSet<>(Arrays.asList(
            ".pdf", ".doc", ".docx", ".xls", ".xlsx"
    ));

    /** 单文件最大 10MB */
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    @Value("${file.upload-path:./static/uploads/}")
    private String uploadPath;

    @PostMapping("/upload")
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        // 1. 空文件检查
        if (file.isEmpty()) {
            log.warn("文件上传失败: 空文件");
            return Result.error(com.trace.model.vo.ResultVO.PARAM_EMPTY);
        }

        // 2. 文件大小校验（代码层二次校验，不依赖spring配置）
        if (file.getSize() > MAX_FILE_SIZE) {
            log.warn("文件上传失败: 文件过大 {} bytes, 上限 {} bytes", file.getSize(), MAX_FILE_SIZE);
            return Result.error("文件大小不能超过10MB");
        }

        try {
            String originalName = file.getOriginalFilename();
            if (originalName == null || originalName.trim().isEmpty()) {
                log.warn("文件上传失败: 原始文件名为空");
                return Result.error("文件名不能为空");
            }

            // 3. 目录穿越防护：过滤 .. 和路径分隔符
            if (originalName.contains("..") || originalName.contains("/") || originalName.contains("\\")) {
                log.warn("文件上传失败: 检测到非法文件名(目录穿越) - {}", originalName);
                return Result.error("非法文件名");
            }

            String ext = originalName.contains(".") ?
                    originalName.substring(originalName.lastIndexOf(".")).toLowerCase() : "";

            // 4. 扩展名白名单校验
            boolean isImage = ALLOWED_IMAGE_EXTENSIONS.contains(ext);
            boolean isDoc = ALLOWED_DOC_EXTENSIONS.contains(ext);
            if (!isImage && !isDoc) {
                log.warn("文件上传失败: 不允许的文件类型 - {}", ext);
                return Result.error("仅支持 jpg/png/gif/bmp/webp/pdf/doc/docx/xls/xlsx 格式");
            }

            // 5. Content-Type 与扩展名双重校验（防止伪装Content-Type）
            String contentType = file.getContentType();
            if (!isValidContentType(contentType, ext)) {
                log.warn("文件上传失败: Content-Type与扩展名不匹配 - contentType={}, ext={}", contentType, ext);
                return Result.error("文件内容与扩展名不匹配");
            }

            // 6. UUID随机文件名 + 子目录分类存储
            String newName = IdUtil.fastSimpleUUID() + ext;
            String subDir = isImage ? "images" : "reports";
            String dirPath = uploadPath + subDir + File.separator;

            File dir = new File(dirPath);
            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                if (!created) {
                    log.error("文件上传失败: 无法创建上传目录 - {}", dirPath);
                    return Result.error("服务器内部错误，请稍后重试");
                }
            }

            File dest = new File(dirPath + newName);
            file.transferTo(dest);

            log.info("文件上传成功: originalName={}, size={}bytes, url={}",
                    originalName, file.getSize(), "/uploads/" + subDir + "/" + newName);

            Map<String, String> result = new HashMap<>();
            result.put("url", "/uploads/" + subDir + "/" + newName);
            result.put("originalName", originalName);
            return Result.success(result);

        } catch (Exception e) {
            log.error("文件上传异常", e);
            return Result.error("文件上传失败，请稍后重试");
        }
    }

    /**
     * 校验 Content-Type 是否与文件扩展名匹配
     */
    private boolean isValidContentType(String contentType, String ext) {
        if (contentType == null) return false;
        switch (ext) {
            case ".jpg":
            case ".jpeg":
                return contentType.equals("image/jpeg");
            case ".png":
                return contentType.equals("image/png");
            case ".gif":
                return contentType.equals("image/gif");
            case ".bmp":
                return contentType.equals("image/bmp");
            case ".webp":
                return contentType.equals("image/webp");
            case ".pdf":
                return contentType.equals("application/pdf");
            case ".doc":
                return contentType.equals("application/msword");
            case ".docx":
                return contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            case ".xls":
                return contentType.equals("application/vnd.ms-excel");
            case ".xlsx":
                return contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            default:
                return false;
        }
    }
}
