package com.tencent.timi.annualparty.entity.transfer;

import com.tencent.timi.annualparty.entity.pojo.QuestionBo;
import com.tencent.timi.annualparty.entity.pojo.QuestionDo;
import com.tencent.timi.annualparty.entity.pojo.QuestionType;
import org.apache.commons.math3.util.Pair;

/**
 * @author hhshan
 * @date 2023/12/20
 */
public class QuestionConverter {

    public static QuestionBo toBo(QuestionDo questionDo) {
        QuestionBo.QuestionBoBuilder builder = QuestionBo.builder();
        builder.id(questionDo.getId())
                .name(questionDo.getName())
                .content(questionDo.getContent())
                .type(QuestionType.forNumber(questionDo.getType()))
                .build();

        return builder.build();
    }

    public static QuestionDo toDo(QuestionBo questionBo) {
        QuestionDo.QuestionDoBuilder builder = QuestionDo.builder();
        builder.id(questionBo.getId())
                .name(questionBo.getName())
                .content(questionBo.getContent())
                .type(QuestionType.forValue(questionBo.getType()))
                .build();

        return builder.build();
    }
}
