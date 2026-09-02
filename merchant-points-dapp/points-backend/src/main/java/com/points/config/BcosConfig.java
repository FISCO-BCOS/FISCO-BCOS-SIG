package com.points.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "bcos")
public class BcosConfig {
    private String nodeIp;
    private Integer nodePort;
    private Integer groupId = 1;
    private String certPath;
    private String webaseUrl;
    private String ownerAddress;
    private ContractAddress contractAddress = new ContractAddress();

    @Data
    public static class ContractAddress {
        private String points;
    }
}