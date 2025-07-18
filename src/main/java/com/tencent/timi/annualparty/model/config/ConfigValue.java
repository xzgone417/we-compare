package com.tencent.timi.annualparty.model.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author haoyangwei
 */
@AllArgsConstructor
@Getter
@Setter
public class ConfigValue {
    private String table;
    private String key;
    private String value;
}
