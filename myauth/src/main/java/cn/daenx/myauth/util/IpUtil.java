package cn.daenx.myauth.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 获取客户端IP
 *
 * @author DaenMax
 */
@Slf4j
public class IpUtil {

    public static String getIpAddr(HttpServletRequest request) {
        String ip = null;
        try {
            ip = request.getHeader("x-forwarded-for");
            if (isBlankIp(ip)) ip = request.getHeader("Proxy-Client-IP");
            if (isBlankIp(ip)) ip = request.getHeader("WL-Proxy-Client-IP");
            if (isBlankIp(ip)) ip = request.getHeader("HTTP_CLIENT_IP");
            if (isBlankIp(ip)) ip = request.getHeader("HTTP_X_FORWARDED_FOR");
            if (isBlankIp(ip)) ip = request.getRemoteAddr();
            // 多个ip获取第一个
            if (!isBlankIp(ip) && ip.length() > 15) ip = ip.split(",")[0];
        } catch (Exception e) {
            log.error("获取客户端IP失败", e);
        }
        if ("0:0:0:0:0:0:0:1".equals(ip)) {
            try {
                ip = InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException e) {
                log.error("获取本机IP失败", e);
            }
        }
        return ip;
    }

    private static boolean isBlankIp(String ip) {
        return ip == null || ip.trim().isEmpty() || "unknown".equalsIgnoreCase(ip);
    }
}
