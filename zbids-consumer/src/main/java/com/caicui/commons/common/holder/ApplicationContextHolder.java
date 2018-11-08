package com.caicui.commons.common.holder;


import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;

import java.io.Serializable;
import java.util.Map;

/**
 * Description: 保存全局变量工具类<br>
 * Co. Lit All Rights Reserved.
 *
 * @version 1.0 2014-4-29 下午1:49:22 by 于科为（yukewei@qingyanhui.com）创建
 */
public final class ApplicationContextHolder implements Serializable {
    private static final long serialVersionUID = -3834011279732775327L;
    private static final ApplicationContextHolder CONTEXT_HOLDER = new ApplicationContextHolder();
    private ApplicationContext context;

    private ApplicationContextHolder() {
        // context = new ClassPathXmlApplicationContext(new String[]{"applicationContext.xml", "applicationContext-*.xml"});
    }

    public ApplicationContext getContext() {
        return context;
    }

    public void setContext(ApplicationContext context) {
        this.context = context;
    }

    /**
     * Description: 获取Bean的对象实例<br>
     *
     * @param beanName
     * @return
     * @Version1.0 2014-4-29 下午1:47:15 by 于科为（yukewei@qingyanhui.com）创建
     */
    public static Object getBean(String beanName) {
        return ApplicationContextHolder.getInstance().getContext().getBean(beanName);
    }

    /**
     * Description:获取单例对象 <br>
     *
     * @return
     * @Version1.0 2014-4-29 下午1:46:38 by 于科为（yukewei@qingyanhui.com）创建
     */
    public static ApplicationContextHolder getInstance() {
        return CONTEXT_HOLDER;
    }

    /**
     * Description: 按类型获取Bean的对象实例<br/>
     *
     * @return
     * @Version1.0 14-4-29 下午3:55 by 于科为（yukewei@qingyanhui.com）创建
     */
    public static <T> T getType(Class<T> clazz) {
        Map<String, T> beansOfType = ApplicationContextHolder.getInstance().getContext().getBeansOfType(clazz);

        if ((beansOfType == null) || (beansOfType.size() == 0)) {
            throw new RuntimeException("获取对象失败, 没有找到该类型对象[" + clazz + "]");
        }

        if (beansOfType.values().size() > 1) {
            throw new RuntimeException("获取对象失败, 存在多个实现[" + clazz + "]");
        }

        return beansOfType.values().iterator().next();
    }


    public static BeanFactory getBeanFactory() {
        final AutowireCapableBeanFactory beanFactory = ApplicationContextHolder.getInstance().getContext().getAutowireCapableBeanFactory();
        return beanFactory;
    }
}


