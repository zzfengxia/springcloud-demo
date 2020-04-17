package com.zz.sccommon.util;

import com.alibaba.fastjson.JSONValidator;

import java.nio.charset.StandardCharsets;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-04-14 10:28
 * ************************************
 */
public class JsonUtils {
    /**
     * 校验数据是否为json格式
     *
     * @param data
     * @return
     */
    public static boolean isJson(Object data) {
        if(!(data instanceof String)) {
            return false;
        }
        try {
            return JSONValidator.fromUtf8(((String) data).getBytes(StandardCharsets.UTF_8)).validate();
        } catch (Exception e) {
            return false;
        }
    }
}
