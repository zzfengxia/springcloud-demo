package com.zz.scgatewaynew.service;

import com.zz.scgatewaynew.GatewayNewApplication;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.config.PropertiesRouteDefinitionLocator;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.route.CachingRouteLocator;
import org.springframework.cloud.gateway.route.CompositeRouteLocator;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.cloud.gateway.route.RouteDefinitionRouteLocator;
import org.springframework.cloud.gateway.route.RouteLocator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * ************************************
 * create by Intellij IDEA
 * 自定义RouteDefinition获取方式，覆盖默认的 GatewayAutoConfiguration.inMemoryRouteDefinitionRepository注入
 * 该类会和{@link PropertiesRouteDefinitionLocator}类一起作为 List<RouteDefinitionLocator> routeDefinitionLocators参数
 * 注入到{@link org.springframework.cloud.gateway.config.GatewayAutoConfiguration#routeDefinitionLocator}方法中
 *
 * @author Francis.zz
 * @date 2020-03-13 14:52
 * ************************************
 */
@Slf4j
public class RedisRouteDefinitionRepository implements RouteDefinitionRepository {
    //private RedisTemplate<String, String> redisTemplate;
    
    /**
     * 加载解析路由并保存到内存的执行步骤：
     * 1. 调用{@link org.springframework.cloud.gateway.config.GatewayAutoConfiguration#cachedCompositeRouteLocator}初始化{@link CachingRouteLocator}，
     * 参数为{@link RouteLocator}接口的实现类集合。比如{@link GatewayNewApplication#myRoutes}定义的实现
     * 和{@link org.springframework.cloud.gateway.config.GatewayAutoConfiguration#routeDefinitionRouteLocator}注入的实现
     *
     * 2. 调用{@link CachingRouteLocator#fetch}方法，并调用{@link CompositeRouteLocator#getRoutes()}方法。该方法会遍历步骤1中RouteLocator的实现类并分别调用其getRoutes方法，
     * 然后将返回的结果合并，即{@link Route}集合
     *
     * 3. 步骤2调用{@link RouteDefinitionRouteLocator#getRoutes()}方法时，routeDefinitionLocator 的实现类是在
     * {@link org.springframework.cloud.gateway.config.GatewayAutoConfiguration#routeDefinitionLocator}中注入的，
     * 而其中的参数 List<RouteDefinitionLocator> routeDefinitionLocators 就是{@link RouteDefinitionLocator}的实现类集合(需要注入到spring),
     * 比如 {@link org.springframework.cloud.gateway.config.GatewayAutoConfiguration#propertiesRouteDefinitionLocator}(接收属性文件配置的路由信息)
     * 以及 inMemoryRouteDefinitionRepository 的覆盖类，我们自定义的动态路由实现 {@link RedisRouteDefinitionRepository}从redis或者DB中获取配置路由信息。
     * 这里就会分别调用实现类的 {@link RouteDefinitionLocator#getRouteDefinitions}，然后再将结果合并。
     *
     * 4. 将步骤2合并的结果保存在本地缓存中
     *
     * 发布刷新{@link RefreshRoutesEvent}事件，会调用调用{@link CachingRouteLocator#fetch}方法，最终调用到{@link RouteDefinitionLocator#getRouteDefinitions}
     * @return
     */
    @Override
    public Flux<RouteDefinition> getRouteDefinitions() {
        /*log.info("CustomRouteDefinitionRepository get invoking...");
        List<RouteDefinition> routeDefinitions = new ArrayList<>();
        Set<String> routeKeys = redisTemplate.keys("config:card:" + "*");
        if(routeKeys == null || routeKeys.size() == 0) {
            log.error("not found routing config");
            return Flux.fromIterable(routeDefinitions);
        }
        redisTemplate.opsForValue().multiGet(routeKeys).stream().forEach(routeInfoJson -> {
            RouteInfo routeInfo = JSON.parseObject(routeInfoJson, RouteInfo.class);
            // text格式： route1=http://127.0.0.1,Host=baidu.com,Path=/get
            // eg： routeId=http://127.0.0.1,Host=**.addrequestparameter.org,Path=/get
            String defineText = StringUtils.join(routeInfo.genId(), "=", routeInfo.getServerUrl(), ",Path=/", routeInfo.getCardExternalCode());
            RouteDefinition routeDefinition = new RouteDefinition(defineText);
            routeDefinition.setOrder(-100);
            routeDefinitions.add(routeDefinition);
        });*/
        return Flux.empty();
    }
    
    /**
     * 参考 {@link org.springframework.cloud.gateway.route.InMemoryRouteDefinitionRepository#save(Mono)}实现
     *
     * @param route
     * @return
     */
    @Override
    public Mono<Void> save(Mono<RouteDefinition> route) {
        log.info("CustomRouteDefinitionRepository save invoking...");
        return null;
    }
    
    @Override
    public Mono<Void> delete(Mono<String> routeId) {
        log.info("CustomRouteDefinitionRepository delete invoking...");
        return null;
    }
}
