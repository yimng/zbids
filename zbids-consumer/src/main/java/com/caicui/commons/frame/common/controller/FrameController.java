package com.caicui.commons.frame.common.controller;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/frame")
public class FrameController {

    @RequestMapping(value = "/index")
    public String index(HttpSession session) {
        if (session.getAttribute("SESSION_USER") != null) {
            return "frame/index";
        }
        return "redirect:/login/login";
    }

    @RequestMapping(value = "/frameHeader")
    public String frameHeader() {
        return "frame/frame_header";
    }

    @RequestMapping(value = "/frameMenu")
    public String frameMenu() {
        return "frame/frame_menu";
    }

    @RequestMapping(value = "/frameMiddle")
    public String frameMiddle() {
        return "frame/frame_middle";
    }

    @RequestMapping(value = "/frameMain")
    public String frameMain(HttpServletRequest request) {
        request.setAttribute("javaVersion", System.getProperty("java.version"));
        request.setAttribute("osName", System.getProperty("os.name"));
        request.setAttribute("osArch", System.getProperty("os.arch"));
        request.setAttribute("osVersion", System.getProperty("os.version"));
        request.setAttribute("serverInfo", StringUtils.substring(request.getSession().getServletContext().getServerInfo(), 0, 30));
        request.setAttribute("servletVersion", request.getSession().getServletContext().getMajorVersion() + "." + request.getSession().getServletContext().getMinorVersion());
        request.setAttribute("version", "System V1.0");
        request.setAttribute("email", "278839530@qq.com");
        return "frame/frame_main";
    }


}
