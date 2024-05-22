package com.hibob.anyim.netty.enums;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
public enum MsgType {
    HELLO(0, "First message after connecting netty server"),

    HEART_BEAT(1, "Heart beat"),

    CHAT(2, "Chat"),

    GROUP_CHAT(3, "Group chat"),

    CLOSE_BY_READ_IDLE(10, "Close channel because of read idle");

    private final int code;
    private final String desc;

    public int code() {
        return this.code;
    }

    public String desc() {
        return this.desc;
    }

}
