package com.evidence.blockchain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChainResult {

    private boolean success;
    private String message;
    private String txHash;
    private Long blockNumber;
    private String output;
}
