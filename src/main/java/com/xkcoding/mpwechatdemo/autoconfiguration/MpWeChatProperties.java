package com.xkcoding.mpwechatdemo.autoconfiguration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * <p>
 * 属性注入
 * </p>
 *
 * @author yangkai.shen
 * @date Created in 2019/10/22 13:54
 */
@Data
@ConfigurationProperties(prefix = "wechat")
public class MpWeChatProperties {
    private String appId;
    private String appSecret;
    private String token;
}
