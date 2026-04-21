package com.goodee.beedan.config.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@EnableCaching
public class RedisConfig implements CachingConfigurer {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.activateDefaultTyping(
                objectMapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL
        );

        GenericJackson2JsonRedisSerializer jsonSerializer =
                new GenericJackson2JsonRedisSerializer(objectMapper);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(jsonSerializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(jsonSerializer);
        template.afterPropertiesSet();

        return template;
    }

    @Bean(name = "thumbnailRedisTemplate")
    public RedisTemplate<String, byte[]> thumbnailRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, byte[]> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(RedisSerializer.byteArray());

        return template;
    }

    // @Cacheable이 실제로 어떤 규칙으로 Redis에 저장될지 정하는 설정
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.activateDefaultTyping(
                objectMapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL
        );
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())) // key는 문자열로 저장
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer)) // value는 JSON 직렬화해서 저장
                .entryTtl(Duration.ofHours(1)); // 기본 캐시 만료 시간

        //  캐시 이름별로 만료 시간(TTL)을 다르게 설정하고 싶을 때 사용가능
        Map<String, RedisCacheConfiguration> customConfigs = new HashMap<>();

        customConfigs.put("shipment:customs", defaultConfig.entryTtl(Duration.ofHours(3)));

        // 챗봇 조회 데이터는 변경이 거의 없어서 30분 캐시로 운영
        customConfigs.put("chatbot:flow", defaultConfig.entryTtl(Duration.ofMinutes(30)));

        // 국내 배송 조회 데이터는 10분 캐시로 운영 (너무 오래 캐시하면 배송 상태가 업데이트 되어도 반영이 안될 수 있어서)
        customConfigs.put("shipment:delivery", defaultConfig.entryTtl(Duration.ofMinutes(10)));

        /* 이런식으로 적용 가능
         customConfigs.put("display:exchangeRate", defaultConfig.entryTtl(Duration.ofDays(1)));
         customConfigs.put("display:newStocks", defaultConfig.entryTtl(Duration.ofMinutes(10)));
         */

        // 최종적으로 Spring Cache가 쓰는 매니저(RedisCacheManager) 생성
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(customConfigs)
                .build();
    }

    // Redis 장애 시 DB로 우회하게 만드는 최후의 방어선

    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(@Nonnull RuntimeException exception, @Nonnull Cache cache, @Nonnull Object key)
            {// Redis가 죽어서 데이터를 못 가져오면 에러를 뿜지 않고 원래 하려던 DB 조회(@Cacheable 달린 메서드 원본)를 실행
                log.warn( // 캐시를 읽을 수 없습니다. DB에서 직접 조회합니다.
                        "[Redis Cache GET FAIL] cache={}, key={}, fallback=method-execution, exceptionType={}, message={}",
                        cache.getName(),
                        key,
                        exception.getClass().getSimpleName(),
                        exception.getMessage()
                );
            }

            @Override
            public void handleCachePutError(@Nonnull RuntimeException exception, @Nonnull Cache cache, @Nonnull Object key,
                                            Object value) {
                log.warn( //캐시를 저장할 수 없습니다. 저장을 생략합니다.
                        "[Redis Cache PUT FAIL] cache={}, key={}, impact=cache-not-updated(stale-possible), exceptionType={}, message={}",
                        cache.getName(),
                        key,
                        exception.getClass().getSimpleName(),
                        exception.getMessage()
                );
            }

            @Override
            public void handleCacheEvictError(@Nonnull RuntimeException exception, @Nonnull Cache cache, @Nonnull Object
                    key) {
                log.warn( // 캐시 삭제 실패.
                        "[Redis Cache EVICT FAIL] cache={}, key={}, impact=stale-cache-possible, exceptionType={}, message={}",
                        cache.getName(),
                        key,
                        exception.getClass().getSimpleName(),
                        exception.getMessage()
                );
            }

            @Override
            public void handleCacheClearError(@Nonnull RuntimeException exception, @Nonnull Cache cache) {
                log.warn( // 캐시 초기화 실패.
                        "[Redis Cache CLEAR FAIL] cache={}, impact=stale-cache-possible, exceptionType={}, message={}",
                        cache.getName(),
                        exception.getClass().getSimpleName(),
                        exception.getMessage()
                );
            }
        };
    }

}
