package com.tencent.timi.annualparty.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author mingxuanyin
 */
public class TimeResponse {
    @JsonProperty("code")
    private int code;
    @JsonProperty("message")
    private String message;
    @JsonProperty("expireTime")
    private long expireTime;

    public TimeResponse (int code, String message, long data) {
        this.code = code;
        this.message = message;
        this.expireTime = data;
    }
}
