package com.evidence.blockchain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChainEvidenceInfo {

    private String hash;
    private String metadata;
    private String owner;
    private String timestamp;
    private String verifyCount;
}
