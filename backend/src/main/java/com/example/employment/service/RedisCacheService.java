package com.example.employment.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.function.Supplier;

@Service
public class RedisCacheService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.redis-cache.enabled:true}")
    private Boolean cacheEnabled;

    public RedisCacheService(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public <T> T getOrLoad(String key, TypeReference<T> typeReference, Duration ttl, Supplier<T> loader) {
        if (!Boolean.TRUE.equals(cacheEnabled)) {
            return loader.get();
        }
        try {
            String cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                return objectMapper.readValue(cached, typeReference);
            }
        } catch (Exception ignored) {
            return loader.get();
        }

        T value = loader.get();
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(value), ttl);
        } catch (Exception ignored) {
            // Redis is an acceleration layer. Database access remains the fallback.
        }
        return value;
    }

    public void evict(String key) {
        if (!Boolean.TRUE.equals(cacheEnabled)) {
            return;
        }
        try {
            redisTemplate.delete(key);
        } catch (Exception ignored) {
            // Cache eviction failure should not block the main business flow.
        }
    }
}
