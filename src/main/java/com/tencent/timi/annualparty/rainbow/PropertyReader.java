package com.tencent.timi.annualparty.rainbow;

import com.tencent.rainbow.base.callback.ListenCallback;
import com.tencent.rainbow.base.config.KvGroup;
import com.tencent.rainbow.base.config.RainbowGroup;
import com.tencent.rainbow.base.entity.RainbowChangeInfo;
import com.tencent.rainbow.base.enums.GroupType;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author haoyangwei
 */
public class PropertyReader {
    private static final PropertyReader INSTANCE = new PropertyReader();
    private final Listener listener = new Listener(this);
    private final Map<String, Map<String, String>> properties = new ConcurrentHashMap<>();

    private static class Listener implements ListenCallback<RainbowChangeInfo> {
        private final PropertyReader instance;

        private Listener(PropertyReader instance) {
            this.instance = instance;
        }

        @Override
        public void callback(RainbowChangeInfo change) {
            if (change == null) {
                return;
            }
            String table = change.getGroup();
            if (StringUtils.isBlank(table)) {
                return;
            }
            RainbowGroup group = Rainbow.getGroup(table);
            instance.properties.put(table, getGroupData(group));
        }
    }

    private PropertyReader() {
    }

    /**
     * 获取七彩石上KV表配置
     *
     * @param table 表名
     * @param key   键名
     * @param def   值不存在时的返回值
     * @return 键对应的值, 如果没有配置项则返回def
     */
    public static String getValue(String table, String key, String def) {
        if (StringUtils.isBlank(table) || StringUtils.isBlank(key)) {
            return def;
        }
        Map<String, String> group = INSTANCE.properties.computeIfAbsent(table, tb -> {
            RainbowGroup create = Rainbow.getGroupAndOpenCallBack(tb, INSTANCE.listener);
            return getGroupData(create);
        });
        return group.getOrDefault(key, def);
    }

    private static Map<String, String> getGroupData(RainbowGroup group) {
        if (group == null || group.getGroupType() != GroupType.KV) {
            return Collections.emptyMap();
        }
        Map<String, KvGroup.KvDataValue> inputs = ((KvGroup) group).getData();
        Map<String, String> output = new HashMap<>(inputs.size());
        for (Map.Entry<String, KvGroup.KvDataValue> input : inputs.entrySet()) {
            String key = input.getKey();
            String value = input.getValue().getValue();
            output.put(key, value);
        }
        return output;
    }
}