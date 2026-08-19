package cn.daenx.myauth.base.interceptor;

import cn.daenx.myauth.base.annotation.DataDecrypt;
import cn.daenx.myauth.base.annotation.SignValidated;
import cn.daenx.myauth.base.annotation.SoftValidated;
import cn.daenx.myauth.base.annotation.UserLogin;
import cn.daenx.myauth.base.annotation.VersionValidated;
import cn.daenx.myauth.main.entity.Soft;
import cn.daenx.myauth.main.entity.User;
import cn.daenx.myauth.main.entity.Version;
import cn.daenx.myauth.main.enums.SoftEnums;
import cn.daenx.myauth.main.enums.VersionEnums;
import cn.daenx.myauth.util.AESUtils;
import cn.daenx.myauth.util.CheckUtils;
import cn.daenx.myauth.util.MyUtils;
import cn.daenx.myauth.util.RedisUtil;
import com.alibaba.fastjson.JSONObject;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.method.HandlerMethod;

/**
 * API 合法性验证拦截器（第2个执行）
 * 职责：
 * 1. @SoftValidated：验证软件 skey 是否合法、状态是否正常
 * 2. @VersionValidated：验证版本 vkey 是否合法、与软件匹配、状态正常
 * 3. @DataDecrypt：对加密请求体进行 AES 解密
 * 4. @UserLogin：验证用户 token 是否有效
 * 5. @SignValidated：验证请求签名 sign 是否正确、时间戳是否在允许范围内
 *
 * @author DaenMax
 */
@Slf4j
public class ApiValidateInterceptor extends BaseInterceptor {

    @Resource
    private RedisUtil redisUtil;

    @Value("${genKey}")
    private String genKey;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        JSONObject jsonObject = getJson(request);

        // ===== @SoftValidated：软件合法性验证 =====
        if (handlerMethod.getMethodAnnotation(SoftValidated.class) != null) {
            String skey = jsonObject.getString("skey");
            if (CheckUtils.isObjectEmpty(skey)) {
                return reject(response, "缺少skey参数");
            }
            Soft soft = (Soft) redisUtil.get("soft:" + skey);
            if (CheckUtils.isObjectEmpty(soft)) {
                return reject(response, "skey错误");
            }
            if (SoftEnums.STATUS_DISABLE.getCode().equals(soft.getStatus())) {
                return reject(response, "软件已停用");
            }
            if (SoftEnums.STATUS_FIX.getCode().equals(soft.getStatus())) {
                return reject(response, "软件维护中");
            }
            request.setAttribute("obj_soft", soft);
        }

        // ===== @VersionValidated：版本合法性验证 =====
        if (handlerMethod.getMethodAnnotation(VersionValidated.class) != null) {
            Soft soft = (Soft) request.getAttribute("obj_soft");
            String vkey = jsonObject.getString("vkey");
            if (CheckUtils.isObjectEmpty(vkey)) {
                return reject(response, "缺少vkey参数");
            }
            Version version = (Version) redisUtil.get("version:" + vkey);
            if (CheckUtils.isObjectEmpty(version)) {
                return reject(response, "vkey错误");
            }
            if (!version.getFromSoftId().equals(soft.getId())) {
                return reject(response, "vkey与skey不匹配");
            }
            if (VersionEnums.STATUS_DISABLE.getCode().equals(version.getStatus())) {
                return reject(response, "版本已停用");
            }
            request.setAttribute("obj_version", version);
        }

        // ===== @DataDecrypt：请求数据 AES 解密 =====
        if (handlerMethod.getMethodAnnotation(DataDecrypt.class) != null) {
            String reqStr = MyUtils.getRequestPostStr(request);
            if (CheckUtils.isObjectEmpty(reqStr)) {
                return reject(response, "获取请求数据失败");
            }
            String skey = jsonObject.getString("skey");
            Soft soft = (Soft) redisUtil.get("soft:" + skey);
            if (CheckUtils.isObjectEmpty(soft)) {
                return reject(response, "skey错误");
            }
            request.setAttribute("obj_soft", soft);
            if (soft.getGenStatus() == 1) {
                String jsonStr = AESUtils.decrypt(jsonObject.getString("data"), soft.getGenKey());
                if (CheckUtils.isObjectEmpty(jsonStr)) {
                    return reject(response, "数据解密失败");
                }
                Object object = JSONObject.parseObject(jsonStr);
                jsonObject.put("data", object);
            }
            request.setAttribute("json", jsonObject);
        }

        // ===== @UserLogin：用户登录状态验证 =====
        if (handlerMethod.getMethodAnnotation(UserLogin.class) != null) {
            Soft soft = (Soft) request.getAttribute("obj_soft");
            String token = jsonObject.getJSONObject("data").getString("token");
            if (CheckUtils.isObjectEmpty(token)) {
                return reject(response, "非法请求");
            }
            JSONObject tokenJson = MyUtils.decUserToken(token, genKey);
            if (CheckUtils.isObjectEmpty(tokenJson)) {
                return reject(response, "非法请求");
            }
            User user = (User) redisUtil.get("user:" + soft.getId() + ":" + tokenJson.getString("user") + ":" + token);
            if (CheckUtils.isObjectEmpty(user)) {
                return reject(response, "用户未登录");
            }
            //仅在开启顶号登录且限制为单设备在线时，校验是否被顶号
            if (soft.getMaxOnlineCount() != null && soft.getMaxOnlineCount().equals(1)
                    && soft.getKickLogin() != null && soft.getKickLogin().equals(1)) {
                if (!user.getToken().equals(token)) {
                    return reject(response, "账号可能异地登录，请重新登录");
                }
            }
            request.setAttribute("obj_user", user);
        }

        // ===== @SignValidated：请求签名验证 =====
        if (handlerMethod.getMethodAnnotation(SignValidated.class) != null) {
            String sign = jsonObject.getString("sign");
            if (CheckUtils.isObjectEmpty(sign)) {
                return reject(response, "缺少sign参数");
            }
            Integer timestamp = jsonObject.getJSONObject("data").getInteger("timestamp");
            if (CheckUtils.isObjectEmpty(timestamp)) {
                return reject(response, "缺少data.timestamp参数");
            }
            String skey = jsonObject.getString("skey");
            Soft soft = (Soft) redisUtil.get("soft:" + skey);
            if (CheckUtils.isObjectEmpty(soft)) {
                return reject(response, "skey错误");
            }
            Integer diffTime = Math.abs(timestamp - Integer.parseInt(MyUtils.getTimeStamp()));
            if (diffTime > soft.getSignTime()) {
                return reject(response, "sign已过期");
            }
            request.setAttribute("obj_soft", soft);
            String privateSign = MyUtils.calculateSign(jsonObject.getJSONObject("data"), soft.getGenKey());
            if (!privateSign.equals(sign)) {
                return reject(response, "sign错误");
            }
        }

        return true;
    }
}
