package com.tencent.timi.annualparty.entity.pojo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author hhshan
 * @date 2023/12/20
 */
@Data
@Builder(toBuilder = true)
public class QuestionnaireDo {
    String id;
    String name;
}
