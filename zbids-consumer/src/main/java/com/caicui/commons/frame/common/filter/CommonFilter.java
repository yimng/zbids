package com.caicui.commons.frame.common.filter;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Description: 用户过滤器 对没有登录 特殊登录等做处理 All Rights Reserved.
 *
 * @version 1.0 2014年4月25日 下午2:27:33 by 魏明勋（weimingxun@qingyanhui.com）创建
 */
public class CommonFilter implements Filter {
    /**
     * 排除特定的URL地址
     */
    private static Set<String> excludeUrls = new HashSet<String>();

    private static String reLoginUrl = "login/login";


    static {
        excludeUrls.add("/**/login/*");
        excludeUrls.add("/**/*.jsp");
        excludeUrls.add("/**/frame/*");
        excludeUrls.add("/**/*.css");
        excludeUrls.add("/**/*.js");
    }

    public void destroy() {

    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        boolean result = false;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;

        String url = httpServletRequest.getRequestURI();
        String requestType = httpServletRequest.getHeader("X-Requested-With");
        if (!StringUtils.isEmpty(url)) {
            if (isIgnorableUrl(url) || loginCorrect(httpServletRequest)) {
                result = true;
            }
        }
        if (result) {
            filterChain.doFilter(request, response);
        } else {
            if (!StringUtils.isEmpty(requestType) && requestType.equalsIgnoreCase("XMLHttpRequest")) {
                httpServletResponse.setHeader("sessionstatus", "timeout");
                return;
            } else {
                String responseUrl = getRedirectedUrl(httpServletRequest);
                httpServletResponse.sendRedirect(responseUrl);
            }

        }
    }

    public void init(FilterConfig arg0) throws ServletException {
    }

    /**
     * Description: 检查请求路径是否忽视登录验证
     *
     * @param url
     * @return
     * @Version1.0 2014年4月25日 下午4:11:53 by 魏明勋（weimingxun@qingyanhui.com）创建
     */
    private Boolean isIgnorableUrl(String url) {
        PathMatcher pathMatcher = new AntPathMatcher();


        // 不完整路径uri方式路径匹配
        // String requestPath="/app/pub/login.do";//请求路径
        // String patternPath="/**/login.do";//路径匹配模式

        // 模糊路径方式匹配
        // String requestPath="/app/pub/login.do";//请求路径
        // String patternPath="/**/*.do";//路径匹配模式


        // 包含模糊单字符路径匹配
        //String requestPath = "/app/pub/login.do";// 请求路径
        //String patternPath = "/**/lo?in.do";// 路径匹配模式

        for (String fullPattern : excludeUrls) {
            if (pathMatcher.match(fullPattern, url)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Description:验证是否登录
     *
     * @param request
     * @return
     * @Version1.0 2014年4月25日 下午4:12:06 by 魏明勋（weimingxun@qingyanhui.com）创建
     */
    private Boolean loginCorrect(HttpServletRequest request) {
        Boolean result = false;
        String sessionUserName = (String) request.getSession().getAttribute("SESSION_USER");
        if (StringUtils.isNotEmpty(sessionUserName)) {
            result = true;
        }
        return result;
    }

    /**
     * Description: 从数据库读取 超时跳转登陆页面的各项参数 项目 端口 IP 如果为空则则使用默认值
     *
     * @return
     * @Version1.0 2014年4月29日 上午10:00:03 by 魏明勋（weimingxun@qingyanhui.com）创建
     */
    private String getRedirectedUrl(HttpServletRequest request) {
        String webRootPath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
        String result = webRootPath + "/" + reLoginUrl;
        return result;
    }
}
