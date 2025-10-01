package com.wonsu.used_market.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching // 스프링 캐쉬 기능 활성화를 위한 애노테이션
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    //Redis 서버 연결
    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        // Lettuce라는 라이브러리를 활용해 Redis 연결을 관리하는 객체를 생성하고
        // Redis 서버에 대한 정보(host, port)를 설정한다.
        return new LettuceConnectionFactory(new RedisStandaloneConfiguration(host, port));
    }

    // Redis 문자열 기반 조작을 위한 Template,리프레쉬토큰과 관련되어 있음
    @Bean
    public StringRedisTemplate stringRedisTemplate(LettuceConnectionFactory redisConnectionFactory) {
        return new StringRedisTemplate(redisConnectionFactory);
    }

    //스프링 캐시 추상화에서 사용하는 캐시 매니저
    @Bean
    public RedisCacheManager cacheManager(LettuceConnectionFactory redisConnectionFactory) {
        //로컬데이트타임을 쓰기위하여
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);



        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                //기본 캐시 시간을 10분으로 설정
                .entryTtl(Duration.ofMinutes(10))

                // 캐시 키를 문자열로 직렬화
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        org.springframework.data.redis.serializer.RedisSerializer.string()))

                // 캐시 밸류를 json으로 직렬화
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));

        //캐시 이름별 TTL 개별 설정
        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
        cacheConfigs.put("productList",defaultConfig.entryTtl(Duration.ofMinutes(2)));
        cacheConfigs.put("productDetail",defaultConfig.entryTtl(Duration.ofMinutes(10)));

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }
}
