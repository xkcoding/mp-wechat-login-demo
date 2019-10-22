package com.xkcoding.mpwechatdemo.autoconfiguration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * <p>
 * 自动装配类
 * </p>
 *
 * @author yangkai.shen
 * @date Created in 2019/10/22 13:56
 */
@Configuration
@EnableConfigurationProperties(MpWeChatProperties.class)
public class MpWeChatAutoConfiguration {
}
