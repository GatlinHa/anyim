package com.hibob.anyim.netty.rpc.processor;

import com.hibob.anyim.common.protobuf.MsgType;
import com.hibob.anyim.netty.utils.SpringContextUtil;

public class SystemMsgProcessorFactory {
    public static SystemMsgProcessor getProcessor(MsgType msgType) {
        switch (msgType) {
            case SYS_GROUP_CREATE:
                return SpringContextUtil.getBean(GroupCreateProcessor.class);
            case SYS_GROUP_ADD_MEMBER:
                return SpringContextUtil.getBean(GroupAddMemberProcessor.class);
            case SYS_GROUP_DEL_MEMBER:
                return SpringContextUtil.getBean(GroupDelMemberProcessor.class);
            case SYS_GROUP_SET_MANAGER:
            case SYS_GROUP_CANCEL_MANAGER:
                return SpringContextUtil.getBean(GroupChangeRoleProcessor.class);
            default:
                return null;
        }
    }
}
