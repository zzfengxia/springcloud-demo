package com.zz.eureka.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2018-06-09 17:44
 * @desc redis缓存配置。自定义key生成格式，自定义序列化方式等。
 * ************************************
 */
@Configuration
public class RedisConfig extends CachingConfigurerSupport {
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        om.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        return om;
    }

    /**
     * 自定义RedisTemplate配置覆盖默认redis注入
     * 查看默认配置{@link org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration}
     *
     * @return
     */
    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, String> cusRedisTemplate = new StringRedisTemplate();

        // 这里如果使用FastJson来序列化value，会由于转义符规则不同导致sptsm无法解析缓存数据。所以要注释掉
        // 使用FastJson序列化value
        //GenericFastJsonRedisSerializer jsonRedisSerializer = new GenericFastJsonRedisSerializer();
        //cusRedisTemplate.setValueSerializer(jsonRedisSerializer);
        //cusRedisTemplate.setHashValueSerializer(jsonRedisSerializer);

        cusRedisTemplate.setConnectionFactory(factory);
        // 手动初始化
        cusRedisTemplate.afterPropertiesSet();

        return cusRedisTemplate;
    }
}
