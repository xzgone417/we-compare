package com.tencent.timi.annualparty.entity.pojo;

import java.util.HashMap;
import java.util.Map;

/**
 * @author hhshan
 * @date 2023/12/20
 */
public enum QuestionType {
    /* 未知 */
    UNKNOWN(0),
    /* 是否 */
    YES_OR_NOT_CHECK(1),
    /* 四个选项 */
    FOUR_ITEMS_CHOOSE(2),
    /* 内容文本 */
    CONTENT_TEXT(3);

    private static final Map<Integer, QuestionType> NUMBERS;

    static {
        NUMBERS = new HashMap<>();
        for (QuestionType type : QuestionType.values()) {
            NUMBERS.put(type.value, type);
        }
    }

    private final int value;

    QuestionType(int value) {
        this.value = value;
    }

    public static QuestionType forNumber(int value) {
        return NUMBERS.getOrDefault(value, UNKNOWN);
    }

    public static int forValue(QuestionType type) {
        return type.value;
    }
}
