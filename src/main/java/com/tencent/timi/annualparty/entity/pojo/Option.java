package com.tencent.timi.annualparty.entity.pojo;

import lombok.Builder;
import lombok.Data;

/**
 * @author mingxuanyin
 */
@Data
@Builder(toBuilder = true)
public class Option {
    Integer questionID;

    String userName;

    Integer option;
}
