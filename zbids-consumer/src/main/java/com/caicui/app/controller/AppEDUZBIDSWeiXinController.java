package com.caicui.app.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.caicui.app.dmo.App;
import com.caicui.app.dmo.AppLoginLog;
import com.caicui.app.entity.Student;
import com.caicui.app.entity.User;
import com.caicui.app.entity.UserLogin;
import com.caicui.app.entity.UserThird;
import com.caicui.app.service.*;
import com.caicui.commons.api.controller.ApiCommonController;
import com.caicui.commons.common.utils.RequestUtils;
import com.caicui.commons.utils.CommonUtil;
import com.caicui.commons.utils.HttpClientUtil;
import com.caicui.commons.utils.JmsQueueSender;
import com.caicui.redis.cache.redis.service.RedisRawService;
import com.caicui.redis.cache.redis.service.RedisService;
import com.edu.commons.utils.JsonResult;
import com.edu.dubbo.student.service.StudentEduService;
import com.edu.student.dmo.StudentRegisterReqDMO;
import com.edu.student.dmo.StudentRegisterRspDMO;
import net.sf.json.JSONObject;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

@Controller
public class AppEDUZBIDSWeiXinController extends ApiCommonController {

    private static Map<String, String> appmap = new HashMap<String, String>();
    static {
        appmap.put("wxc3b4f68473fb0481", "a0d9a71a1ccad893c89a306e5e868551");
        appmap.put("wx82164c5417074b21", "d3cdb8be8a2cb49692ebfb92e4155954");
        appmap.put("wxeec928e90d7d6cb6", "0d64fcd9bae8f096f9bec04841a01982");
        appmap.put("wx6f3cdeca7dee5cc7", "0b55483f9ab36a467b5a79c3bfdd095d");
    }

    @Autowired
    private RedisRawService redisRawService;
    @Autowired
    private RedisService redisService;
    @Reference
    private UserService userService;
    @Reference
    private UserTypeService userTypeService;
    @Reference
    private UserThirdService userThirdService;
    @Reference
    private StudentService studentService;
    @Reference
    private UserLoginService userLoginService;
    @Reference
    private EduAppService appService;
    @Reference
    private EduAppLoginLogService appLoginLogService;
    @Reference
    StudentEduService studentEduService;
    @Autowired
    private JmsQueueSender messageSender;


    /**
     * 获取微信二维码
     *
     * @param request
     * @param response
     * @param path
     * @param width
     */
    @RequestMapping(value = "/api/edu/zbids/weixin/getwxaqrcode", method = RequestMethod.GET)
    @ResponseBody
    public void getwxaqrcode(HttpServletRequest request, HttpServletResponse response, String path, String width, String appId) {
        if (StringUtils.isEmpty(path) || StringUtils.isEmpty(width)) {
            return;
        }
        if (!appmap.containsKey(appId)){
            return;
        }
        String access_token = "";
        String token = "accesstoken" + appId;
        Object object = redisService.get(token);
        if (object == null) {
            String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=" + appId + "&secret=" + appmap.get(appId);
            //获取微信id
            String jsonString = HttpClientUtil.sendGetRequest(url, "UTF-8");
            if (!StringUtils.isEmpty(jsonString)) {
                JSONObject jsonObject = JSONObject.fromObject(jsonString);
                String access_ = jsonObject.getString("access_token");
                if (!StringUtils.isEmpty(access_)) {
                    setex(token, 3600, access_);
                    access_token = access_;
                }
            }
        } else {
            access_token = object.toString();
        }

        String url = "https://api.weixin.qq.com/cgi-bin/wxaapp/createwxaqrcode?access_token=" + access_token;
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(url);
        InputStream instream = null;
        BufferedInputStream bis = null;
        OutputStream output = null;
        BufferedOutputStream bos = null;
        try {
            // 接收参数json列表
            JSONObject jsonParam = new JSONObject();
            jsonParam.put("path", path.replaceAll("&amp;", "&"));
            jsonParam.put("width", width);

            StringEntity entity = new StringEntity(jsonParam.toString(), "utf-8");
            entity.setContentEncoding("UTF-8");
            entity.setContentType("application/json");
            httpPost.setEntity(entity);
            HttpResponse res = httpClient.execute(httpPost);
            HttpEntity entityResponse = res.getEntity();
            if (entityResponse != null) {
                instream = entityResponse.getContent();
                bis = new BufferedInputStream(instream);//输入缓冲流

                output = response.getOutputStream();//得到输出流
                bos = new BufferedOutputStream(output);//输出缓冲流
                byte data[] = new byte[4096];//缓冲字节数
                int size = 0;
                size = bis.read(data);
                while (size != -1) {
                    bos.write(data, 0, size);
                    size = bis.read(data);
                }
                httpPost.clone();
                instream.close();
                bis.close();
                bos.flush();//清空输出缓冲流
                output.close();
                bos.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            httpClient.getConnectionManager().shutdown();

        }

    }

    /**
     * 获取微信accessToken
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/api/edu/zbids/weixin/getAccessToken", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> getAccessToken(HttpServletRequest request, String appId) {
        String access_token = "";
        String token = "accesstoken" + appId;
        Object object = redisService.get(token);
        if (!appmap.containsKey(appId)) {
            return error("appId 无效");
        }
        if (object == null) {
            String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=" + appId + "&secret=" + appmap.get(appId);
            //获取微信id
            String jsonString = HttpClientUtil.sendGetRequest(url, "UTF-8");
            if (!StringUtils.isEmpty(jsonString)) {
                JSONObject jsonObject = JSONObject.fromObject(jsonString);
                access_token = jsonObject.getString("access_token");
                if (!StringUtils.isEmpty(access_token)) {
                    setex(token, 3600, access_token);
                } else {
                    String errcode = jsonObject.getString("errcode");
                    return error(errcode);
                }
            }
        } else {
            access_token = object.toString();
        }

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("access_token", access_token);
        // 成功返回
        return success(data);
    }

    /**
     * 获取微信OpenId
     *
     * @param request
     * @param code
     * @return
     */
    @RequestMapping(value = "/api/edu/zbids/weixin/getOpenId", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> getOpenId(HttpServletRequest request, String code, String appId) {
        String openid = "";

        if (StringUtils.isEmpty(code)) {
            return error("授权码为空");
        }

        openid = getWeiXinOpenid(code, appId);

        if (StringUtils.isEmpty(openid)) {
            return error("微信授权失败");
        }

        Map<String, Object> date = new HashMap<>();
        date.put("openid", openid);
        // 成功返回
        return success(date);
    }

    /**
     * 微信绑定账号
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/api/edu/zbids/weixin/mobileBindOpneId", method = RequestMethod.POST)
    @ResponseBody
    @Transactional
    public Map<String, Object> oauthBinding(HttpServletRequest request,
                                            String token, String msgCode, String mobile, String code, String appId) {
        String openid;
        if (StringUtils.isEmpty(token)) {
            return error("token已过期");
        }
        Object object = redisService.get(token);
        if (object == null) {
            return error("token已过期");
        }

        boolean b = getCheckCode(token, "smscode", mobile, msgCode);
        if (!b) {
            return error("手机验证码错误！");
        }

        if (StringUtils.isEmpty(code)) {
            return error("授权码为空");
        }

        openid = getWeiXinOpenid(code, appId);

        if (StringUtils.isEmpty(openid)) {
            return error("微信授权失败");
        }

        UserThird third = userThirdService.getUserThirdBySocietyId(1, openid);
        if (third != null) {
            // 此账号已绑定
            return error("账号已绑定");
        }

        User user = userService.getUserByPhone(mobile);

        if (user != null && user.getState() == 0) {
            return error("账号没有启用");
        }
        if (user == null) {
            String regActiveId = request.getParameter("regactiveid");
            StudentRegisterReqDMO studentRegisterReqDMO = new StudentRegisterReqDMO();
            studentRegisterReqDMO.setMobile(mobile);
            studentRegisterReqDMO.setPassword(DigestUtils.md5Hex(mobile.substring(mobile.length() - 6)));
            if (StringUtils.isNotEmpty(regActiveId)) {
                studentRegisterReqDMO.setRegActiveId(regActiveId);
                studentRegisterReqDMO.setRegActiveType(1);
            }
            studentRegisterReqDMO.setIp(RequestUtils.getRemoteAddr(request));
            JsonResult<StudentRegisterRspDMO> studentByApp = studentEduService.createStudentByApp(studentRegisterReqDMO);
            if (!studentByApp.isSuccess()) {
                return error(studentByApp.getMsg());
            }
            user = userService.getUserByPhone(mobile);
        }

        UserThird userThird = new UserThird();
        userThird.setThirdType(1);
        userThird.setThirdUserid(openid);
        userThird.setUserId(user.getUserId());
        userThirdService.insertUserThird(userThird);

        UserLogin userLogin = userLoginService.getUserLoginByUserId(user.getUserId());
        if (userLogin != null) {
            if (userLogin.getIsAccountLocked()) {
                return error("账号被禁用");
            }
        }

        App app = appService.selectAppById(object.toString());
        if (app != null) {
            AppLoginLog loginLog = new AppLoginLog();
            loginLog.setMemberId(user.getUserId());
            loginLog.setIp(RequestUtils.getRemoteAddr(request));
            loginLog.setTokenId(token);
            loginLog.setLoginTime(new Date());
            loginLog.setClientType(app.getAppType());
            appLoginLogService.insertAppLoginLog(loginLog);
        }
        setex(token, 6 * 60 * 60, user.getUserId());

        Student student = studentService.selectStudentById(user.getUserId());
        Map<String, Object> date = new HashMap<>();
        date.put("nickName", user.getLoginName());
        date.put("mobile", user.getMobile());
        date.put("memberId", user.getUserId());
        date.put("avatar", student==null?"":student.getAvatar());
        date.put("token", token);
        date.put("isAvatar", student==null?"":student.isAvatarUploaded());
        // 成功返回
        return success(date);

    }

    /**
     * 微信解绑定账号
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/api/edu/zbids/weixin/mobileUnBindOpneId", method = RequestMethod.POST)
    @ResponseBody
    @Transactional
    public Map<String, Object> oauthUnBinding(HttpServletRequest request,
                                            String token, String mobile) {
        String openid;
        if (StringUtils.isEmpty(token)) {
            return error("token为空");
        }
        Object object = redisService.get(token);
        if (object == null) {
            return error("token已过期");
        }

        User user = userService.getUserByPhone(mobile);

        if (user == null) {
            return error("用户不存在");
        }

        int i = userThirdService.deleteUserThird(user.getUserId());
        if (i != 1) {
            return error("解绑失败");
        }

        App app = appService.selectAppById(object.toString());
        if (app != null) {
            AppLoginLog loginLog = new AppLoginLog();
            loginLog.setMemberId(user.getUserId());
            loginLog.setIp(RequestUtils.getRemoteAddr(request));
            loginLog.setTokenId(token);
            loginLog.setLoginTime(new Date());
            loginLog.setClientType(app.getAppType());
            appLoginLogService.insertAppLoginLog(loginLog);
        }
        setex(token, 6 * 60 * 60, user.getUserId());

        Student student = studentService.selectStudentById(user.getUserId());
        Map<String, Object> date = new HashMap<>();
        date.put("nickName", user.getLoginName());
        date.put("mobile", user.getMobile());
        date.put("memberId", user.getUserId());
        date.put("avatar", student==null?"":student.getAvatar());
        date.put("token", token);
        date.put("isAvatar", student==null?"":student.isAvatarUploaded());
        // 成功返回
        return success(date);

    }

    /**
     * 微信授权登录网站
     *
     * @param request
     * @param code
     * @return
     */
    @RequestMapping(value = "/api/edu/zbids/weixin/login", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> weixinlogin(HttpServletRequest request, String code, String appId) {
        String openid = "";
        if (StringUtils.isEmpty(code)) {
            return error("授权码为空");
        }

        openid = getWeiXinOpenid(code, appId);
        if (StringUtils.isEmpty(openid)) {
            return error("微信授权失败");
        }

        UserThird userThird = userThirdService.getUserThirdBySocietyId(1, openid);
        if (userThird == null) {
            // 此账号没还没绑定财萃账号
            return error("1001");
        }

        User user = userService.getUserById(userThird.getUserId());
        UserLogin userLogin = userLoginService.getUserLoginByUserId(userThird.getUserId());

        if (user.getState() == 0) {
            return error("账号没有启用");
        }
        if (userLogin != null) {
            if (userLogin.getIsAccountLocked()) {
                return error("账号被禁用");
            }
        }
	/*	loginMember.setLoginIp(RequestUtils.getRemoteAddr(request));
		loginMember.setLoginDate(new Date());
		loginMember.setLoginFailureCount(0);
		loginMember.setWeixin(openid);userLogin
		appMemberService.updateMember(loginMember);*/
        String token = UUID.randomUUID().toString();
        setex(token, 6 * 60 * 60, user.getUserId());

        Student student = studentService.selectStudentById(user.getUserId());
        Map<String, Object> date = new HashMap<>();
        date.put("nickName", user.getLoginName());
        date.put("memberId", user.getUserId());
        date.put("avatar", student==null?"":student.getAvatar());
        date.put("token", token);
        date.put("mobile", user.getMobile());
        date.put("isAvatar", student==null?"":student.isAvatarUploaded());
        // 成功返回
        return success(date);
    }


    /**
     * 获取微信Openid
     *
     * @param code
     * @return
     */
    private String getWeiXinOpenid(String code, String appId) {
        String openid = "";
        if (!appmap.containsKey(appId)) {
            throw new RuntimeException("appId 无效");
        }
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + appId
                + "&secret=" + appmap.get(appId) + "&js_code=" + code + "&grant_type=authorization_code";
        //获取微信id
        String jsonString = HttpClientUtil.sendGetRequest(url, "UTF-8");
        if (!StringUtils.isEmpty(jsonString)) {
            JSONObject jsonObject = JSONObject.fromObject(jsonString);
            if (jsonObject.containsKey("openid")) {
                openid = jsonObject.getString("openid");
            }
        }
        return openid;
    }


    /**
     * 设置token，并根据传值清楚已有token
     *
     * @param key
     * @param seconds
     * @param value
     */
    private void setex(String key, int seconds, String value) {
        //获取所有key
        Set<String> keySet = redisService.keys("*");
        if (keySet != null && !keySet.isEmpty()) {
            for (String ks : keySet) {
                byte[] o = redisRawService.get(key);
                if (o != null) {
                    String kvalue = new String(o);
                    //比较值是否有相同的
                    if (!StringUtils.isEmpty(kvalue) && kvalue.equals(value)) {
                        redisService.delete(ks);
                    }
                }
            }
        }
        redisRawService.set(key, value, Long.valueOf(seconds));
    }

    /**
     * 通过token获取验证码
     *
     * @param token
     * @param type
     * @param client
     * @param code
     * @return
     */
    private Boolean getCheckCode(String token, String type, String client, String code) {
        String oldCode = "";

        if (!StringUtils.isEmpty(token) && !StringUtils.isEmpty(type)) {
            String key = type;
            if (!StringUtils.isEmpty(client)) {
                key += "." + client;
            }
            key += "." + token;
            byte[] object = redisRawService.get(key);
            if (object != null) {
                oldCode = new String(object);
            }
            if (!StringUtils.isEmpty(code) && oldCode.toLowerCase().equals(code.toLowerCase())) {
                redisService.delete(key);
                return true;
            }
        }

        return false;
    }

    /**
     * 随机生成用户昵称
     *
     * @return
     */
    private String getNickname() {
        String nickName = "app" + CommonUtil.getRandomString(4).toLowerCase();
        User app = userService.getUserByNickname(nickName);
        if (app != null) {
            return getNickname();
        }
//		if (appMemberService.isNameKeep(nickName)) {
//			// 用户名不可用
//			return getNickname();
//		}
        return nickName;
    }
}
