package com.trace.service.impl;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BcosServiceImplTest {

    @Test
    void parsesScalarContractResult() {
        Map<String, Object> result = BcosServiceImpl.parseContractResult("0");
        assertEquals("0", result.get("value"));
    }

    @Test
    void parsesObjectContractResult() {
        Map<String, Object> result = BcosServiceImpl.parseContractResult("{\"connected\":true}");
        assertEquals(true, result.get("connected"));
    }
}
