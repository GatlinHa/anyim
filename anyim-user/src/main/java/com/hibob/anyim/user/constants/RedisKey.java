package com.hibob.anyim.user.constants;

public class RedisKey {

    private RedisKey() {

    }

    /**
     * 存储注销的用户的黑名单
     */
    public static final String USER_DEREGISTER = com.hibob.anyim.common.constants.RedisKey.USER_PREFIX + "deregister:";

    /**
     * 存储正在使用的用户的token
     */
    public static final String USER_ACTIVE_TOKEN = com.hibob.anyim.common.constants.RedisKey.USER_PREFIX + "activeToken:";


}
