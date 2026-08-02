package cn.daenx.myauth.base.interceptor;

import cn.daenx.myauth.base.annotation.AdminLogin;
import cn.daenx.myauth.base.annotation.NoEncryptNoSign;
import cn.daenx.myauth.base.vo.Result;
import cn.daenx.myauth.main.entity.Admin;
import cn.daenx.myauth.main.entity.Role;
import cn.daenx.myauth.main.enums.AdminEnums;
import cn.daenx.myauth.main.enums.NoEncryptNoSignEnums;
import cn.daenx.myauth.main.mapper.AdminMapper;
import cn.daenx.myauth.util.CheckUtils;
import cn.daenx.myauth.util.MyUtils;
import cn.daenx.myauth.util.RedisUtil;
import com.alibaba.fastjson.JSONObject;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 * 请求解析 + 管理员鉴权拦截器（第1个执行）
 * 职责：
 * 1. 解析请求体 JSON，写入 request attribute "json"
 * 2. 判断 @NoEncryptNoSign，写入 request attribute "NoEncryptNoSign"
 * 3. 打印 "接收->" 日志
 * 4. 处理 @AdminLogin：验证管理员 token、状态、有效期、角色权限
 * 5. afterCompletion：打印 "响应->" 日志
 *
 * @author DaenMax
 */
@Slf4j
public class AdminAuthInterceptor extends BaseInterceptor {

    @Resource
    private AdminMapper adminMapper;
    @Resource
    private RedisUtil redisUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        response.setHeader("Content-Type", "application/json; charset=utf-8");

        // ===== 公共解析（所有请求均执行）=====
        String reqStr = MyUtils.getRequestPostStr(request);
        JSONObject jsonObject = new JSONObject(true);
        if (!CheckUtils.isObjectEmpty(reqStr)) {
            jsonObject = JSONObject.parseObject(reqStr);
        }
        request.setAttribute("json", jsonObject);

        // 判断是否标注了 @NoEncryptNoSign
        if (handlerMethod.getMethodAnnotation(NoEncryptNoSign.class) != null) {
            request.setAttribute("NoEncryptNoSign", NoEncryptNoSignEnums.YES.getCode());
        } else {
            request.setAttribute("NoEncryptNoSign", NoEncryptNoSignEnums.NO.getCode());
        }

        log.info("接收->" + jsonObject.toJSONString());

        // ===== @AdminLogin 管理员鉴权 =====
        AdminLogin adminLogin = handlerMethod.getMethodAnnotation(AdminLogin.class);
        if (adminLogin != null) {
            String token = request.getHeader("token");
            if (CheckUtils.isObjectEmpty(token)) {
                return reject(response, 401, "非法请求");
            }
            Admin admin = (Admin) redisUtil.get("admin:" + token);
            Admin adminA = adminMapper.selectById(admin);
            if (CheckUtils.isObjectEmpty(admin) || !admin.getToken().equals(adminA.getToken())) {
                redisUtil.del("admin:" + token);
                return reject(response, 402, "token无效，请重新登录");
            }
            if (admin.getStatus().equals(AdminEnums.STATUS_DISABLE.getCode())) {
                redisUtil.del("admin:" + token);
                return reject(response, 405, "账号已被禁用");
            }
            if (admin.getLastTime() + AdminEnums.TOKEN_VALIDITY.getCode() < Integer.parseInt(MyUtils.getTimeStamp())) {
                redisUtil.del("admin:" + token);
                return reject(response, 403, "登录状态失效，请重新登录");
            }
            // 检查是否要求超级角色
            if (adminLogin.is_super_role()) {
                Role role = (Role) redisUtil.get("role:" + admin.getRole());
                if (!role.getFromSoftId().equals("0")) {
                    return reject(response, 500, "你没有权限[1001]");
                }
            }
            // 检查是否要求 admin 账号
            if (adminLogin.is_admin()) {
                if (!"admin".equals(admin.getUser())) {
                    return reject(response, 500, "你没有权限[1002]");
                }
            }
            request.setAttribute("obj_admin", admin);
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        if (handler instanceof HandlerMethod) {
            if (!CheckUtils.isObjectEmpty(request.getSession())) {
                Result result = (Result) request.getSession().getAttribute("body");
                if (!CheckUtils.isObjectEmpty(result)) {
                    log.info("响应->" + result.toJsonString());
                    request.getSession().setAttribute("body", null);
                }
            }
        }
    }
}
