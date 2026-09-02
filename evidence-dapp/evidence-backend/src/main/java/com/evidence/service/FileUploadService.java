package com.evidence.service;

import cn.hutool.core.util.HexUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.evidence.model.entity.WorkFileEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class FileUploadService {

    @Value("${file.upload-path}")
    private String uploadPath;

    public WorkFileEntity uploadFile(MultipartFile file, Long userId, Long evidenceId) throws Exception {
        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String dirPath = uploadPath + userId + "/" + dateDir;
        File dir = new File(dirPath);
        if (!dir.exists()) dir.mkdirs();

        String originalFilename = file.getOriginalFilename();
        String ext = originalFilename.contains(".") ? originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
        String newFilename = UUID.randomUUID().toString().replace("-", "") + ext;

        File dest = new File(dir, newFilename);
        file.transferTo(dest.getAbsoluteFile());

        String fileHash = HexUtil.encodeHexStr(DigestUtil.sha256(dest));

        WorkFileEntity entity = new WorkFileEntity();
        entity.setEvidenceId(evidenceId);
        entity.setFileName(originalFilename);
        entity.setFilePath(userId + "/" + dateDir + "/" + newFilename);
        entity.setFileSize(file.getSize());
        entity.setFileType(file.getContentType());
        entity.setFileHash(fileHash);
        entity.setUploadTime(LocalDateTime.now());
        return entity;
    }
}
