package com.evidence.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("work_file")
public class WorkFileEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long evidenceId;
    private String fileName;
    private String filePath;
    private Long fileSize;
    private String fileType;
    private String fileHash;
    private LocalDateTime uploadTime;
}
