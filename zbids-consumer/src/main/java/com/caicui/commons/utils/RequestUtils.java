package com.caicui.commons.utils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * com.bitzh.util.request
 * Created by yukewi on 2015/9/14 17:06.
 */
public class RequestUtils {

    /**
     * 获取请求的真实ip地址
     *
     * @param request
     * @return
     * @Description: 获取请求的真实ip地址
     */
    public static String getIpAddr(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        /**
         * 如果通过了多级反向代理的话，X-Forwarded-For的值并不止一个，而是一串IP值
         * X-Forwarded-For：192.168.1.110, 192.168.1.120, 192.168.1.130, 192.168.1.100
         * 用户真实IP为： 192.168.1.110
         */
        if (ip.contains(",")) {
            ip = ip.split(",")[0];
        }
        return ip;
    }

    /**
     * 获取用户会话的ID
     *
     * @param request
     * @return
     */
    public static String getSessionId(HttpServletRequest request) {
        if (request == null || request.getSession() == null) {
            return null;
        }
        HttpSession session = request.getSession();
        return session.getId();
    }
}
