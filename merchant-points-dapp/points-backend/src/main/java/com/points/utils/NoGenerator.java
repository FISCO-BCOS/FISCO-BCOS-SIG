package com.points.utils;

import java.util.concurrent.ThreadLocalRandom;

public class NoGenerator {
    public static String generateMerchantNo() {
        return "M" + cn.hutool.core.date.DateUtil.format(new java.util.Date(), "yyyyMMdd")
                + String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
    }

    public static String generateTxNo() {
        return "TX" + System.currentTimeMillis()
                + String.format("%06d", ThreadLocalRandom.current().nextInt(1000000));
    }

    public static String generateIssueNo() {
        return "IS" + System.currentTimeMillis()
                + String.format("%06d", ThreadLocalRandom.current().nextInt(1000000));
    }
}