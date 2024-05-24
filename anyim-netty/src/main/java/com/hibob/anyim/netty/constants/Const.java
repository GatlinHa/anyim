package com.hibob.anyim.netty.constants;

public class Const {
    private Const() {
    }

    public static final int MAGIC = 0x12345678; // TODO 先随便起个

    public static final String KEY_AUTHORIZATION = "token";

    public static final String KEY_UNIQUE_ID = "uniqueId";

    public static final int CHANNEL_EXPIRE = 60; // 默认1800 TODO 这里先写小方便测试


}
