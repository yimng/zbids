package com.caicui.area.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.caicui.app.service.AreaService;
import com.caicui.commons.api.controller.ApiCommonController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;


@Controller
public class AreaController extends ApiCommonController {

    @Reference
    private AreaService areaService;

    @RequestMapping(value = "/api/edu/zbids/area/getlist", method = RequestMethod.GET)
    @ResponseBody
    public List<Map<String, Object>> selectAreaByParentId(HttpServletRequest request) {
        String areaid = request.getParameter("areaid");
        return areaService.selectAreaByParentId(areaid);
    }
}