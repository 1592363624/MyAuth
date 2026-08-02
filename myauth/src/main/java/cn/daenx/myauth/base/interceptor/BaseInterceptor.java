package cn.daenx.myauth.base.interceptor;

import cn.daenx.myauth.base.vo.Result;
import cn.daenx.myauth.util.CheckUtils;
import cn.daenx.myauth.util.MyUtils;
import com.alibaba.fastjson.JSONObject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

/**
 * 拦截器基类，提供公共工具方法
 *
 * @author DaenMax
 */
@Slf4j
public abstract class BaseInterceptor implements HandlerInterceptor {

    /**
     * 读取请求体中的 JSON 对象（由 AdminAuthInterceptor 解析后存入 request）
     */
    protected JSONObject getJson(HttpServletRequest request) {
        Object obj = request.getAttribute("json");
        return obj == null ? new JSONObject(true) : (JSONObject) obj;
    }

    /**
     * 向客户端写出错误响应并记录日志，返回 false 以中断请求
     */
    protected boolean reject(HttpServletResponse response, int code, String msg) {
        log.info("响应->" + msg);
        response.setHeader("Content-Type", "application/json; charset=utf-8");
        try {
            response.getWriter().write(Result.error(code, msg).toJsonString());
        } catch (IOException e) {
            log.error("写出响应失败", e);
        }
        return false;
    }

    /**
     * 向客户端写出错误响应（默认 500 状态码）
     */
    protected boolean reject(HttpServletResponse response, String msg) {
        return reject(response, 500, msg);
    }
}
