package com.tencent.timi.annualparty.util;

import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * @author haoyangwei
 * @date 2023/12/21
 */
public class RandomUtil {

    public static List<String> random(List<String> names, int number, long seed) {
        if (names == null || names.isEmpty()) {
            return Collections.emptyList();
        }
        Collections.sort(names);
        if (names.size() <= number) {
            return names;
        }
        Random random = new Random(seed);
        Collections.shuffle(names, random);
        List<String> result = names.subList(0, number);
        Collections.sort(result);
        return result;
    }
}