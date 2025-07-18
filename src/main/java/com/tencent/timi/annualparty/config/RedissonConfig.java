package com.tencent.timi.annualparty.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redisson() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://127.0.0.1:6379")
                .setPassword("xzgone1");
//                .setAddress("redis://11.141.65.183:6380")
//                .setPassword("DrUxQ*2768SmsY");

        config.setLockWatchdogTimeout(3000);
        return Redisson.create(config);
    }
}