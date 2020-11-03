package com.zz.sccommon.util;

import java.util.UUID;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-04-03 17:29
 * ************************************
 */
public class UuidUtils {
    public static String generateUuid() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
    
    public static String generateUuid(CaseType type) {
        if(type.equals(CaseType.UPPER_CASE)) {
            return generateUuid().toUpperCase();
        }
        return generateUuid().toLowerCase();
    }
    
    public enum CaseType {
        UPPER_CASE,
        LOWER_CASE;
    }
}
