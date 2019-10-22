package com.xkcoding.mpwechatdemo.enums;

import com.xkcoding.magic.core.tool.util.StrUtil;

/**
 * <p>
 * 事件类型
 * </p>
 *
 * @author yangkai.shen
 * @date Created in 2019/10/22 16:37
 */
public enum EventType {
    /**
     * 取消关注
     */
    UNSUBSCRIBE,
    /**
     * 关注
     */
    SUBSCRIBE,
    /**
     * 扫码
     */
    SCAN,
    /**
     * 其他事件(text/image...)
     */
    OTHER;

    public static EventType build(String eventType) {

        for (EventType value : EventType.values()) {
            if (StrUtil.equalsIgnoreCase(eventType, value.name())) {
                return value;
            }
        }
        return OTHER;
    }
}
