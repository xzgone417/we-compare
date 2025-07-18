package com.tencent.timi.annualparty.rainbow;

import com.tencent.rainbow.base.callback.ListenCallback;
import com.tencent.rainbow.base.config.RainbowGroup;
import com.tencent.rainbow.base.entity.RainbowChangeInfo;
import com.tencent.rainbow.base.entity.RainbowInfo;
import com.tencent.rainbow.sdk.RainbowSdkApplication;
import org.apache.commons.lang3.StringUtils;

/**
 * @author haoyangwei
 */
class Rainbow {
    private static final RainbowSdkApplication APPLICATION = new RainbowSdkApplication();
    private static final RainbowConfig CONFIGURE = new RainbowConfig();

    private static class RainbowConfig {
        public final String appId = "edde067f-0d1d-4df5-b577-5a2dd96656b2";
        public final String environ = "2025AnnualParty";
        public final String userId = "a7985c34b2754549a7aa1b3820da545b";
        public final String secretKey = "9d3923deaa8daff21d9da31483be005004fd";

        public RainbowInfo getRainbowInfo(String table) {
            return new RainbowInfo(appId, environ, table, userId, secretKey);
        }
    }

    public static RainbowGroup getGroup(String group) {
        if (StringUtils.isBlank(group)) {
            throw new NullPointerException();
        }
        return APPLICATION.getGroup(CONFIGURE.getRainbowInfo(group));
    }


    public static RainbowGroup getGroupAndOpenCallBack(String group, ListenCallback<RainbowChangeInfo> callback) {
        if (StringUtils.isBlank(group)) {
            throw new NullPointerException();
        }
        if (callback == null) {
            return getGroup(group);
        }
        return APPLICATION.getGroupAndOpenCallBack(CONFIGURE.getRainbowInfo(group), callback);
    }
}
