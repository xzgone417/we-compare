package com.tencent.timi.annualparty.util.tof;

import lombok.Builder;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;

/**
 * @author hhshan
 * @date 2023/12/21
 */
@Data
@Builder(toBuilder = true)
public class AuthStaff {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthStaff.class);

    // 用户Id
    private Long staffId;
    // 企业微信名
    private String loginName;
    private String chineseName;
    private Integer deptId;
    private String deptName;
    // 微信Id
    private String openId;

    public static AuthStaff getInstance(Map<String, Object> data) {
        AuthStaff.AuthStaffBuilder builder = AuthStaff.builder();
        try {
            builder.staffId((Long) data.getOrDefault("StaffId", -1))
                    .loginName((String) data.getOrDefault("LoginName", "unknown"))
                    .openId((String) data.getOrDefault("open_id", "unknown"));
        } catch (Exception e) {
            LOGGER.error("AuthStaff.getInstance Error", e);
            return null;
        }
        return builder.build();
    }
}