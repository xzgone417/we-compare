package com.tencent.timi.annualparty.verify;

import com.tencent.timi.annualparty.rainbow.Whitelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author hhshan
 * @date 2023/12/21
 */
public class AuthVerify {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthVerify.class);

    /**
     * 验证用户是否在白名单中
     * @param userNameEng
     * @return
     */
    public static boolean checkInWhiteList(String userNameEng) {
        if (Whitelist.contains("names.txt", userNameEng)) {
            LOGGER.info("Check user-{} in WhiteList", userNameEng);
            return true;
        } else {
            LOGGER.info("Check user-{} Not in WhiteList", userNameEng);
            return false;
        }
    }
}
