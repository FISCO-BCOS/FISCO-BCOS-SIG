package com.evidence.blockchain;

import org.fisco.bcos.sdk.BcosSDK;
import org.fisco.bcos.sdk.client.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Configuration
public class FiscoBcosConfig {

    private static final Logger log = LoggerFactory.getLogger(FiscoBcosConfig.class);

    @Value("${fisco.bcos.group-id}")
    private Integer groupId;

    private BcosSDK bcosSDK;
    private Client client;

    @Bean
    public Client fiscoClient() throws Exception {
        String configFilePath = prepareConfig();
        log.info("FISCO-BCOS SDK building from: {}", configFilePath);

        bcosSDK = BcosSDK.build(configFilePath);
        client = bcosSDK.getClient(groupId);
        log.info("FISCO-BCOS SDK connected, groupId={}, blockNumber={}",
                groupId, client.getBlockNumber().getBlockNumber());
        return client;
    }

    private String prepareConfig() throws Exception {
        String tmpDir = System.getProperty("java.io.tmpdir") + "fisco-sdk-" + System.currentTimeMillis();
        Path workDir = Paths.get(tmpDir);
        Files.createDirectories(workDir);

        Path certDir = workDir.resolve("conf");
        Files.createDirectories(certDir);

        ClassLoader cl = getClass().getClassLoader();
        for (String certFile : new String[]{"ca.crt", "sdk.crt", "sdk.key"}) {
            try (InputStream in = cl.getResourceAsStream("conf/" + certFile)) {
                if (in != null) {
                    Files.copy(in, certDir.resolve(certFile), StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }

        try (InputStream in = cl.getResourceAsStream("config.toml")) {
            String content = new String(in.readAllBytes());
            String absCertPath = certDir.toAbsolutePath().toString().replace("\\", "/");
            content = content.replace("certPath = \"conf\"",
                    "certPath = \"" + absCertPath + "\"");
            Path configFile = workDir.resolve("config.toml");
            Files.writeString(configFile, content);
            log.info("SDK config prepared at: {}", configFile.toAbsolutePath());
            return configFile.toAbsolutePath().toString();
        }
    }

    @PreDestroy
    public void destroy() {
        if (bcosSDK != null) {
            bcosSDK.stopAll();
            log.info("FISCO-BCOS SDK stopped");
        }
    }
}