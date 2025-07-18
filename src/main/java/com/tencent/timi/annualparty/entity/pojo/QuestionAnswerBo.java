package com.tencent.timi.annualparty.entity.pojo;

import lombok.Builder;
import lombok.Data;

/**
 * @author hhshan
 * @date 2023/12/20
 */
@Data
@Builder(toBuilder = true)
public class QuestionAnswerBo {
    String userId;
    String questionnaireId;
    String questionId;

    public QuestionAnswerBo(String userId, String questionnaireId, String questionId, String answer) {
        this.userId = userId;
        this.questionnaireId = questionnaireId;
        this.questionId = questionId;
        this.answer = answer;
    }

    String answer;
}
