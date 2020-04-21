package com.zz.scgatewaynew.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Service;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-03-13 17:26
 * ************************************
 */
@Service
@Slf4j
public class DynamicGatewayService implements ApplicationEventPublisherAware {
    @Autowired
    private RouteDefinitionWriter routeDefinitionWriter;
    private ApplicationEventPublisher publisher;
    
    @Value("nacos.config.serverAddr")
    private String serverAddr;
    @Value("nacos.config.groupId")
    private String groupId;
    @Value("nacos.config.dataId")
    private String dataId;
    
    /**
     * 动态添加路由
     *
     * @return
     */
    public void save() {
        log.info("save invoke...");
        // InMemoryRouteDefinitionRepository的save方法必须要调用subscribe才能生效。参考 ReactorTest的 testFaltMap 测试用例来验证。
        //routeDefinitionWriter.save(Mono.just(definition)).subscribe();
        // 调用刷新事件，更新路由缓存。由于没有使用 RouteDefinitionWriter 创建路由，所以这里无需清空操作
        this.publisher.publishEvent(new RefreshRoutesEvent(this));
    }
    
    public void update() {
        // 调用刷新事件，更新路由缓存。由于没有使用 RouteDefinitionWriter 创建路由，所以这里无需清空操作
        // this.publisher.publishEvent(new RefreshRoutesEvent(this));
    }
    
    public void delete() {
        /*log.info("delete invoke...");
        return this.routeDefinitionWriter.delete(Mono.just(id))
                .then(Mono.defer(() -> Mono.just(ResponseEntity.ok().build())))
                .onErrorResume(t -> t instanceof NotFoundException, t -> Mono.just(ResponseEntity.notFound().build()));*/
    }
    
    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.publisher = applicationEventPublisher;
    }
}
