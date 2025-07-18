package com.tencent.timi.annualparty.entity.pojo;

import lombok.Builder;
import lombok.Data;
import org.apache.commons.math3.util.Pair;

import java.util.List;

/**
 * @author hhshan
 * @date 2023/12/20
 */
@Data
@Builder(toBuilder = true)
public class SubmitQuestionnaireDto {
    String userId;
    String userName;

    String questionnireId;
    List<Pair<String, String>> questionList;
}
