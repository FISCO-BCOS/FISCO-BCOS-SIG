package com.trace.model.bo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.web.multipart.MultipartFile;

@Data
@EqualsAndHashCode(callSuper = true)
public class TraceTestingDTO extends TraceRecordDTO {

    private String testItems;

    private Integer testResult;

    private String testDate;

    private MultipartFile reportFile;
}