package com.tencent.timi.annualparty.service.impl;

import com.tencent.timi.annualparty.dao.impl.ActivityDaoImpl;
import com.tencent.timi.annualparty.entity.pojo.Option;
import com.tencent.timi.annualparty.service.IActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @author hhshan
 * @date 2023/12/20
 */
@Service
public class ActivityServiceImpl implements IActivityService {

    @Autowired
    private ActivityDaoImpl activityDao;


    public boolean deleteAll() {
        return activityDao.deleteAll();
    }

    public boolean chooseOption(Option option) {return activityDao.chooseOption(option);}

    public Integer getUserOption(Option option) {return activityDao.getUserOption(option);}

    public BigDecimal getQuestionAccuracy(Integer questionID) {return activityDao.getQuestionAccuracy(questionID);}

    public void questionBegin(Integer questionID) { activityDao.questionBegin(questionID);}

    public long getQuestionLastTime(Integer questionID) {return activityDao.getQuestionLastTime(questionID);}

    public boolean resetTimer(Integer qustionID) {return activityDao.resetTimer(qustionID);}

}
