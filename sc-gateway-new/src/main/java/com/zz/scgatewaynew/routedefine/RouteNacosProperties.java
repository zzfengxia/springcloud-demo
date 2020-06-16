package com.zz.scgatewaynew.routedefine;

import com.alibaba.cloud.sentinel.datasource.config.NacosDataSourceProperties;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * ************************************
 * create by Intellij IDEA
 * {@link ConfigurationProperties} 需要配合{@link Component}
 * 或者{@link org.springframework.boot.context.properties.EnableConfigurationProperties} 使用
 *
 * @author Francis.zz
 * @date 2020-06-08 11:11
 * ************************************
 */
@ConfigurationProperties("spring.cloud.gateway.route.nacos")
@Getter
@Setter
public class RouteNacosProperties extends NacosDataSourceProperties {
    public boolean checkProp() {
        return StringUtils.isNotBlank(getServerAddr()) && StringUtils.isNotBlank(getDataId());
    }
}
