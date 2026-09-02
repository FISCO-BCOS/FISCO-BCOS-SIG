package com.trace.model.bo;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductCreateDTO {
    @NotBlank
    private String productName;
    @NotBlank
    private String category;
    private String description;
    @NotNull
    private BigDecimal price;
    @NotBlank
    private String unit;
    @NotBlank
    private String originAddress;
    private Long farmerId;
    private Long processorId;
    private List<String> images;
}