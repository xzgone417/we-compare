package com.tencent.timi.annualparty.entity.pojo;

import lombok.Builder;
import lombok.Data;

/**
 * @author hhshan
 * @date 2023/12/20
 */
@Data
@Builder(toBuilder = true)
public class QuestionDo {
    String id;
    String name;
    String content;
    int type;
}
