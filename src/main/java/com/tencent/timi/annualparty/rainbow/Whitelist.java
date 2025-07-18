package com.tencent.timi.annualparty.rainbow;

import com.tencent.rainbow.base.callback.ListenCallback;
import com.tencent.rainbow.base.config.FileGroup;
import com.tencent.rainbow.base.config.RainbowGroup;
import com.tencent.rainbow.base.entity.RainbowChangeInfo;
import com.tencent.rainbow.base.enums.GroupType;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author haoyangwei
 */
public class Whitelist {
    private static final String FILENAME = "whitelist";
    private static final String NEWLINES = "\r?\n|\r";
    private static final Whitelist INSTANCE = new Whitelist();
    private final Listener listener = new Listener(this);
    private volatile Map<String, Set<String>> whitelist = null;

    private static class Listener implements ListenCallback<RainbowChangeInfo> {
        private final Whitelist instance;

        private Listener(Whitelist instance) {
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
            instance.whitelist.putAll(getGroupData(group));
        }
    }

    /**
     * 检查文本是否是出现在文件中
     *
     * @param file 文件名
     * @param text 文本
     * @return 是否存在
     */
    public static boolean contains(String file, String text) {
        if (StringUtils.isBlank(file) || StringUtils.isBlank(text)) {
            return false;
        }
        if (INSTANCE.whitelist == null) {
            synchronized (INSTANCE) {
                if (INSTANCE.whitelist == null) {
                    RainbowGroup create = Rainbow.getGroupAndOpenCallBack(FILENAME, INSTANCE.listener);
                    INSTANCE.whitelist = getGroupData(create);
                }
            }
        }
        return INSTANCE.whitelist.getOrDefault(file, Collections.emptySet()).contains(text);
    }

    private static Map<String, Set<String>> getGroupData(RainbowGroup group) {
        if (group == null || group.getGroupType() != GroupType.FILE) {
            return Collections.emptyMap();
        }
        FileGroup files = (FileGroup) group;
        Map<String, Set<String>> output = new ConcurrentHashMap<>(files.getData().size());
        for (FileGroup.FileData file : files.getData()) {
            String content = new String(file.getProperties(), StandardCharsets.UTF_8);
            Set<String> values = new HashSet<>();
            // 对每个名字进行trim
            String[] lines = content.split(NEWLINES);
            for (String line : lines) {
                values.add(line.trim());
            }
            output.put(file.getName(), values);
        }
        return output;
    }
}
