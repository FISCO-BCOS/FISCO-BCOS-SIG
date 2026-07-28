package com.evidence.blockchain;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BlockchainEvidenceServiceTest {

    @Test
    void verifyEvidenceParsesTransactionReceiptAndReadsUpdatedCount() {
        WeBASEUtils webase = mock(WeBASEUtils.class);
        BlockchainEvidenceService service = new BlockchainEvidenceService();
        ReflectionTestUtils.setField(service, "weBASEUtils", webase);
        ReflectionTestUtils.setField(service, "ownerAddress", "0xowner");

        when(webase.funcPost(eq("0xowner"), eq("verifyEvidence"), anyList()))
                .thenReturn("{\"transactionHash\":\"0xtx\",\"blockNumber\":\"0x10\",\"statusOK\":true}");
        when(webase.funcPost(eq("0xowner"), eq("getEvidence"), anyList()))
                .thenReturn("[\"meta\",\"0xowner\",\"123\",\"2\"]");

        ChainResult result = service.verifyEvidenceExists("hash");

        assertTrue(result.isSuccess());
        assertEquals("0xtx", result.getTxHash());
        assertEquals(16L, result.getBlockNumber());
        assertEquals("2", result.getOutput());
    }

    @Test
    void verifyEvidenceRejectsRevertedReceipt() {
        WeBASEUtils webase = mock(WeBASEUtils.class);
        BlockchainEvidenceService service = new BlockchainEvidenceService();
        ReflectionTestUtils.setField(service, "weBASEUtils", webase);
        ReflectionTestUtils.setField(service, "ownerAddress", "0xowner");

        when(webase.funcPost(eq("0xowner"), eq("verifyEvidence"), anyList()))
                .thenReturn("{\"transactionHash\":\"0xtx\",\"blockNumber\":\"17\",\"statusOK\":false,\"statusMsg\":\"Revert\",\"message\":\"failed\"}");

        ChainResult result = service.verifyEvidenceExists("hash");

        assertFalse(result.isSuccess());
        assertEquals(17L, result.getBlockNumber());
    }
}
