package com.xkcoding.mpwechatdemo.controller;

import cn.hutool.core.date.DateTime;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.google.common.collect.Maps;
import com.xkcoding.magic.core.tool.api.R;
import com.xkcoding.magic.core.tool.enums.CommonResultCode;
import com.xkcoding.magic.core.tool.exception.ServiceException;
import com.xkcoding.magic.core.tool.util.StrUtil;
import com.xkcoding.mpwechatdemo.autoconfiguration.MpWeChatProperties;
import com.xkcoding.mpwechatdemo.enums.EventType;
import com.xkcoding.mpwechatdemo.enums.MsgType;
import com.xkcoding.mpwechatdemo.utils.XmlUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * <p>
 * 测试
 * </p>
 *
 * @author yangkai.shen
 * @date Created in 2019/10/22 13:57
 */
@Slf4j
@Controller
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DemoController {
    private static final String ACCESS_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s";

    private static final String TICKET_URL = "https://api.weixin.qq.com/cgi-bin/qrcode/create?access_token=%s";

    private static final String QR_CODE_URL = "https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=%s";

    private static final String USER_INFO_URL = "https://api.weixin.qq.com/cgi-bin/user/info?access_token=%s&openid=%s&lang=zh_CN";

    private final MpWeChatProperties properties;

    /**
     * 生成登录二维码
     */
    @GetMapping("/qrcode")
    public String qrCode() {
        // 获取 token，参考文档 https://developers.weixin.qq.com/doc/offiaccount/Basic_Information/Get_access_token.html
        String accessTokenUrl = String.format(ACCESS_TOKEN_URL, properties.getAppId(), properties.getAppSecret());
        String tokenJson = HttpUtil.get(accessTokenUrl);
        JSONObject token = JSONUtil.parseObj(tokenJson);
        log.info("【token】= {}", token);

        if (token.containsKey("errcode")) {
            throw new ServiceException(CommonResultCode.INTERNAL_SERVER_ERROR, token);
        }

        // 获取 ticket，参考文档 参考文档 https://developers.weixin.qq.com/doc/offiaccount/Account_Management/Generating_a_Parametric_QR_Code.html
        String ticketUrl = String.format(TICKET_URL, token.getStr("access_token"));
        String ticketJson = HttpUtil.post(ticketUrl, "{\"action_name\": \"QR_LIMIT_STR_SCENE\", \"action_info\": {\"scene\": {\"scene_str\": \"test\"}}}");
        JSONObject ticket = JSONUtil.parseObj(ticketJson);
        log.info("【ticket】= {}", ticket);

        if (!ticket.containsKey("ticket")) {
            throw new ServiceException("无 ticket 信息");
        }

        // 获取二维码图片信息，参考文档 https://developers.weixin.qq.com/doc/offiaccount/Account_Management/Generating_a_Parametric_QR_Code.html
        String qrCodeUrl = String.format(QR_CODE_URL, ticket.getStr("ticket"));
        return "redirect:" + qrCodeUrl;
    }

    /**
     * 回调处理
     */
    @RequestMapping("/oauth/mp/callback")
    @ResponseBody
    public Object callback(HttpServletRequest request, String echostr) {
        String type = "";
        Map<String, String> map = Maps.newHashMap();
        try {
            // 读取输入流
            SAXReader reader = new SAXReader();
            Document document = reader.read(request.getInputStream());
            // 得到xml根元素
            Element root = document.getRootElement();

            XmlUtil.parserXml(root, map);
            log.info("【map】= {}", map);

            String msgType = map.get("MsgType");
            if (StrUtil.isNotBlank(msgType)) {
                type = msgType;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        MsgType msgType = MsgType.build(type);

        switch (msgType) {
            case EVENT:
                return processEvent(map);
            case BINDING:
                // 配置回调接口时，微信官方校验时，执行此逻辑
                // 测试直接返回，生产环境需要根据 token 计算，比对结果，一切正确之后，返回 echostr
                // 参考文档：https://developers.weixin.qq.com/doc/offiaccount/Getting_Started/Getting_Started_Guide.html
                return echostr;
            case OTHER:
            default:
                return R.success();
        }
    }

    private Object processEvent(Map<String, String> map) {
        String event = map.get("Event");
        EventType eventType = EventType.build(event);
        switch (eventType) {
            case SCAN:
                log.info("微信号：{} 在 {} 扫码登录", map.get("FromUserName"), new DateTime(Long.parseLong(map.get("CreateTime") + "000")));
                getUserInfo(map.get("FromUserName"));
                break;
            case SUBSCRIBE:
                log.info("微信号：{} 在 {} 关注，与自己系统用户绑定关系", map.get("FromUserName"), new DateTime(Long.parseLong(map.get("CreateTime") + "000")));
                getUserInfo(map.get("FromUserName"));
                break;
            case UNSUBSCRIBE:
                log.info("微信号：{} 在 {} 取关，需要解绑用户关系", map.get("FromUserName"), new DateTime(Long.parseLong(map.get("CreateTime") + "000")));
                getUserInfo(map.get("FromUserName"));
                break;
            case OTHER:
            default:
                log.info("【map】= {}", map);
                break;
        }

        return R.success();
    }

    /**
     * 获取用户信息，参考文档 https://developers.weixin.qq.com/doc/offiaccount/User_Management/Get_users_basic_information_UnionID.html#UinonId
     */
    private void getUserInfo(String openId) {
        // 获取 token
        String accessTokenUrl = String.format(ACCESS_TOKEN_URL, properties.getAppId(), properties.getAppSecret());
        String tokenJson = HttpUtil.get(accessTokenUrl);
        JSONObject token = JSONUtil.parseObj(tokenJson);

        // 获取 userInfo
        String userInfoUrl = String.format(USER_INFO_URL, token.getStr("access_token"), openId);
        String userInfoJson = HttpUtil.get(userInfoUrl);
        JSONObject userInfo = JSONUtil.parseObj(userInfoJson);
        log.info("【userInfo】= {}", userInfo);
    }

}
