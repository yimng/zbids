package com.caicui.app.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.caicui.app.dmo.App;
import com.caicui.app.dmo.AppChecktokenLog;
import com.caicui.app.dmo.AppGettokenLog;
import com.caicui.app.service.EduAppService;
import com.caicui.commons.api.controller.ApiCommonController;
import com.caicui.commons.common.utils.RequestUtils;
import com.caicui.redis.cache.redis.service.RedisRawService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@Controller
public class AppEDUZBIDSController extends ApiCommonController {

    @Reference
    private EduAppService appService;
    @Autowired
    private RedisRawService redisRawService;


    /**
     * 获取token
     *
     * @param request
     * @param appType
     * @return
     */
    @RequestMapping(value = "/api/edu/zbids/app/gettoken")
    @ResponseBody
    public Map<String, Object> getToken(HttpServletRequest request,
                                        String appType) {
        String appId = request.getParameter("appId");
        String appKey = request.getParameter("appKey");
        if (StringUtils.isEmpty(appType) || StringUtils.isEmpty(appId)
                || StringUtils.isEmpty(appKey)) {
            return error("1000");
        }
        AppGettokenLog log = new AppGettokenLog();
        log.setReqAppId(appId);
        log.setReqAppKey(appKey);
        log.setReqAppType(appType);
        log.setReqIp(RequestUtils.getRemoteAddr(request));

        App a = new App();
        a.setAppId(appId);
        a.setAppKey(appKey);
        a.setAppType(appType);
        List<App> list = appService.selectApp(a);
        if (!list.isEmpty()) {
            App _app = list.get(0);
            String token = UUID.randomUUID().toString();
            redisRawService.set(token, _app.getId(), Long.valueOf(10 * 60));

            log.setAppId(_app.getId());
            log.setState("1");
            log.setToken(token);
            appService.insertAppGettokenLog(log);

            Map<String, String> map = new HashMap<String, String>();
            map.put("token", token);
            return success(map);
        }

        log.setState("0");
        appService.insertAppGettokenLog(log);

        return error("1000");

    }


    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/api/edu/zbids/app/checktoken")
    @ResponseBody
    public Map<String, Object> checkToken(HttpServletRequest request,
                                          String token) {
        String guestIP = request.getParameter("guestIP");
        if (StringUtils.isEmpty(token)) {
            return error("1000");
        }
        byte[] o = redisRawService.get(token);
        if (o == null) {
            return error("token已过期");
        }
        String appid = new String(o);
        redisRawService.set(token, appid, Long.valueOf(10 * 60));

        AppChecktokenLog log = new AppChecktokenLog();
        log.setReqIp(guestIP);
        log.setToken(token);

        App app = appService.selectAppById(appid.toString());
        //认证是否启动
        if (app != null && app.getState().equals("1")) {
            //安全级别判断
            if (!app.getAppLevl().isEmpty() && Integer.valueOf(app.getAppLevl()) > 3) {
                if (StringUtils.isEmpty(guestIP)) {
                    log.setState("0");
                    log.setInfo("ip为空");
                    appService.insertAppChecktokenLog(log);
                    return error("1000");
                }
                if (app.getWhiteIpList().indexOf(guestIP) != -1) {
                    log.setState("1");
                    log.setInfo("认证成功");
                    appService.insertAppChecktokenLog(log);

                    Map<String, String> map = new HashMap<String, String>();
                    map.put("APPID", app.getAppId());
                    return success(map);
                } else {
                    log.setState("0");
                    log.setInfo("ip不在白名单");
                    appService.insertAppChecktokenLog(log);
                    return error("1010");
                }

            } else {
                log.setState("1");
                log.setInfo("认证成功");
                appService.insertAppChecktokenLog(log);

                Map<String, String> map = new HashMap<String, String>();
                map.put("APPID", app.getAppId());
                return success(map);
            }
        } else {
            log.setState("0");
            log.setInfo("认证未启动");
            appService.insertAppChecktokenLog(log);
            return error("1010");
        }
    }

}
