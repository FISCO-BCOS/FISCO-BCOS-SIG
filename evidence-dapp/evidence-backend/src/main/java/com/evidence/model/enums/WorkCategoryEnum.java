package com.evidence.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum WorkCategoryEnum {

    IMAGE("image", "图片"),
    DOCUMENT("document", "文档"),
    AUDIO("audio", "音频"),
    VIDEO("video", "视频"),
    CODE("code", "代码");

    private final String code;
    private final String desc;
}
