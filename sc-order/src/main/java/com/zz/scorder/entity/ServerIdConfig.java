package com.zz.scorder.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-05-09 17:09
 * ************************************
 */
@Setter
@Getter
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class ServerIdConfig {
    /**
     * 客户端ID/租户ID
     */
    private String issueId;
    /**
     * 路由服务名，网关路由使用
     */
    private String routeServerName;
    /**
     * 调用服务名，处理不同业务的服务
     */
    private String serverName;
    /**
     * 调用服务的实际服务ID，不同的租户可能会有定制化的服务
     */
    private String serverId;
    /**
     * 城市代码
     */
    private String cardCode;
    
    private Date createTime;
    private Date updateTime;
}
