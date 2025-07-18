package com.tencent.timi.annualparty.verify;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ScanVerification {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScanVerification.class);

    public boolean checkLeakScan(Map<String, String> requestHeaderMap) {
        if (requestHeaderMap == null) {
            LOGGER.error("Request Header is null");
            return false;
        }
        final String TST_CODE = "TST";
        final String LEAK_SCAN_PARA = "Tencent-Leakscan";
        String paramVal = requestHeaderMap.getOrDefault(LEAK_SCAN_PARA.toLowerCase(), null);
        if (paramVal != null) {
            if (paramVal.contains(TST_CODE)) {
                return true;
            }
        }
        final String UA_PARA = "user-agent";
        String uaVal = requestHeaderMap.getOrDefault(UA_PARA.toLowerCase(), null);
        if (uaVal != null) {
            if (uaVal.contains(TST_CODE)) {
                return true;
            }
        }
        final String COOKIE_PARA = "cookie";
        final String INVALIS_QQ_1 = "347486423";
        final String INVALIS_QQ_2 = "632789064";
        String cookieVal = requestHeaderMap.getOrDefault(COOKIE_PARA.toLowerCase(), null);
        if (cookieVal != null) {
            if (cookieVal.contains(INVALIS_QQ_1) || cookieVal.contains(INVALIS_QQ_2)) {
                return true;
            }
        }
        return false;
    }
}
