package com.tencent.timi.annualparty.controller;

import com.tencent.timi.annualparty.config.TofConfig;
import com.tencent.timi.annualparty.entity.pojo.Option;
import com.tencent.timi.annualparty.rainbow.PropertyReader;
import com.tencent.timi.annualparty.service.impl.ActivityServiceImpl;
import com.tencent.timi.annualparty.util.tof.AuthStaff;
import com.tencent.timi.annualparty.util.tof.TofUtil;
import com.tencent.timi.annualparty.verify.ScanVerification;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @author hhshan
 * @date 2023/12/20
 */
@RestController
@RequestMapping("/annual")
@CrossOrigin
public class ActivityController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ActivityController.class);

    @Autowired
    private ActivityServiceImpl activityService;

    @Autowired
    private TofConfig tofConfig;

    @Autowired
    private ScanVerification verification;

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 获取当前阶段
     * 0：投票尚未开始
     * 1：投票正在进行
     * 2：投票已经结束
     */
    public int getStage() {
        String stageTag = StringUtils.trim(PropertyReader.getValue("config", "stageTag", "0"));
        return Integer.parseInt(stageTag);
    }

    @GetMapping("/delete/all")
    public ResponseEntity deleteAll(@RequestHeader Map<String, String> headerJsonMap) {
//        if (verification.checkLeakScan(headerJsonMap)) {
//            LOGGER.info("LeakScan Request , Discard.");
//            return new ResponseEntity("OK", HttpStatus.OK);
//        }
        if (activityService.deleteAll()) {
            return new ResponseEntity("delete all data", HttpStatus.OK);
        } else {
            return new ResponseEntity("delete data fail", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 检测鉴权接口
     *
     * @return
     */
    @GetMapping("/auth")
    public ResponseEntity getLoginUserInfo(@RequestHeader Map<String, String> headerJsonMap) {

        // 解析获取用户信息
        AuthStaff staff = TofUtil.parse(headerJsonMap, tofConfig.getToken());
        if (staff == null) {
            LOGGER.info("Parse user From {} get null", headerJsonMap);
            return new ResponseEntity("no auth", HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity(staff.toString(), HttpStatus.OK);
    }

    @GetMapping("/index")
    public ResponseEntity hello() {
        return new ResponseEntity("Hello", HttpStatus.OK);
    }

    @GetMapping("/LdpCr0C1ci.txt")
    public ResponseEntity check() {
        return new ResponseEntity("ffca6a8134904b7ebdc737773f608456", HttpStatus.OK);
    }

    @PostMapping("/choose")
    public ResponseEntity chooseOption(@RequestHeader Map<String, String> headerJsonMap,
                                       @RequestParam(value = "userName", required = false) String user,
                                       @RequestBody Map<String, String> data) {
//        if (verification.checkLeakScan(headerJsonMap)) {
//            LOGGER.info("LeakScan Request , Discard.");
//            return new ResponseEntity("OK", HttpStatus.OK);
//        }
//        String isTestMode = PropertyReader.getValue("config", "isTestMode", "0");
//        AuthStaff staff = TofUtil.parse(headerJsonMap, tofConfig.getToken());
//        if (staff != null) {
//            LOGGER.info("receive request, staff name {} staff chinese name {}", staff.getLoginName(), staff.getChineseName());
//        }
//        String userName = (staff != null) ? staff.getLoginName() : null;
//        if (userName == null && StringUtils.equals(isTestMode, "1")) {
//            userName = user;
//        }
//        if (userName == null) {
//            LOGGER.info("Parse user From {} get null", headerJsonMap);
//            return new ResponseEntity("no auth", HttpStatus.UNAUTHORIZED);
//        }
        String userName = "";
        Integer questionID = 0;
        Integer option = 0;
        try {
            questionID = Integer.valueOf(data.getOrDefault("questionID", "0"));
            option = Integer.valueOf(data.getOrDefault("option", "0"));
        } catch (NumberFormatException e) {
            LOGGER.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("请求体参数类型错误");
        }
        LOGGER.info("test choose username {}", userName);
        if (questionID == 0 || option == 0) {
            return new ResponseEntity("数据缺失", HttpStatus.BAD_REQUEST);
        }
        Option userOption = Option.builder()
                .questionID(questionID)
                .userName(userName)
                .option(option)
                .build();
        boolean isRightOption = activityService.chooseOption(userOption);
        return new ResponseEntity(isRightOption, HttpStatus.OK);
    }

    @GetMapping("/get/option")
    public ResponseEntity getUserOption(@RequestHeader Map<String, String> headerJsonMap,
                                        @RequestParam(value = "questionID") Integer questionID,
                                        @RequestParam(value = "userName", required = false) String user) {
//        if (verification.checkLeakScan(headerJsonMap)) {
//            LOGGER.info("LeakScan Request , Discard.");
//            return new ResponseEntity("OK", HttpStatus.OK);
//        }
//        String isTestMode = PropertyReader.getValue("config", "isTestMode", "0");
//        AuthStaff staff = TofUtil.parse(headerJsonMap, tofConfig.getToken());
//        String userName = (staff != null) ? staff.getLoginName() : null;
//        if (userName == null && StringUtils.equals(isTestMode, "1")) {
//            userName = user;
//        }
//        if (userName == null) {
//            LOGGER.info("Parse user From {} get null", headerJsonMap);
//            return new ResponseEntity("no auth", HttpStatus.UNAUTHORIZED);
//        }
        String userName = "";
        Option option = Option.builder()
                .questionID(questionID)
                .userName(userName)
                .option(-1)
                .build();
        Integer userOption = activityService.getUserOption(option);
        if (userOption != -1) {
            return new ResponseEntity(userOption, HttpStatus.OK);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("no option found for the user");
        }
    }

    @GetMapping("/get/accuracy")
    public ResponseEntity getQuestionAccuracy(@RequestHeader Map<String, String> headerJsonMap,
                                              @RequestParam(value = "questionID") Integer questionID) {
//        if (verification.checkLeakScan(headerJsonMap)) {
//            LOGGER.info("LeakScan Request , Discard.");
//            return new ResponseEntity("OK", HttpStatus.OK);
//        }
        BigDecimal accuracy = activityService.getQuestionAccuracy(questionID);
        return new ResponseEntity(accuracy.toString(), HttpStatus.OK);
    }

    @PostMapping("/begin")
    public ResponseEntity receiveQuestionBegin(@RequestHeader Map<String, String> headerJsonMap,
                                               @RequestBody Integer questionID) {
//        if (verification.checkLeakScan(headerJsonMap)) {
//            LOGGER.info("LeakScan Request , Discard.");
//            return new ResponseEntity("OK", HttpStatus.OK);
//        }
        activityService.questionBegin(questionID);
        return new ResponseEntity("答题开始", HttpStatus.OK);
    }

    @GetMapping("/get/time")
    public ResponseEntity getQuestionLastTime(@RequestHeader Map<String, String> headerJsonMap,
                                              @RequestParam(value = "questionID") Integer questionID) {
//        if (verification.checkLeakScan(headerJsonMap)) {
//            LOGGER.info("LeakScan Request , Discard.");
//            return new ResponseEntity("OK", HttpStatus.OK);
//        }
        long expireTime = activityService.getQuestionLastTime(questionID);

        if (expireTime >= 0) {
            return new ResponseEntity(expireTime, HttpStatus.OK);
        } else if (expireTime == -1) {
            return new ResponseEntity("问题尚未开始", HttpStatus.FORBIDDEN);
        } else {
            return new ResponseEntity("问题已经结束", HttpStatus.FORBIDDEN);
        }
    }

    @PostMapping("/reset/time")
    public ResponseEntity resetTimer(@RequestHeader Map<String, String> headerJsonMap,
                                     @RequestBody Integer questionID) {
//        if (verification.checkLeakScan(headerJsonMap)) {
//            LOGGER.info("LeakScan Request , Discard.");
//            return new ResponseEntity("OK", HttpStatus.OK);
//        }
        boolean result = activityService.resetTimer(questionID);
        if (result) {
            return new ResponseEntity<>("重置计时器成功", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("重置计时器失败", HttpStatus.BAD_REQUEST);
        }
    }


}
