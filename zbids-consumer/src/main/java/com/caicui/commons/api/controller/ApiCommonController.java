package com.caicui.commons.api.controller;

import com.caicui.commons.token.TokenService;
import com.caicui.commons.utils.StringEscapeEditor;
import com.edu.commons.constants.CommonsApiErrorCodeConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * com.caicui.commons.api.controller
 * Created by yukewi on 2016/4/11 18:14.
 */
public class ApiCommonController {
    protected HttpServletRequest request;
    protected HttpServletResponse response;
    @Autowired
    protected TokenService tokenService;

    @ModelAttribute
    public void setReqAndRes(HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
    }

    // 时间类型转换
    @InitBinder
    protected void initBinder(HttpServletRequest request,
                              HttpServletResponse response, ServletRequestDataBinder binder)
            throws Exception {
        binder.registerCustomEditor(Date.class, new CustomDateEditor(
                new SimpleDateFormat("yyyy-MM-dd"), true));
        binder.registerCustomEditor(String.class, new StringEscapeEditor(false,
                false, false, true));

        response.setHeader("Access-Control-Allow-Origin", "*");
    }

    /**
     * 失败后返回
     *
     * @param msg
     * @return
     */
    protected Map<String, Object> error(String msg) {
        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> date = new HashMap<String, Object>();
        map.put("state", "error");
        map.put("msg", msg);
        map.put("data", date);
        return map;
    }

    /**
     * 获取用户的ID
     *
     * @param token
     * @return
     */
    protected String getMemberId(String token) {
        return tokenService.getMemberId(token);
    }

    /**
     * 初始化Response
     */
    protected HttpServletResponse initResponse(HttpServletResponse response) {
        response.setContentType("text/plain;charset=UTF-8");
        response.setDateHeader("Expires", 1L);
        response.addHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache, no-store, max-age=0");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Headers", "x-requested-with,content-type");
        return response;
    }


    /**
     * 成功后返回
     *
     * @return
     */
    protected Map<String, Object> success(Object data) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("state", "success");
        map.put("data", data);
        map.put("msg", "");
        return map;
    }

    public static <T> T setNullValue(T source) throws Exception {
        Class<?> clazz = source.getClass();
        for (; clazz != Object.class; clazz = clazz.getSuperclass()) {
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                if (field.getGenericType().toString().equals(
                        "class java.lang.String")) {
                    field.setAccessible(true);
                    Object obj = field.get(source);
                    if (obj != null && obj.equals("")) {
                        field.set(source, null);
                    }
//                        else if (obj != null) {
//                            String str = obj.toString();
//                            str = StringEscapeUtils.escapeSql(str);//StringEscapeUtils是commons-lang中的通用类
//                            field.set(source, str.replace("\\", "\\" + "\\").replace("(", "\\(").replace(")", "\\)")
//                                    .replace("%", "\\%").replace("*", "\\*").replace("[", "\\[").replace("]", "\\]")
//                                    .replace("|", "\\|").replace(".", "\\.").replace("$", "\\$").replace("+", "\\+").trim()
//                            );
//                        }
                }
            }
        }
        return source;
    }
}
