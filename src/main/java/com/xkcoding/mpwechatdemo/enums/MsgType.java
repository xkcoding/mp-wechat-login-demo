package com.xkcoding.mpwechatdemo.enums;

import com.xkcoding.magic.core.tool.util.StrUtil;

/**
 * <p>
 * 消息类型
 * </p>
 *
 * @author yangkai.shen
 * @date Created in 2019/10/22 16:29
 */
public enum MsgType {
    /**
     * 绑定回调地址消息
     */
    BINDING,
    /**
     * 事件消息
     */
    EVENT,
    /**
     * 其他消息类型
     */
    OTHER;

    public static MsgType build(String msgType) {
        if (StrUtil.isBlank(msgType)) {
            return BINDING;
        }
        for (MsgType value : MsgType.values()) {
            if (StrUtil.equalsIgnoreCase(msgType, value.name())) {
                return value;
            }
        }
        return OTHER;
    }
}
