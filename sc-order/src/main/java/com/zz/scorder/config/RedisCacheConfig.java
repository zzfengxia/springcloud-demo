package com.zz.scorder.config;

import com.alibaba.fastjson.support.spring.GenericFastJsonRedisSerializer;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * ************************************
 * create by Intellij IDEA
 * springboot使用redis作为缓存配置，继承CachingConfigurerSupport，自定义springboot注解缓存的key生产策略等配置
 * @see {@link org.springframework.cache.annotation.Cacheable}
 * @see {@link org.springframework.cache.annotation.EnableCaching}
 * @author Francis.zz
 * @date 2020-07-16 09:38
 * ************************************
 */
@Configuration
public class RedisCacheConfig extends CachingConfigurerSupport {
    /**
     * 重写{@link CachingConfigurerSupport}类，自定义{@link org.springframework.cache.annotation.Cacheable}的缓存KEY生产策略
     *
     * @return
     */
    @Override
    @Bean("myKeyGenerator")
    public KeyGenerator keyGenerator() {
        return (target, method, params) -> {
            StringBuilder sb = new StringBuilder();
            for(Object arg : params) {
                if(arg instanceof String || arg instanceof Long || arg instanceof Integer) {
                    sb.append(arg);
                }
            }
            return String.format("cache:%s:%s", target.getClass().getSimpleName(), sb.toString());
        };
    }
    
    /**
     * 自定义Cacheable注解的缓存管理
     * 覆盖{@link org.springframework.boot.autoconfigure.cache.RedisCacheConfiguration}的默认CacheManager注入
     * springboot默认使用redis作为缓存
     * @see {@link org.springframework.boot.autoconfigure.cache.EhCacheCacheConfiguration}
     *
     * @return
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        // 使用FastJson序列化value
        GenericFastJsonRedisSerializer jsonRedisSerializer = new GenericFastJsonRedisSerializer();
        
        RedisCacheConfiguration cacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                // 默认缓存时间
                .entryTtl(Duration.ofMillis(1000 * 60 * 2))
                .disableCachingNullValues()
                // 自定义redisKey的前缀
                .computePrefixWith(cacheName -> cacheName.concat(":"))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonRedisSerializer));
        
        return RedisCacheManager.builder(factory)
                .cacheDefaults(cacheConfiguration)
                .build();
    }
}
