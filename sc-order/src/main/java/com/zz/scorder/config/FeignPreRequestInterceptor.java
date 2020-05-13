package com.zz.scorder.config;

import com.google.common.collect.Lists;
import com.zz.sccommon.common.FeignDataThreadLocal;
import com.zz.sccommon.common.RequestExtParams;
import com.zz.scorder.entity.ServerIdConfig;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

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
    private static List<ServerIdConfig> serverIdConfig = Lists.newArrayList(
            ServerIdConfig.of("demo1", "", "sc-service", "sc-service1", null, null),
            ServerIdConfig.of("demo2", "", "sc-service", "sc-service2", null, null)
    );
    
    private String getServerId(String issueId, String serverName) {
        List<ServerIdConfig> matchList = serverIdConfig.stream().filter(c -> {
            return c.getIssueId().equals(issueId) && c.getServerName().equals(serverName);
        }).collect(Collectors.toList());
        
        return matchList.size() > 0 ? matchList.get(0).getServerId() : null;
    }
    
    @Override
    public void apply(RequestTemplate template) {
        RequestExtParams data = FeignDataThreadLocal.get();
        String issueId = data != null ? data.getIssueId() : null;
        
        String serverName = template.feignTarget().name();
        String serverId = getServerId(issueId, serverName);
    
        if (StringUtils.isNotBlank(serverId)) {
            String url;
            // 拼接http://
            if (!StringUtils.startsWith(serverId, "http")) {
                url = "http://" + serverId;
            } else {
                url = serverId;
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
