package com.redis.filteringapp;

import com.redis.om.spring.repository.RedisEnhancedRepository;

public interface StreamEventRepository extends RedisEnhancedRepository<StreamEvent, String> {
}
