package com.points.utils;

public class PhoneDesensitizeUtil {
    public static String desensitize(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}