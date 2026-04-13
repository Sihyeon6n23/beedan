package com.goodee.beedan.redisTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class RedisTemplateTest {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Test
    void strTest(){
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        String key = "stringKey";

        valueOperations.set(key, "hello");

        String value = valueOperations.get(key);
        assertThat(value).isEqualTo("hello");
    }

    @Test
    void testSet() {
        SetOperations<String, String> setOperations = redisTemplate.opsForSet();
        String key = "setKey";

        // when
        setOperations.add(key, "h", "e", "l", "l", "o");

        Set<String> members = setOperations.members(key);
        Long size = setOperations.size(key);

        assertThat(members).containsOnly("h", "e", "l", "o");
        assertThat(size).isEqualTo(4);
    }

    @Test
    void testHash() {
        HashOperations<String, Object, Object> hashOperations = redisTemplate.opsForHash();
        String key = "hashKey";

        hashOperations.put(key, "hello", "world");

        Object value = hashOperations.get(key, "hello");
        assertThat(value).isEqualTo("world");

        Map<Object, Object> entries = hashOperations.entries(key);
        assertThat(entries.keySet()).containsExactly("hello");
        assertThat(entries.values()).containsExactly("world");

        Long size = hashOperations.size(key);
        assertThat(size).isEqualTo(entries.size());
    }



}
