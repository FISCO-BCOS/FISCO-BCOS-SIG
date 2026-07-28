package com.trace.service;

import java.util.Map;

public interface IBcosService {

    Map<String, Object> createProduct(String userAddress, String productNo);

    Map<String, Object> recordPlanting(String userAddress, String plantingNo, String productNo, String dataHash, String username);

    Map<String, Object> recordProcessing(String userAddress, String processingNo, String productNo, String dataHash, String username);

    Map<String, Object> recordTesting(String userAddress, String testingNo, String productNo, String dataHash, String username);

    Map<String, Object> recordLogistics(String userAddress, String logisticsNo, String productNo, String dataHash, String username);

    Map<String, Object> recordScan(String userAddress, String productNo);

    Map<String, Object> getTraceRecord(String productNo);

    Map<String, Object> getTraceCount(String productNo);
}