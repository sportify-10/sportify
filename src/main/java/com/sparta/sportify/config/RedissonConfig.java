package com.sparta.sportify.config;

import java.time.Duration;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.spring.data.connection.RedissonConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;

@EnableCaching
@Configuration
public class RedissonConfig {
	@Value("${spring.data.redis.host}")
	private String redisHost;

	@Value("${spring.data.redis.port}")
	private int redisPort;

	private static final String REDISSON_HOST_PREFIX = "redis://";

	@Bean
	public RedissonClient redissonClient() {
		RedissonClient redisson = null;
		Config config = new Config();
		config.useSingleServer().setAddress(REDISSON_HOST_PREFIX + redisHost + ":" + redisPort);
		redisson = Redisson.create(config);
		return redisson;
	}

	@Bean
	public ObjectMapper objectMapper() {
		return new ObjectMapper();
	}

	// spring의 캐시 타입 Redis로 설정한다. (이 과정이 있다면 spring.cache.type 설정 생략 가능)
	@Bean
	public CacheManager cacheManager(RedisConnectionFactory cf) {
		return RedisCacheManager.RedisCacheManagerBuilder
			.fromConnectionFactory(cf)
			.cacheDefaults(redisCacheConfiguration())
			.build();
	}

	// Redis 연결 설정
	@Bean
	public RedisConnectionFactory redisConnectionFactory() {
		return new RedissonConnectionFactory();
	}

	// Redis 캐시 설정
	@Bean
	public RedisCacheConfiguration redisCacheConfiguration() {
		return RedisCacheConfiguration.defaultCacheConfig()
			.serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())) // 키를 String으로 직렬화해 저장
			.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer(objectMapper()))) // 객체를 JSON으로 바꿔서 Redis에 저장
			.entryTtl(Duration.ofDays(1L));
	}

	// Redis의 세밀한 조작이 필요할 경우 RedisTemplate를 빈 등록하면된다.
	// Redis의 모든 자료구조 다 쓸 수 있음 (String, List, Hash, Set 등)
	// 간단한 캐시 조작이라면 추가하지 않아도 된다.
	// @Bean
	// public RedisTemplate<?, ?> redisTemplate() {
	// 	RedisTemplate<?, ?> redisTemplate = new RedisTemplate<>();
	// 	redisTemplate.setConnectionFactory(redisConnectionFactory());
	// 	redisTemplate.setDefaultSerializer(new StringRedisSerializer());
	// 	return redisTemplate;
	// }
}
