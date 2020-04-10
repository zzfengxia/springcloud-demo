package com.zz.eureka.util;

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
        return UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
    }
}
