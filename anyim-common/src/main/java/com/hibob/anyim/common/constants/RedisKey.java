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

    /**
     * 存储注销的用户的黑名单
     */
    public static final String USER_DEREGISTER = USER_PREFIX + "deregister:";

    /**
     * 存储正在使用的用户的token
     */
    public static final String USER_ACTIVE_TOKEN = USER_PREFIX + "activeToken:";

    /**
     * Netty在活全局路由表的Key
     */
    public static final String NETTY_GLOBAL_ROUTE = NETTY_PREFIX + "route:";


}
