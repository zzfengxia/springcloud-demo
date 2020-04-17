package com.zz.eureka.util.sign;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-04-09 14:06
 * ************************************
 */
@Slf4j
public class SignatureUtils {
    
    public static String sign(JsonObject jsonObject, String signAlgorithms, String privateKeyStr) {
        String plainText = SortUtil.getSortString(jsonObject);
        log.info("签名前字符串:" + plainText);
        String signStr = RSASignatureUtil.sign(plainText, privateKeyStr, "UTF-8", signAlgorithms);
        return signStr;
    }
    
    public static String sign(Object obj, String algorithms, String privateKey) {
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
        JsonObject jsonObject = gson.toJsonTree(obj).getAsJsonObject();
        
        return sign(jsonObject, algorithms, privateKey);
    }
    
    public static String sign(String jsonStr, String algorithms, String privateKey) {
        JsonObject jsonObject = JsonParser.parseString(jsonStr).getAsJsonObject();
        
        return sign(jsonObject, algorithms, privateKey);
    }
}
