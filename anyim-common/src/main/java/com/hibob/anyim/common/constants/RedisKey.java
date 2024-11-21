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
     * Netty服务的前缀
     */
    public static final String CHAT_PREFIX = COMMON_PREFIX + "chat:";

    /**
     * 存储注销的用户的黑名单
     */
    public static final String USER_DEREGISTER = USER_PREFIX + "deregister:";

    /**
     * 存储正在使用的用户的accessToken
     */
    public static final String USER_ACTIVE_TOKEN = USER_PREFIX + "activeAccessToken:";

    /**
     * 存储正在使用的用户的refreshToken
     */
    public static final String USER_ACTIVE_TOKEN_REFRESH = USER_PREFIX + "activeRefreshToken:";

    /**
     * 请求记录，后面接用户token+traceId
     */
    public static final String USER_REQ_RECORD = USER_PREFIX + "reqRecord:";

    /**
     * Netty在活全局路由表的Key，后面接uniqueId，value是netty的实例名
     */
    public static final String NETTY_GLOBAL_ROUTE = NETTY_PREFIX + "route:";

    /**
     * Netty在线客户端，后面接账号，value是uniqueId的集合
     */
    public static final String NETTY_ONLINE_CLIENT = NETTY_PREFIX + "online:";

    /**
     * Netty保存的参考MSG ID，后面接账号a@账号b
     */
    public static final String NETTY_REF_MSG_ID = NETTY_PREFIX + "refMsgId:";

    /**
     * 保存的SessionId下都有哪些MSG ID, 后面接sessionId
     */
    public static final String CHAT_SESSION_MSG_ID = CHAT_PREFIX + "sessionId:";

    /**
     * 保存的message消息, 后面接sessionId:msgId
     */
    public static final String CHAT_SESSION_MSG = CHAT_PREFIX + "msg:";

}
