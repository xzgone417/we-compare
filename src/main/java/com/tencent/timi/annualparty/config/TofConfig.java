package com.tencent.timi.annualparty.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author hhshan
 * @date 2023/12/22
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "tof")
public class TofConfig {
    private String token;
}
