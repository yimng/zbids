package com.caicui.commons.common.listener;


import com.caicui.commons.common.holder.ApplicationContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContextEvent;


/**
 * Description:初始化WebApplicationContext的对象的监听器<br>
 * http://www.qingyanhui.com Co. Lit All Rights Reserved.
 *
 * @version 1.0 2014-4-29 下午1:52:19 by 于科为（yukewei@qingyanhui.com）创建
 */
public class ContextLoaderListener extends org.springframework.web.context.ContextLoaderListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(ContextLoaderListener.class);
    private static final String START_CONFIG_FILE = "startInitialization";

    @Override
    public void contextInitialized(ServletContextEvent contextEvent) {
        super.contextInitialized(contextEvent);
        try {
            WebApplicationContext webApplicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(contextEvent.getServletContext());
            ApplicationContextHolder.getInstance().setContext(webApplicationContext);
            LOGGER.info("加载Spring 配置成功");
            LOGGER.info("执行初始化配置成功");
        } catch (Throwable e) {
            LOGGER.error("加载Spring 配置失败: " + e.getMessage());
        }
    }
}
