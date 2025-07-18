package com.tencent.timi.annualparty.dao.impl;

import com.tencent.timi.annualparty.dao.IActivityDao;
import com.tencent.timi.annualparty.entity.pojo.Option;
import com.tencent.timi.annualparty.entity.pojo.QuestionAnswerBo;
import com.tencent.timi.annualparty.rainbow.PropertyReader;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * @author hhshan
 * @date 2023/12/20
 */
@Repository
public class ActivityDaoImpl implements IActivityDao {

    private static final String TOTALCOUNT = "total";

    private static final String QUESTIONTABLE = "question";

    private static final String QUESTIONTIMETABLE = "question-time";

    private static final String OPTIONKEY = "question-option";

    private static final String QUESTIONKEY = "question-num";

    private static final String RIGHTNUM = "right-answer-num";

    private static final String TOTALNUM = "total-num";

    private static final String QUESTIONBEGINKEY = "question-begin";

    private static final Logger LOGGER = LoggerFactory.getLogger(ActivityDaoImpl.class);

    private final StringRedisTemplate redisTemplate;

    @Autowired
    public ActivityDaoImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public List<QuestionAnswerBo> getAllAnswer() {
        return null;
    }


    @Override
    public QuestionAnswerBo getAllAnswerByUserOfQuestion(String userId, String questionId) {
        return null;
    }

    @Override
    public List<QuestionAnswerBo> getAllAnswerByUserOfQuestionnaire(String userId, String questionnaireId) {
        return null;
    }

    @Override
    public boolean addQuestionAnswer(QuestionAnswerBo answerBo) {
        return false;
    }


    public boolean deleteAll() {
        try{
            String deleteAllTag = PropertyReader.getValue("config", "deleteFormalData", "0");
            if (StringUtils.equals(deleteAllTag, "1")) {
                Set<String> keys = redisTemplate.keys("*");
                if (keys != null && !keys.isEmpty()) {
                    redisTemplate.delete(keys);
                }
                LOGGER.info("delete all data success");
            } else {
                Set<String> keys = redisTemplate.keys("*test");
                if (keys != null && !keys.isEmpty()) {
                    redisTemplate.delete(keys);
                }
                LOGGER.info("delete test data success");
            }
            return true;
        } catch (Exception e) {
            LOGGER.error("delete all data fail", e);
            return false;
        }
    }

    public boolean chooseOption(Option option) {
        Integer questionID = option.getQuestionID();
        String userName = option.getUserName();
        Integer userOption = option.getOption();
        Integer rightOption = Integer.parseInt(PropertyReader.getValue(QUESTIONTABLE, "question" + questionID, "1"));
        boolean isAnswerNotExist = redisTemplate.opsForHash().putIfAbsent(getQuestionOptionKey(questionID), userName, userOption.toString());
        if (isAnswerNotExist) {
            boolean isRightAnswer = userOption.equals(rightOption);
            if (isRightAnswer) {
                redisTemplate.opsForHash().increment(getQuestionNumKey(questionID), RIGHTNUM, 1);
            }
            redisTemplate.opsForHash().increment(getQuestionNumKey(questionID), TOTALNUM, 1);
            return isRightAnswer;
        } else {
            Object userOptionObject = redisTemplate.opsForHash().get(getQuestionOptionKey(questionID), userName);
            Integer preOption = Integer.valueOf((String) userOptionObject);
            return preOption.equals(rightOption);
        }
    }

    public Integer getUserOption(Option option) {
        Integer questionID = option.getQuestionID();
        String userName = option.getUserName();
        Object userOptionObject = redisTemplate.opsForHash().get(getQuestionOptionKey(questionID), userName);
        LOGGER.info("user: {} question {} option {}", option.getUserName(), option.getQuestionID(), userOptionObject);
        if(userOptionObject == null) {
            return -1;
        } else {
            return  Integer.valueOf((String) userOptionObject);
        }
    }

    public BigDecimal getQuestionAccuracy(Integer questionID) {
        Object rightNumObject = redisTemplate.opsForHash().get(getQuestionNumKey(questionID), RIGHTNUM);
        Object totalNumObject = redisTemplate.opsForHash().get(getQuestionNumKey(questionID), TOTALNUM);
        Integer addAccuracy = Integer.valueOf(PropertyReader.getValue("config", "addAccuracy", "0"));
        BigDecimal addAccuracyDecimal = new BigDecimal(addAccuracy);
        if (rightNumObject != null && totalNumObject != null
                && rightNumObject instanceof String && totalNumObject instanceof String) {
            BigDecimal rightNum = new BigDecimal(Integer.valueOf((String) rightNumObject));
            BigDecimal totalNum = new BigDecimal(Integer.valueOf((String) totalNumObject));
            BigDecimal result = new BigDecimal(0).setScale(6, BigDecimal.ROUND_HALF_UP);
            if (!totalNum.equals(0)) {
                result = rightNum.divide(totalNum, 6, RoundingMode.HALF_UP);
                result = result.add(addAccuracyDecimal.divide(new BigDecimal(100), 6, RoundingMode.HALF_UP));
            }
            if (result.compareTo(BigDecimal.ONE) > 0) {
                return BigDecimal.ONE.setScale(6, BigDecimal.ROUND_HALF_UP);
            } else if (result.compareTo(BigDecimal.ZERO) < 0) {
                return BigDecimal.ZERO.setScale(6, BigDecimal.ROUND_HALF_UP);
            } else {
                return result;
            }
        } else {
            BigDecimal bigDecimal = new BigDecimal(0).setScale(6, BigDecimal.ROUND_HALF_UP);
            return bigDecimal;
        }

    }

    public void questionBegin(Integer questionID) {
        String questionBeginKey = getQuestionBeginKey(questionID);

        String timeout = PropertyReader.getValue(QUESTIONTIMETABLE, getQuestionRainbowKey(questionID), "120");

        LOGGER.info("question {} timeout {}", questionID, timeout);

        Instant now = Instant.now();
        Instant expireTime = now.plus(Integer.valueOf(timeout), ChronoUnit.SECONDS);

        redisTemplate.opsForValue().set(questionBeginKey, expireTime.toString());

    }

    public long getQuestionLastTime(Integer questionID) {
        Instant now = Instant.now();
        String expireTimeStr = redisTemplate.opsForValue().get(getQuestionBeginKey(questionID));
        if(expireTimeStr != null && !expireTimeStr.isEmpty()) {
            Instant expireTime = Instant.parse(expireTimeStr);
            long lastTime = Duration.between(now, expireTime).getSeconds();
            if(lastTime < 0) {
                return -2;
            } else {
                return lastTime;
            }
        } else {
            return -1;
        }
    }

    public boolean resetTimer(Integer questionID) {
        return  redisTemplate.delete(getQuestionBeginKey(questionID));
    }


    public String getQuestionOptionKey(Integer questionID) {
        return OPTIONKEY + "-" + questionID;
    }

    public String getQuestionNumKey(Integer questionID) {
        return QUESTIONKEY + "-" + questionID;
    }

    public String getQuestionBeginKey(Integer questionID) {return QUESTIONBEGINKEY + "-" + questionID;}

    public String getQuestionRainbowKey(Integer questionID) {return QUESTIONTABLE + questionID;}
}
