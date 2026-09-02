package com.evidence.model.bo;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class SaveEvidenceRequest {

    @NotBlank(message = "作品标题不能为空")
    @Size(max = 200, message = "标题最长200字")
    private String workTitle;

    @NotBlank(message = "作品分类不能为空")
    private String workCategory;

    private String workDesc;

    private Boolean uploadToChain;
}
