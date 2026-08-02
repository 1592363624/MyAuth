package cn.daenx.myauth.base.interceptor;

import cn.daenx.myauth.base.annotation.BanValidated;
import cn.daenx.myauth.main.entity.Ban;
import cn.daenx.myauth.main.entity.Soft;
import cn.daenx.myauth.main.entity.User;
import cn.daenx.myauth.util.CheckUtils;
import cn.daenx.myauth.util.IpUtil;
import cn.daenx.myauth.util.MyUtils;
import cn.daenx.myauth.util.RedisUtil;
import com.alibaba.fastjson.JSONObject;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.method.HandlerMethod;

/**
 * 封禁检查拦截器（第3个执行）
 * 职责：处理 @BanValidated，依次检查 IP、设备码、用户账号是否被封禁
 *
 * @author DaenMax
 */
@Slf4j
public class BanCheckInterceptor extends BaseInterceptor {

    @Resource
    private RedisUtil redisUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        BanValidated banValidated = handlerMethod.getMethodAnnotation(BanValidated.class);
        if (banValidated == null) {
            return true;
        }

        JSONObject jsonObject = getJson(request);
        String ip = IpUtil.getIpAddr(request);

        String skey = jsonObject.getString("skey");
        Soft soft = (Soft) redisUtil.get("soft:" + skey);
        if (CheckUtils.isObjectEmpty(soft)) {
            return reject(response, "skey错误");
        }

        // ===== IP 封禁检查 =====
        if (banValidated.is_ip()) {
            Ban ban = (Ban) redisUtil.get("ban:" + ip + "-" + 2 + "-" + 0);
            if (CheckUtils.isObjectEmpty(ban)) {
                ban = (Ban) redisUtil.get("ban:" + ip + "-" + 2 + "-" + soft.getId());
            }
            if (!CheckUtils.isObjectEmpty(ban)) {
                String result = checkBanTime(ban, "ip", ip);
                if (result != null) {
                    return reject(response, 300, result);
                }
            }
        }

        // ===== 设备码封禁检查 =====
        if (banValidated.is_device_code()) {
            String deviceCode = jsonObject.getJSONObject("data").getString("device_code");
            String deviceInfo = jsonObject.getJSONObject("data").getString("device_info");
            if (CheckUtils.isObjectEmpty(deviceCode)) {
                return reject(response, "缺少data.device_code参数");
            }
            if (CheckUtils.isObjectEmpty(deviceInfo)) {
                return reject(response, "缺少data.device_info参数");
            }
            Ban ban = (Ban) redisUtil.get("ban:" + deviceCode + "-" + 1 + "-" + 0);
            if (CheckUtils.isObjectEmpty(ban)) {
                ban = (Ban) redisUtil.get("ban:" + deviceCode + "-" + 1 + "-" + soft.getId());
            }
            if (!CheckUtils.isObjectEmpty(ban)) {
                String result = checkBanTime(ban, "device_code", deviceCode);
                if (result != null) {
                    return reject(response, 300, result);
                }
            }
        }

        // ===== 用户账号封禁检查 =====
        if (banValidated.is_user()) {
            User user = (User) request.getAttribute("obj_user");
            if (!CheckUtils.isObjectEmpty(user)) {
                Ban ban = (Ban) redisUtil.get("ban:" + user.getUser() + "-" + 3 + "-" + 0);
                if (CheckUtils.isObjectEmpty(ban)) {
                    ban = (Ban) redisUtil.get("ban:" + user.getUser() + "-" + 3 + "-" + soft.getId());
                }
                if (!CheckUtils.isObjectEmpty(ban)) {
                    String result = checkBanTime(ban, "user", user.getUser());
                    if (result != null) {
                        return reject(response, 300, result);
                    }
                }
            }
        }

        return true;
    }

    /**
     * 检查封禁是否仍在有效期内
     *
     * @return 若仍在封禁中，返回封装好的错误消息字符串；若封禁已过期，返回 null
     */
    private String checkBanTime(Ban ban, String type, String value) {
        if (ban.getToTime() == -1) {
            return "msg=被封禁" + "&type=" + type + "&value=" + value + "&toTime=-1&time=" + ban.getAddTime()
                    + "&why=" + ban.getWhy();
        }
        Integer seconds = ban.getToTime() - Integer.parseInt(MyUtils.getTimeStamp());
        if (seconds > 0) {
            return "msg=被封禁" + "&type=" + type + "&value=" + value + "&toTime=" + ban.getToTime() + "&time=" + ban.getAddTime()
                    + "&why=" + ban.getWhy();
        }
        return null;
    }
}
