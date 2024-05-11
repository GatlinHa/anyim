package com.hibob.anyim.common.constants;

public class RedisKey {

    private RedisKey() {

    }

    /**
     * 统一前缀
     */
    public static final String COMMON_PREFIX = "anyim:";

    /**
     * User服务的前缀
     */
    public static final String USER_PREFIX = COMMON_PREFIX + "user:";

    /**
     * Netty服务的前缀
     */
    public static final String NETTY_PREFIX = COMMON_PREFIX + "netty:";


}
