package com.tencent.timi.annualparty.entity.transfer;

import com.tencent.timi.annualparty.entity.pojo.QuestionBo;
import com.tencent.timi.annualparty.entity.pojo.QuestionDo;
import com.tencent.timi.annualparty.entity.pojo.QuestionnaireBo;
import com.tencent.timi.annualparty.entity.pojo.QuestionnaireDo;

/**
 * @author hhshan
 * @date 2023/12/20
 */
public class QuestionnaireConverter {

    public static QuestionnaireBo toBo(QuestionnaireDo questionnaireDo) {
        QuestionnaireBo.QuestionnaireBoBuilder builder = QuestionnaireBo.builder();
        builder.id(questionnaireDo.getId())
                .name(questionnaireDo.getName());
        return builder.build();
    }

    public static QuestionnaireDo toDo(QuestionnaireBo questionnaireBo) {
        QuestionnaireDo.QuestionnaireDoBuilder builder = QuestionnaireDo.builder();
        builder.id(questionnaireBo.getId())
                .name(questionnaireBo.getName());
        return builder.build();
    }
}
