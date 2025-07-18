package com.tencent.timi.annualparty.util;

import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * @author haoyangwei
 */
public class DateTimeUtil {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ZoneId TIMEZONE = ZoneId.of("Asia/Shanghai");

    /**
     * yyyy-mm-dd hh:mm:ss -> 时间戳
     *
     * @param datetime 日期
     * @return 时间戳
     */
    public static long getTimestamp(String datetime) {
        if (StringUtils.isBlank(datetime)) {
            return 0L;
        }
        try {
            LocalDateTime dateTime = LocalDateTime.parse(datetime.trim(), FORMATTER);
            return dateTime.atZone(TIMEZONE).toInstant().toEpochMilli();
        } catch (DateTimeParseException e) {
            return 0L;
        }
    }
}
