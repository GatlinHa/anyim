package com.hibob.anyim.common.utils;

public class CommonUtil {

    private CommonUtil() {

    }

    public static String conUniqueId(String account, String clientId) {
        return account + "|" + clientId;
    }

    /**
     * 获取本机IP
     */
    public static String getLocalIp() {
        // 如果是windwos系统

        return "";
    }

}
