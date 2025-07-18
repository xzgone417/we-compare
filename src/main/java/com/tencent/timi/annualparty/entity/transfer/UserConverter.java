package com.tencent.timi.annualparty.entity.transfer;

import com.tencent.timi.annualparty.entity.pojo.UserBo;
import com.tencent.timi.annualparty.entity.pojo.UserDo;

/**
 * @author hhshan
 * @date 2023/12/20
 */
public class UserConverter {
    public static UserBo toBo(UserDo userDo) {
        UserBo.UserBoBuilder builder = UserBo.builder();
        builder.id(userDo.getId())
                .name(userDo.getName())
                .build();
        return builder.build();
    }

    public static UserDo toDo(UserBo userBo) {
        UserDo.UserDoBuilder builder = UserDo.builder();
        builder.id(userBo.getId())
                .name(userBo.getName())
                .build();
        return builder.build();
    }
}
