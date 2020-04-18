package com.zz.sccommon.util;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-04-17 16:29
 * ************************************
 */
public class NumberStringUtil {
    public static String addRightZero(String str, int length) {
        int str_length = str.length();
        
        for(int i = 0; i < length - str_length; ++i) {
            str = str + '0';
        }
        
        return str;
    }
}
