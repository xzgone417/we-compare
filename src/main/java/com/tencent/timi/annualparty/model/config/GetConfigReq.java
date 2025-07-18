package com.tencent.timi.annualparty.model.config;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author haoyangwei
 */
@Getter
@Setter
public class GetConfigReq {
    private List<ConfigKey> keys;
}
