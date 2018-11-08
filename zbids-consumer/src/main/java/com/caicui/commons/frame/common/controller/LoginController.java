package com.caicui.commons.frame.common.controller;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
@RequestMapping("/login")
public class LoginController {

    private static Map<String, String> userMap = new ConcurrentHashMap<String, String>();

    static {
        userMap.put("admin", "admin!@#");
    }

    @RequestMapping(value = "/login")
    public String login(HttpServletRequest request) {

        return "login/login";
    }

    @RequestMapping(value = "/check")
    public String check(HttpServletRequest request, HttpSession session) {
        String j_password = request.getParameter("j_password");
        String j_username = request.getParameter("j_username");
        String tip = "";

        if (StringUtils.isEmpty(j_password)) {
            tip = "密码不能为空!";
        } else if (StringUtils.isEmpty(j_username)) {
            tip = "用户名不能为空!";
        } else if (StringUtils.isNotEmpty(j_password) && StringUtils.isNotEmpty(j_username)) {
            tip = check(j_username, j_password);
        }

        if (StringUtils.isEmpty(tip)) {
            session.setAttribute("SESSION_USER", j_username);
            return "redirect:/frame/index";
        }

        request.setAttribute("tip", tip);
        return "login/login";
    }

    private String check(String j_username, String j_password) {
        String tip = "";
        boolean contains = userMap.containsKey(j_username);
        if (!contains) {
            tip = "用户不存在!";
        } else if (!StringUtils.equals(j_password, userMap.get(j_username))) {
            tip = "密码错误!";
        } else {
            tip = "";
        }
        return tip;
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String logout(HttpServletRequest request, HttpSession session) {
        session.invalidate();
        request.setAttribute("redirectUrl", "/login/login");
        return "common/success";
    }
}
