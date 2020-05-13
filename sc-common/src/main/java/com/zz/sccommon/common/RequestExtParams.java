package com.zz.sccommon.common;

import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-05-09 18:00
 * ************************************
 */
@Setter
@Getter
public class RequestExtParams {
    private String issueId;
    private String serverName;
    private Map<String, List<String>> extHeaders;
    
    public RequestExtParams addHeader(String name, String... value) {
        if(extHeaders == null) {
            extHeaders = new HashMap<>();
        }
        this.extHeaders.put(name, Arrays.asList(value));
        return this;
    }
}
