package com.zz.eureka.common;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-03-13 15:11
 * ************************************
 */
@Data
public class RouteInfo {
    private Integer id;
    private String cardExternalCode;
    private String serverUrl;
    
    public String genId() {
        return StringUtils.join(cardExternalCode, id);
    }
}
