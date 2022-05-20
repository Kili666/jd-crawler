package com.lqjai.common.config;//package cn.ecnu.trace.common.config;
//
//import com.alibaba.fastjson.support.spring.FastJsonRedisSerializer;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.redis.connection.RedisConnectionFactory;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.serializer.StringRedisSerializer;
//
///**
// * @author lqj
// * @date 2021/2/1 Redis 序列化配置类
// */
//@Configuration
//public class RedisTemplaConfig {
//
//	/**
//	 * redisTemplate 默认的序列化方式为 JdkSerializationRedisSerializer
//	 * StringRedisTemplate 的默认序列化方式为 StringRedisSerializer
//	 * 使用 fastJsonRedisSerializer 替换默认序列化方式
//	 */
//	@Bean
//	public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
//		RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
//		redisTemplate.setConnectionFactory(redisConnectionFactory);
//
//		FastJsonRedisSerializer<Object> fastJsonRedisSerializer = new FastJsonRedisSerializer<>(Object.class);
//
//		// 设置值value的序列化方式
//		redisTemplate.setValueSerializer(fastJsonRedisSerializer);
//		redisTemplate.setHashValueSerializer(fastJsonRedisSerializer);
//
//		// 设置键key的序列化方式
//		redisTemplate.setKeySerializer(new StringRedisSerializer());
//		redisTemplate.setHashKeySerializer(new StringRedisSerializer());
//
//		redisTemplate.afterPropertiesSet();
//		return redisTemplate;
//	}
//}