package com.tencent.timi.annualparty.model.config;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author haoyangwei
 * @date 2023/12/20
 */
@Getter
@Setter
public class GetConfigRes {
    private List<ConfigValue> values;
}
