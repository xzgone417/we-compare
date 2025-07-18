package com.tencent.timi.annualparty.dao;

import com.tencent.timi.annualparty.entity.pojo.QuestionAnswerBo;

import java.util.List;

/**
 * @author hhshan
 * @date 2023/12/20
 */
public interface IActivityDao {

    /**
     * 获取所有提交
     * @return
     */
    List<QuestionAnswerBo> getAllAnswer();

    /**
     * 获取指定用户在指定问题下的提交
     * @param userId
     * @param questionId
     * @return
     */
    QuestionAnswerBo getAllAnswerByUserOfQuestion(String userId, String questionId);

    /**
     * 获取指定用户在指定问卷下的所有提交
     * @param userId
     * @param questionnaireId
     * @return
     */
    List<QuestionAnswerBo> getAllAnswerByUserOfQuestionnaire(String userId, String questionnaireId);

    /**
     * 用户提交的问卷数据入库
     * @param answerBo
     * @return
     */
    boolean addQuestionAnswer(QuestionAnswerBo answerBo);
}
