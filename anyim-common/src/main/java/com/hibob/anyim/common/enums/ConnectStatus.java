package com.hibob.anyim.common.enums;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum ConnectStatus {
    OFFLINE(0),
    LEAVE(1),
    ONLINE(2),
    BUSYING(3),
    ;

    /**
     * 状态值，数值越大，表示权重越大，当多设备同时登录时，最终状态是权重最大的
    */
    private final int value;

    public static ConnectStatus fromValue(int value) {
        for (ConnectStatus status : ConnectStatus.values()) {
            if (status.value == value) {
                return status;
            }
        }
        return null;
    }

    public int getValue() {
        return value;
    }
}
