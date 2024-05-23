package com.hibob.anyim.common.utils;

public class CommonUtil {

    private CommonUtil() {

    }

    public static String conUniqueId(String account, String clientId) {
        return account + "|" + clientId;
    }

}
