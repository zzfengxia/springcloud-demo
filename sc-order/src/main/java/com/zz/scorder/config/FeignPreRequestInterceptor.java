package com.zz.scorder.config;

import com.zz.sccommon.common.FeignDataThreadLocal;
import com.zz.sccommon.common.RequestExtParams;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * ************************************
 * create by Intellij IDEA
 * OpenFeign请求服务前的拦截器，定制请求参数、多租户定制服务等操作
 *
 * @author Francis.zz
 * @date 2020-05-09 17:30
 * ************************************
 */
@Component
public class FeignPreRequestInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate template) {
        RequestExtParams data = FeignDataThreadLocal.get();
        String serverId = data != null ? data.getServerId() : null;
        String cardCode = data != null ? data.getCardCode() : null;
        
        String serverName = template.feignTarget().name();
        
        if(data != null) {
            data.setServerName(serverName);
        }
        if (StringUtils.isNotBlank(serverId)) {
            String url;
            // 拼接http://
            if (!StringUtils.startsWith(serverId, "http")) {
                url = "http://" + serverId;
            } else {
                url = serverId;
            }
            
            if(StringUtils.isNotBlank(cardCode)) {
                url += "/" + cardCode;
            }
            
            if (template.url().indexOf("http") != 0) {
                template.target(url);
            }
        }
        // 添加请求头
        if(data != null && data.getExtHeaders() != null && !data.getExtHeaders().isEmpty()) {
            data.getExtHeaders().forEach(template::header);
        }
    }
}
