package com.caicui.app.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.caicui.app.dmo.App;
import com.caicui.app.dmo.AppLoginLog;
import com.caicui.app.entity.*;
import com.caicui.app.service.*;
import com.caicui.commons.api.controller.ApiCommonController;
import com.caicui.commons.common.utils.RequestUtils;
import com.caicui.commons.utils.*;
import com.caicui.redis.cache.redis.service.RedisRawService;
import com.caicui.redis.cache.redis.service.RedisService;
import com.edu.commons.utils.JsonResult;
import com.edu.dubbo.student.service.StudentEduService;
import com.edu.student.dmo.StudentCreateReqDMO;
import com.edu.student.dmo.StudentRegisterReqDMO;
import com.edu.student.dmo.StudentRegisterRspDMO;
import net.coobird.thumbnailator.Thumbnails;
import net.sf.json.JSONObject;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Controller
public class AppEDUZBIDSLoginController extends ApiCommonController {

    @Reference
    private EduAppService appService;
    @Reference
    private UserLoginService userLoginService;

    @Reference
    private UserService userService;

    @Reference
    private UserThirdService userThirdService;

    @Reference
    private UserTypeService userTypeService;

    @Reference
    private StudentService studentService;

    @Reference
    private StudentEduService studentEduService;

    @Reference
    private EduAppLoginLogService appLoginLogService;
    @Autowired
    private RedisRawService redisRawService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private JmsQueueSender messageSender;
//	@Autowired
//    private CourseActiveDubboService courseActiveDubboService;
//    @Autowired
//    private AreaService areaService;


    /***********************************************************************************
     **********************   用户信息接口            *********************************************
     ***********************************************************************************/
    @RequestMapping(value = "/api/edu/zbids/member/fastreg", method = RequestMethod.POST)
    @ResponseBody
    @CrossOrigin
    public Map<String, Object> fastreg(HttpServletRequest request, String token, String userName, String phone, String email, String password) {

        Object object = getUserId(token, 60 * 60);
        if (object == null) {
            return error("token已过期");
        }
        App app = appService.selectAppById(object.toString());
        if (app == null) {
            return error("token已过期");
        }
        User e;
        if (StringUtils.isNotEmpty(email)) {
            e = userService.getUserByEmail(email);
            if (e != null) {
                return error("邮箱已注册！");
            }
        }

        if (StringUtils.isNotEmpty(phone)) {
            e = userService.getUserByPhone(phone);
            if (e != null) {
                // 手机号已注册
                return error("手机号已注册");
            }
        }

        if (StringUtils.isNotEmpty(userName)) {
            e = userService.getUserByNickname(userName);
            if (e != null) {
                // 用户名已存在
                return error("用户名已存在");
            }
        }
        String regactiveid = request.getParameter("regactiveid");

        StudentRegisterReqDMO studentRegisterReqDMO = new StudentRegisterReqDMO();
        if (StringUtils.isNotEmpty(regactiveid)) {
            studentRegisterReqDMO.setRegActiveId(regactiveid);
        } studentRegisterReqDMO.setEmail(email);
        studentRegisterReqDMO.setMobile(phone);
        studentRegisterReqDMO.setLoginName(userName);
        studentRegisterReqDMO.setPassword(password);
        studentRegisterReqDMO.setIp(RequestUtils.getRemoteAddr(request));
        JsonResult<StudentRegisterRspDMO> studentByApp = studentEduService.createStudentByApp(studentRegisterReqDMO);

        if (!studentByApp.isSuccess()) {
            return error(studentByApp.getMsg());
        }

        User loginUser = userService.getUserByPhone(phone);
        UserType userType = userTypeService.getUserTypeByUserId(loginUser.getUserId());
        //添加同步数据代码
        JSONObject j = new JSONObject();
        j.put("businessSign", "addUser");
        j.put("recordId", loginUser.getUserId());
        j.put("recordOpId", "");
        j.put("dataSource", "webpc");
        j.put("businessInfo", "添加用户");
        messageSender.simpleSend(j.toString());

        setex(token, 6 * 60 * 60, loginUser.getUserId());
        Student student = studentService.selectStudentById(loginUser.getUserId());
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("nickName", loginUser.getLoginName());
        data.put("memberId", loginUser.getUserId());
        data.put("avatar", student.getAvatar());
        data.put("userLevel", userType.getUsertype());
        data.put("token", token);
        data.put("isAvatar", student.isAvatarUploaded());
        // 成功返回
        return success(data);
    }

    @RequestMapping(value = "/api/edu/zbids/member/emailreg", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> emailreg(HttpServletRequest request, String token,
                                        String code, String email, String password,
                                        String agreement, HttpServletResponse response) {
        String regactiveid = request.getParameter("regactiveid");
        if (StringUtils.isEmpty(email)
                || StringUtils.isEmpty(code) || StringUtils.isEmpty(password)
                || StringUtils.isEmpty(agreement)) {
            // 参数错误
            return error("参数错误！");
        }
        boolean b = getCheckCode(token, "loginmailcode", email, code);
        if (!b) {
            return error("邮箱验证错误！");
        }

        if (!StringUtils.isEmpty(agreement) && !"1".equals(agreement)) {
            // 同意《会员注册协议》方可注册
            return error("同意《会员注册协议》方可注册！");
        }

        User user = new User();

        User e = userService.getUserByEmail(email);
        if (e != null) {
            return error("邮箱已注册！");
        }

        StudentRegisterReqDMO studentRegisterReqDMO = new StudentRegisterReqDMO();
        studentRegisterReqDMO.setEmail(email);
        studentRegisterReqDMO.setPassword(DigestUtils.md5Hex(password));
        if (StringUtils.isNotEmpty(regactiveid) ) {
            studentRegisterReqDMO.setRegActiveId(regactiveid);
        }
        studentRegisterReqDMO.setIp(RequestUtils.getRemoteAddr(request));
        JsonResult<StudentRegisterRspDMO> studentByApp = studentEduService.createStudentByApp(studentRegisterReqDMO);

        if (!studentByApp.isSuccess()) {
            return error(studentByApp.getMsg());
        }

        User loginUser = userService.getUserByEmail(email);
        UserType userType = userTypeService.getUserTypeByUserId(loginUser.getUserId());
        //添加同步数据代码
        JSONObject j = new JSONObject();
        j.put("businessSign", "addUser");
        j.put("recordId", loginUser.getUserId());
        j.put("recordOpId", "");
        j.put("dataSource", "webpc");
        j.put("businessInfo", "添加用户");
        messageSender.simpleSend(j.toString());

        //String orderid = courseActiveDubboService.makeOrder(loginUser.getUserId(),12, "AF9G");
        //添加同步数据代码
//        JSONObject oj = new JSONObject();
//        oj.put("businessSign", "payFinishOrder");
//        oj.put("recordId", orderid);
//        oj.put("recordOpId", "");
//        oj.put("dataSource", "webpc");
//        oj.put("businessInfo", "订单完成");
//        messageSender.simpleSend(oj.toString());

        setex(token, 6 * 60 * 60, loginUser.getUserId());
        Student student = studentService.selectStudentById(loginUser.getUserId());
        Map<String, Object> date = new HashMap<String, Object>();
        date.put("nickName", loginUser.getLoginName());
        date.put("memberId", loginUser.getUserId());
        date.put("avatar", student.getAvatar());
        date.put("userLevel", userType.getUsertype());
        date.put("token", token);
        date.put("isAvatar", student.isAvatarUploaded());
        // 成功返回
        return success(date);
    }

    @RequestMapping(value = "/api/edu/zbids/member/mobilereg", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> mobilereg(HttpServletRequest request,
                                         String token, String code, String phone, String password,
                                         String agreement, String societyId, String societyType) throws UnsupportedEncodingException {

        String regactiveid = request.getParameter("regactiveid");
        Object object = getUserId(token, 60 * 60);
        if (object == null) {
            return error("token已过期");
        }
        App app = appService.selectAppById(object.toString());
        if (app == null) {
            return error("token已过期");
        }
        boolean b = getCheckCode(token, "loginsmscode", phone, code);
        if (!b) {
            return error("手机验证码错误！");
        }

        if (agreement == null) {
            // 同意《会员注册协议》方可注册
            return error("同意《会员注册协议》方可注册");
        }
        User _p = userService.getUserByPhone(phone);
        if (_p != null) {
            // 手机号已注册
            return error("手机号已注册");
        }

        StudentRegisterReqDMO studentRegisterReqDMO = new StudentRegisterReqDMO();
        studentRegisterReqDMO.setMobile(phone);
        studentRegisterReqDMO.setPassword(DigestUtils.md5Hex(password));
        if (StringUtils.isNotEmpty(regactiveid)) {
            studentRegisterReqDMO.setRegActiveId(regactiveid);
        }
        studentRegisterReqDMO.setIp(RequestUtils.getRemoteAddr(request));
        JsonResult<StudentRegisterRspDMO> studentByApp = studentEduService.createStudentByApp(studentRegisterReqDMO);

        if (!studentByApp.isSuccess()) {
            return error(studentByApp.getMsg());
        }
        if (!StringUtils.isEmpty(societyId)
                && !StringUtils.isEmpty(societyType)) {
            UserThird ut = userThirdService.getUserThirdBySocietyId(Integer.valueOf(societyType), societyId);
            if (ut != null) {
                // 此账号已绑定
                throw new RuntimeException("账号已绑定");
            }
            UserThird userThird = new UserThird();
            userThird.setThirdType(Integer.valueOf(societyType));
            userThird.setThirdUserid(societyId);
            userThird.setUserId(studentByApp.getData().getUserId());
            userThirdService.insertUserThird(userThird);
        }

        User loginMember = userService.getUserByPhone(phone);
        UserType userType = userTypeService.getUserTypeByUserId(loginMember.getUserId());
        //添加同步数据代码
        JSONObject j = new JSONObject();
        j.put("businessSign", "addUser");
        j.put("recordId", loginMember.getUserId());
        j.put("recordOpId", "");
        j.put("dataSource", "webpc");
        j.put("businessInfo", "添加用户");
        messageSender.simpleSend(j.toString());

        setex(token, 6 * 60 * 60, loginMember.getUserId());
        Student student = studentService.selectStudentById(loginMember.getUserId());
        Map<String, Object> date = new HashMap<String, Object>();
        date.put("nickName", loginMember.getLoginName());
        date.put("memberId", loginMember.getUserId());
        date.put("avatar", student.getAvatar());
        date.put("userLevel", userType.getUsertype());
        date.put("token", token);
        date.put("isAvatar", student.isAvatarUploaded());
        // 成功返回
        return success(date);
    }

    @RequestMapping(value = "/api/edu/zbids/member/mobilelogin", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> codelogin(HttpServletRequest request, String token,
                                         String mobile, String msgcode) {
        if (StringUtils.isEmpty(token)) {
            return error("token为空");
        }
        Object object = redisService.get(token);
        if (object == null) {
            return error("token已过期");
        }
        if (StringUtils.isEmpty(mobile) || StringUtils.isEmpty(msgcode)) {
            return error("手机号或验证码为空");
        }
        boolean b = getCheckCode(token, "smscode", mobile, msgcode);
        if (!b) {
            return error("手机验证码错误！");
        }

        //AppMember loginMember = appMemberService.getMemberByPhone(mobile);
        User user = userService.getUserByPhone(mobile);

        if (user == null) {
            //新建用户
            String regactiveid = request.getParameter("regactiveid");

            StudentRegisterReqDMO studentRegisterReqDMO = new StudentRegisterReqDMO();
            studentRegisterReqDMO.setMobile(mobile);
            studentRegisterReqDMO.setPassword(DigestUtils.md5Hex(mobile.substring(mobile.length() - 6)));
            studentRegisterReqDMO.setIp(RequestUtils.getRemoteAddr(request));
            if (StringUtils.isNotEmpty(regactiveid)) {
                studentRegisterReqDMO.setRegActiveId(regactiveid);
            }
            JsonResult<StudentRegisterRspDMO> studentByApp = studentEduService.createStudentByApp(studentRegisterReqDMO);
            if (!studentByApp.isSuccess()) {
                return error(studentByApp.getMsg());
            }

            //添加同步数据代码
            JSONObject j = new JSONObject();
            j.put("businessSign", "addUser");
            j.put("recordId", studentByApp.getData().getUserId());
            j.put("recordOpId", "");
            j.put("dataSource", "webpc");
            j.put("businessInfo", "添加用户");
            messageSender.simpleSend(j.toString());

            //String orderid = courseActiveDubboService.makeOrder(user.getUserId(),12, "AF9G");
            //添加同步数据代码
//            JSONObject oj = new JSONObject();
//            oj.put("businessSign", "payFinishOrder");
//            oj.put("recordId", orderid);
//            oj.put("recordOpId", "");
//            oj.put("dataSource", "webpc");
//            oj.put("businessInfo", "订单完成");
//            messageSender.simpleSend(oj.toString());
        }
        if (user.getState() == 0) {
            return error("账号没有启用");
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
        Map<String, Object> date = new HashMap<String, Object>();
        date.put("nickName", user.getLoginName());
        date.put("memberId", user.getUserId());
        date.put("avatar", student==null?"":student.getAvatar());
        date.put("userLevel", 5);
        date.put("token", token);
        date.put("isAvatar", student==null?"":student.isAvatarUploaded());
        // 成功返回
        return success(date);

    }

    @RequestMapping(value = "/api/edu/zbids/member/login", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> login(HttpServletRequest request, String token,
                                     String account, String password) {
        String code = request.getParameter("code");
        if (StringUtils.isEmpty(token)) {
            return error("token为空");
        }
        Object object = redisService.get(token);
        if (object == null) {
            return error("token已过期");
        }
        if (StringUtils.isEmpty(account) || StringUtils.isEmpty(password)) {
            return error("用户名或密码为空");
        }

        User user;
        if (ValidatorUtil.isEmail(account)) {
            user = userService.getUserByEmail(account);
        } else if (ValidatorUtil.isMobile(account)) {
            user = userService.getUserByPhone(account);
        } else {
            user = userService.getUserByNickname(StringEscapeUtils.unescapeHtml4(account));
        }

        if (user == null) {
            // 用户名或密码错误
            return error("用户名或密码错误");
        }
        if (user.getState() == 0) {
            return error("账号没有启用");
        }

        if (!StringUtils.isEmpty(code)) {
            boolean b = getCheckCode(token, "loginimgcode", null, code);
            if (!b) {
                return error("图片验证码错误！");
            }
        }
        //密码错误
        if (!user.getPassword().equals(DigestUtils.md5Hex(password))) {
            return error("用户名或密码错误");
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
        UserType userType = userTypeService.getUserTypeByUserId(user.getUserId());
        Student student = studentService.selectStudentById(user.getUserId());
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("nickName", user.getLoginName());
        data.put("memberId", user.getUserId());
        data.put("avatar", student == null ? "" : student.getAvatar());
        data.put("userLevel", userType == null ? "" : userType.getUsertype());
        data.put("token", token);
        data.put("isAvatar", student==null?"":student.isAvatarUploaded());
        // 成功返回
        return success(data);

    }
    @RequestMapping(value = "/api/edu/zbids/member/detail")
    @ResponseBody
    private  Map<String, Object> memberDetail(HttpServletRequest request,
                                                     String token, String code, String type) {
        if (StringUtils.isEmpty(token) || StringUtils.isEmpty(code) || StringUtils.isEmpty(type)) {
            // 参数错误
            return error("参数错误！");
        }

        Boolean b = checkToken(token,RequestUtils.getRemoteAddr(request));
        if (!b) {
            return error("认证失败");
        }
        User user = null;
        if(type.equals("1")){
            user = userService.getUserById(code);
        }else if(type.equals("2")){
            user = userService.getUserByPhone(code);
        }else if(type.equals("3")){
            user = userService.getUserByEmail(code);
        }else if(type.equals("4")){
            user = userService.getUserByNickname(code);
        }
        Map<String, Object> p = new HashMap<String, Object>();
        if (user != null) {
            Student student = studentService.selectStudentById(user.getUserId());
            UserType userType = userTypeService.getUserTypeByUserId(user.getUserId());
            p.put("id", user.getUserId());
            p.put("nickeName", user.getLoginName());
            p.put("email", user.getEmail());
            p.put("gender", user.getGender());
            p.put("birth", user.getBirth());
            p.put("address", user);
            //p.put("areaStore", member.getAreaStore());
            //p.put("zipCode", member.getZipCode());
            p.put("mobile", user.getMobile());
            p.put("idCard", user.getCertificateNo());
            //p.put("signature", member.getSignature());
            p.put("educationExperience", student == null ? "" : student.getEducationId());
            //p.put("IdentityType", member.getIdentity());
            p.put("school", student == null ? "" : student.getGraduateUniversityId());
            p.put("learnSchool", student == null ? "" : student.getStudyCampusId());
            p.put("learnSecialty", student == null ? "" : student.getStudySpecialty());
            p.put("learnGrade", student == null ? "" : student.getStudyGrade());
            p.put("emergencyContactName", student == null ? "" : student.getEmergencyContactName());
            p.put("emergencyContactPhone", student == null ? "" : student.getEmergencyContactPhone());
            p.put("weixin", student == null ? "" : student.getWeixin());
            p.put("weibo", student == null ? "" : student.getWeibo());
            p.put("qq", student == null ? "" : student.getQq());
            //p.put("accaName", student);
            //p.put("accaPassword", member.getAccaPassword());
            //p.put("cmaLoginName", member.getCmaLoginName());
            //p.put("cmaPassword", member.getCmaPassword());
            //p.put("cmaName", member.getCmaName());
            p.put("userLevel", userType.getUsertype());
        }
        return success(p);
    }

    @RequestMapping(value = "/api/edu/zbids/member/changePwdByPhone", method = RequestMethod.POST)
    @ResponseBody
    private Map<String, Object> changePwdByPhone(HttpServletRequest request,
                                                 String token, String mobile, String code, String password) {

        if (StringUtils.isEmpty(token) || StringUtils.isEmpty(mobile)
                || StringUtils.isEmpty(code) || StringUtils.isEmpty(password)) {
            // 参数错误
            return error("参数错误！");
        }

        boolean b = getCheckCode(token, "getpwssmscode", mobile, code);
        if (!b) {
            return error("手机验证码错误！");
        }
        User user = userService.getUserByPhone(mobile);
        if (user == null) {
            return error("手机号错误！");
        }

        user.setPassword(DigestUtils.md5Hex(password));
        userService.updateUser(user);

        //添加同步数据代码
        JSONObject j = new JSONObject();
        j.put("businessSign", "editUser");
        j.put("recordId", user.getUserId());
        j.put("recordOpId", "");
        j.put("dataSource", "webpc");
        j.put("businessInfo", "编辑用户");

        messageSender.simpleSend(j.toString());

        Map<String, Object> date = new HashMap<String, Object>();
        date.put("nickName", user.getLoginName());
        date.put("mobile", user.getMobile());
        date.put("password", password);
        return success(date);

    }

    @RequestMapping(value = "/api/edu/zbids/member/changePwdByEmail", method = RequestMethod.POST)
    @ResponseBody
    private Map<String, Object> changePwdByEmail(HttpServletRequest request, String token,
                                                 String email, String code, String password) {

        if (StringUtils.isEmpty(email)
                || StringUtils.isEmpty(code) || StringUtils.isEmpty(password)) {
            // 参数错误
            return error("参数错误！");
        }
        boolean b = getCheckCode(token, "getpwsmailcode", email, code);
        if (!b) {
            return error("邮箱验证码错误！");
        }

        User user = userService.getUserByEmail(email);
        if (user == null) {
            return error("邮箱地址错误！");
        }

        user.setPassword(DigestUtils.md5Hex(password));
        userService.updateUser(user);
        //添加同步数据代码
        JSONObject j = new JSONObject();
        j.put("businessSign", "editUser");
        j.put("recordId", user.getUserId());
        j.put("recordOpId", "");
        j.put("dataSource", "webpc");
        j.put("businessInfo", "编辑用户");

        messageSender.simpleSend(j.toString());

        Map<String, Object> date = new HashMap<String, Object>();
        date.put("nickName", user.getLoginName());
        return success(date);
    }

    @RequestMapping(value = "/api/edu/zbids/member/getmemberinfo")
    @ResponseBody
    private Map<String, Object> getmemberinfo(HttpServletRequest request, String token) {

        String userId = getUserId(token, 6 * 60 * 60);
        if (StringUtils.isEmpty(userId)) {
            return error("nologin");
        }

        User user = userService.getUserById(userId);
        Student student = studentService.selectStudentById(userId);
        UserType userType = userTypeService.getUserTypeByUserId(userId);
        Map<String, Object> jsonMap = new HashMap<String, Object>();
        if (user != null) {
            jsonMap.put("id", user.getUserId());
            jsonMap.put("nickName", user.getLoginName());
            jsonMap.put("email", user.getEmail());
            jsonMap.put("name", user.getRealName());
            jsonMap.put("gender", user.getGender());
            jsonMap.put("birth", user.getBirth());
            jsonMap.put("address", student.getAddress());
            //jsonMap.put("zipCode", student.get);
            jsonMap.put("mobile", user.getMobile());
            jsonMap.put("idCard", user.getCertificateNo());
            jsonMap.put("isAvatar", student.isAvatarUploaded());
            jsonMap.put("avatar", student.getAvatar());
            jsonMap.put("userLevel", userType.getUsertype());
        }

        return success(jsonMap);
    }

    @RequestMapping(value = "/api/edu/zbids/member/getLoginLog")
    @ResponseBody
    public Object getLoginLog(HttpServletRequest request, String memberid, Integer pageNo,
                              Integer pageSize) {

        if (StringUtils.isEmpty(memberid)) {
            return error("参数错误");
        }
        if (pageSize == null || pageSize < 0) {
            return error("参数错误");
        }
        if (pageNo == null || pageNo < 0) {
            return error("参数错误");
        }

        Map<String, Object> mesMap = appLoginLogService.selectAppLoginLogByPage(memberid, pageNo, pageSize);
        mesMap.put("state", "success");
        mesMap.put("msg", "");
        return mesMap;
    }

    @RequestMapping(value = "/api/edu/zbids/member/editinfo", method = RequestMethod.POST)
    @ResponseBody
    public Object ibRegMember(HttpServletRequest request, String token, HttpServletResponse response) throws ParseException {
        String memberId = getUserId(token, 6 * 60 * 60);
        if (StringUtils.isEmpty(memberId)) {
            return error("nologin");
        }
        User user = userService.getUserById(memberId);
        if (user == null) {
            return error("用户不存在!");
        }
        //String id = request.getParameter("id");
        String nickName = request.getParameter("nickName");
        String email = request.getParameter("email");
        String mobile = request.getParameter("mobile");
        //String password = request.getParameter("password");
        String name = request.getParameter("name");
        String gender = request.getParameter("gender");
        String birth = request.getParameter("birth");
        String address = request.getParameter("address");
        String areaPath = request.getParameter("areaPath");
        String zipCode = request.getParameter("zipCode");
        String phone = request.getParameter("phone");
        String idCard = request.getParameter("idCard");
        String signature = request.getParameter("signature");
        String educationExperience = request.getParameter("educationExperience");
        String identity = request.getParameter("identityType");
        String school = request.getParameter("school");
        String learnSchool = request.getParameter("learnSchool");
        String learnSecialty = request.getParameter("learnSecialty");
        String learnGrade = request.getParameter("learnGrade");
        String emergencyContactName = request.getParameter("emergencyContactName");
        String emergencyContactPhone = request.getParameter("emergencyContactPhone");
        String weixin = request.getParameter("weixin");
        String weibo = request.getParameter("weibo");
        String qq = request.getParameter("qq");
        String accaName = request.getParameter("accaName");
        String accaPassword = request.getParameter("accaPassword");
        String cmaLoginName = request.getParameter("cmaLoginName");
        String cmaPassword = request.getParameter("cmaPassword");
        String cmaName = request.getParameter("cmaName");

        Student student = studentService.selectStudentById(user.getUserId());
        UserType userType = userTypeService.getUserTypeByUserId(user.getUserId());

        user.setUserId(memberId);

        if (!StringUtils.isEmpty(nickName)) {
//            if(appMemberService.isNameKeep(nickName)){
//                return error("该用户名已存在!");
//            }
            if (StringUtils.isEmpty(user.getLoginName())) {
                User _n = userService.getUserByNickname(nickName.trim());
                if (_n != null) {
                    return error("该用户名已存在!");
                }
            } else {
                if (!user.getLoginName().equals(nickName)) {
                    User _n = userService.getUserByNickname(nickName.trim());
                    if (_n != null) {
                        return error("该用户名已存在!");
                    }
                }
            }
            user.setLoginName(nickName.trim());
        }

        if (!StringUtils.isEmpty(email)) {
            if (StringUtils.isEmpty(user.getEmail())) {
                User _e = userService.getUserByEmail(email.trim());
                if (_e != null) {
                    return error("该电子邮箱已存在!");
                }
            } else {
                if (!user.getEmail().equals(email)) {
                    User _e = userService.getUserByEmail(email.trim());
                    if (_e != null) {
                        return error("该电子邮箱已存在!");
                    }
                }
            }
            user.setEmail(email.trim());
        }

        if (!StringUtils.isEmpty(mobile)) {
            if (StringUtils.isEmpty(user.getMobile())) {
                User _m = userService.getUserByPhone(mobile.trim());
                if (_m != null) {
                    return error("手机号已存在!");
                }
            } else {
                if (!user.getMobile().equals(mobile)) {
                    User _m = userService.getUserByPhone(mobile.trim());
                    if (_m != null) {
                        return error("手机号已存在!");
                    }
                }
            }
            user.setMobile(mobile.trim());
        }

        if (!StringUtils.isEmpty(name)) {
            user.setRealName(name);
        }

        if (!StringUtils.isEmpty(gender)) {
            user.setGender(Integer.valueOf(gender.trim()));
        }

        if (!StringUtils.isEmpty(birth)) {
            Date d = DateUtil.strToDate(birth, "yyyy-MM-dd");
            user.setBirth(d);
        }

        if (!StringUtils.isEmpty(address)) {
            student.setAddress(address);
        }

//        if(!StringUtils.isEmpty(areaPath)){
//            Map<String, Object> area = areaService.selectAreaByPath(areaPath);
//            JSONObject jsonObject = JSONObject.fromObject(area);
//            member.setAreaStore(jsonObject.toString());
//        }

//        if(!StringUtils.isEmpty(zipCode)){
//            member.setZipCode(zipCode.trim());
//        }

//        if (!StringUtils.isEmpty(phone)) {
//            user.setMobile(phone.trim());
//        }

        if (!StringUtils.isEmpty(idCard)) {
            user.setCertificateNo(idCard);
        }

//        if(!StringUtils.isEmpty(signature)){
//            member.setSignature(signature.trim());
//        }

        if (!StringUtils.isEmpty(educationExperience)) {
            student.setEducationId(educationExperience);
        }

        if (!StringUtils.isEmpty(identity)) {
            userType.setUsertype(Integer.valueOf(identity.trim()));
        }

        if (!StringUtils.isEmpty(school)) {
            student.setGraduateUniversityId(school);
        }

        if (!StringUtils.isEmpty(learnSchool)) {
            student.setStudyCampusId(learnSchool.trim());
        }

        if (!StringUtils.isEmpty(learnSecialty)) {
            student.setStudySpecialty(learnSecialty.trim());
        }

        if (!StringUtils.isEmpty(learnGrade)) {
            student.setStudyGrade(learnGrade.trim());
        }

        if (!StringUtils.isEmpty(emergencyContactName)) {
            student.setEmergencyContactName(emergencyContactName.trim());
        }

        if (!StringUtils.isEmpty(emergencyContactPhone)) {
            student.setEmergencyContactPhone(emergencyContactPhone.trim());
        }

        if (!StringUtils.isEmpty(weixin)) {
            student.setWeixin(weixin.trim());
        }

        if (!StringUtils.isEmpty(weibo)) {
            student.setWeibo(weibo.trim());
        }

        if (!StringUtils.isEmpty(qq)) {
            student.setQq(qq.trim());
        }

//        if(!StringUtils.isEmpty(accaName)){
//            member.setAccaName(accaName.trim());
//        }
//
//        if(!StringUtils.isEmpty(accaPassword)){
//            member.setAccaPassword(accaPassword.trim());
//        }
//
//        if(!StringUtils.isEmpty(cmaLoginName)){
//            member.setCmaLoginName(cmaLoginName.trim());
//        }
//
//        if(!StringUtils.isEmpty(cmaPassword)){
//            member.setCmaPassword(cmaPassword.trim());
//        }
//
//        if(!StringUtils.isEmpty(cmaName)){
//            member.setCmaName(cmaName.trim());
//        }

        userService.updateUser(user);
        studentService.updateStudent(student);
        userTypeService.updateUserType(userType);
        //添加同步数据代码
        JSONObject j = new JSONObject();
        j.put("businessSign", "editUser");
        j.put("recordId", user.getUserId());
        j.put("recordOpId", "");
        j.put("dataSource", "webpc");
        j.put("businessInfo", "编辑用户");
        messageSender.simpleSend(j.toString());

        return success("");
    }

    @RequestMapping(value = "/api/edu/zbids/member/checklogin")
    @ResponseBody
    public Map<String, Object> checkMemberToken(HttpServletRequest request,
                                                String viewertoken, String viewername) {
        Map<String, Object> map = new HashMap<String, Object>();
        if (StringUtils.isEmpty(viewertoken) || StringUtils.isEmpty(viewername)) {
            map.put("result", "error");
            map.put("message", "登录失败");
            return map;
        }
        //验证token
        String value = getUserId(viewertoken, 6 * 60 * 60);
        if (StringUtils.isEmpty(value)) {
            //判断用户信息
            User loginMember = null;
            if (ValidatorUtil.isEmail(viewername)) {
                loginMember = userService.getUserByEmail(viewername);
            } else if (ValidatorUtil.isMobile(viewername)) {
                loginMember = userService.getUserByPhone(viewername);
            } else {
                loginMember = userService.getUserByNickname(viewername);
            }
            if (loginMember == null) {
                // 用户名或密码错误
                map.put("result", "error");
                map.put("message", "登录失败");
                return map;
            }

            //密码错误
            if (!loginMember.getPassword().equals(DigestUtils.md5Hex(viewertoken))) {
                map.put("result", "error");
                map.put("message", "用户名或密码错误");
                return map;
            }
            Map<String, Object> jsonMap = new HashMap<String, Object>();
            jsonMap.put("id", loginMember.getUserId());
            jsonMap.put("name", loginMember.getLoginName());
            //jsonMap.put("avatar", "http://img.caicui.com"+loginMember.getBigAvatar());
            map.put("result", "ok");
            map.put("message", "登录成功");
            map.put("user", jsonMap);
            return map;

        } else if (value.equals(viewername)) {
            User member = userService.getUserById(viewername);
            Map<String, Object> jsonMap = new HashMap<String, Object>();
            if (member != null) {
                jsonMap.put("id", member.getUserId());
                jsonMap.put("name", member.getLoginName());
                //jsonMap.put("avatar", "http://img.caicui.com"+member.getBigAvatar());
            }
            map.put("result", "ok");
            map.put("message", "登录成功");
            map.put("user", jsonMap);
            return map;
        } else {
            map.put("result", "error");
            map.put("message", "登录失败");
            return map;
        }
    }

    /**
     * 通过老密码修改密码
     *
     * @param request
     * @param token
     * @return
     */
    @RequestMapping(value = "/api/edu/zbids/member/changePwdByPWD", method = RequestMethod.POST)
    @ResponseBody
    private Map<String, Object> changePwdByPWD(HttpServletRequest request,
                                               String token) {

        String oldpwd = request.getParameter("oldpwd");
        String newpwd = request.getParameter("newpwd");
        if (StringUtils.isEmpty(oldpwd) || StringUtils.isEmpty(newpwd) || StringUtils.isEmpty(token)) {
            return error("参数错误！");
        }

        String userId = getUserId(token, 6 * 60 * 60);
        if (StringUtils.isEmpty(userId)) {
            return error("nologin");
        }
        User user = userService.getUserById(userId);
        if (user == null) {
            return error("用户不存在");
        }
        if (!DigestUtils.md5Hex(oldpwd).equals(user.getPassword())) {
            return error("原密码错误");
        }

        user.setPassword(DigestUtils.md5Hex(newpwd));
        userService.updateUser(user);
        //添加同步数据代码
        JSONObject j = new JSONObject();
        j.put("businessSign", "editUser");
        j.put("recordId", user.getUserId());
        j.put("recordOpId", "");
        j.put("dataSource", "webpc");
        j.put("businessInfo", "编辑用户");

        messageSender.simpleSend(j.toString());

        return success("修改密码成功！");

    }

    @RequestMapping(value = "/api/edu/zbids/member/loginout/")
    @ResponseBody
    public Object logout(HttpServletRequest request,
                         HttpServletResponse response, String token) {
        try {
            if (!StringUtils.isEmpty(token)) {
                byte[] o = redisRawService.get(token);
                if (o != null) {
                    redisService.delete(token);
                }
            }
        } catch (Exception e) {
            // 程序异常
            error("-1000");
        }

        // 退出成功
        return success(null);
    }

    /**
     * 第三方授权后,获取用户id app调用此方法判断用户是否已绑定
     * 如果绑定直接登录；如果还没绑定，返回跟app没有绑定的状态，app需要调整到绑定或注册页面，引导用户进行注册或绑定
     *
     * @param request
     * @param token
     * @param societyType 第三方类型：1微信;2微博;3qq
     * @param societyId
     * @return
     */

    @RequestMapping(value = "/api/edu/zbids/member/oauthLogin/", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> oauthLogin(HttpServletRequest request,
                                          String token, String societyType, String societyId) {

        if (StringUtils.isEmpty(token)) {
            return error("token已过期");
        }
        Object object = redisService.get(token);
        if (object == null) {
            return error("token已过期");
        }

        if (StringUtils.isEmpty(societyType) || StringUtils.isEmpty(societyId)) {
            // 参数有误！
            return error("1000");
        }

//        societyId = societyType.substring(societyType.indexOf("_") + 1) + "_"
//                + societyId;

        UserThird userThird = userThirdService.getUserThirdBySocietyId(Integer.valueOf(societyType), societyId);
        if (userThird == null) {
            // 此账号没还没绑定财萃账号
            return error("1001");
        }

        User user = userService.getUserById(userThird.getUserId());
        UserLogin userLogin = userLoginService.getUserLoginByUserId(userThird.getUserId());
        if (user.getState() == 0) {
            return error("账号没有启用");
        }
        if (userLogin.getIsAccountLocked()) {
            return error("账号被禁用");
        }

        int loginFailureCount = userLogin.getLoginFailureCount();
        if (loginFailureCount > 0) {
            userLogin.setLoginIp(RequestUtils.getRemoteAddr(request));
            userLogin.setLoginDate(new Date());
            userLogin.setLoginFailureCount(0);
            userLoginService.updateUserLogin(userLogin);
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
        Map<String, Object> date = new HashMap<String, Object>();
        date.put("nickName", user.getLoginName());
        date.put("memberId", user.getUserId());
        date.put("avatar", student.getAvatar());
        date.put("token", token);
        date.put("isAvatar", student.isAvatarUploaded());
        // 成功返回
        return success(date);
    }

    @RequestMapping(value = "/api/edu/zbids/member/edituserheadr", method = RequestMethod.POST)
    @ResponseBody
    public Object cutAndSaveAvatar(HttpServletRequest request,
                                   @RequestParam(value = "file", required = false) MultipartFile file) throws IOException {
        if (file == null) {
            return error("没有找到图片");
        }
        String token = request.getParameter("token");
        String userId = getUserId(token, 6 * 60 * 60);
        if (StringUtils.isEmpty(userId)) {
            return error("nologin");
        }
        User user = userService.getUserById(userId);
        if (user == null) {
            return error("用户不存在!");
        }

        String path = FileUtil.copyFile(request, file);
        File f = new File(request.getSession().getServletContext()
                .getRealPath(path));
        int width = 0; // 得到图片的宽度
        int height = 0; // 得到图片的高度
        try {
            BufferedImage buff = ImageIO.read(f);
            width = buff.getWidth(); // 得到图片的宽度
            height = buff.getHeight(); // 得到图片的高度
        } catch (IOException e) {
            e.printStackTrace();
        }

        String AVATAR_PATH = "/upload/avatar/";

        int left = 0, top = 0;

        path = StringUtils.substringBeforeLast(path, "?");

        File imgFile = new File(request.getSession().getServletContext()
                .getRealPath(path));

        String id = user.getUserId();
        // 图像文件保存目录
        String bigAvatar = AVATAR_PATH + "big_" + id + ".jpg";
        String middleAvatar = AVATAR_PATH + "middle_" + id + ".jpg";
        String smallAvatar = AVATAR_PATH + "small_" + id + ".jpg";

        String realPath = request.getSession().getServletContext()
                .getRealPath(bigAvatar);
        File outFile = new File(realPath);
        Thumbnails
                .of(imgFile)
                .sourceRegion(left, top, width, height)
                .size(width, height)
                .outputQuality(1.0f)
                .toFile(outFile);

        ImageUtil.zoom(outFile, new File(request.getSession()
                        .getServletContext().getRealPath(middleAvatar)),
                (int) (width * 0.8), (int) (height * 0.8));
        ImageUtil.zoom(outFile, new File(request.getSession()
                        .getServletContext().getRealPath(smallAvatar)),
                (int) (width * 0.5), (int) (height * 0.5));

        // 更新会员信息
        //TODO 更新student 表
        Student student = studentService.selectStudentById(user.getUserId());
        student.setAvatar(bigAvatar);
        studentService.updateStudent(student);
        // json参数容器

        Map<String, Object> jsonMap = new HashMap<String, Object>();
        jsonMap.put("path", path);
        jsonMap.put("width", width);
        jsonMap.put("height", height);
        return success(jsonMap);
    }

    /**
     * 第三方绑定
     *
     * @param request
     * @param token
     * @param account
     * @param password
     * @param societyId
     * @param societyType
     * @return
     */
    @RequestMapping(value = "/api/edu/zbids/member/oauthBinding", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> oauthBinding(HttpServletRequest request,
                                            String token, String account, String password, String societyId,
                                            String societyType) {
        String code = request.getParameter("code");

        if (StringUtils.isEmpty(token)) {
            return error("token已过期");
        }
        Object object = redisService.get(token);
        if (object == null) {
            return error("token已过期");
        }

        if (StringUtils.isEmpty(account) || StringUtils.isEmpty(password)
                || StringUtils.isEmpty(societyId)
                || StringUtils.isEmpty(societyType)) {
            // 用户名为空
            return error("1000");
        }

        /*societyId = societyType.substring(societyType.indexOf("_") + 1) + "_"
                + societyId;*/

        User user;
        UserLogin userLogin;
        if (ValidatorUtil.isEmail(account)) {
            user = userService.getUserByEmail(account);
        } else if (ValidatorUtil.isMobile(account)) {
            user = userService.getUserByPhone(account);
        } else {
            user = userService.getUserByNickname(account);
        }

        if (user == null) {
            // 用户名或密码错误
            return error("用户名或密码错误");
        }
        userLogin = userLoginService.getUserLoginByUserId(user.getUserId());
        if (user.getState() == 0) {
            return error("账号没有启用");
        }
        if (userLogin.getIsAccountLocked()) {
            return error("账号被禁用");
        }

        if (!StringUtils.isEmpty(code)) {
            boolean b = getCheckCode(token, "loginimgcode", null, code);
            if (!b) {
                return error("图片验证码错误！");
            }
        }
        //密码错误
        if (!user.getPassword().equals(DigestUtils.md5Hex(password))) {
            int loginFailureCount = userLogin.getLoginFailureCount() + 1;
            if (loginFailureCount >= 6) {
                userLogin.setIsAccountLocked(true);
                userLogin.setLockedDate(new Date());
            }
            userLogin.setLoginFailureCount(loginFailureCount);
            userLoginService.updateUserLogin(userLogin);
            if (userLogin.getIsAccountLocked()) {
                //添加同步数据代码
                JSONObject j = new JSONObject();
                j.put("businessSign", "editUser");
                j.put("recordId", user.getUserId());
                j.put("recordOpId", "");
                j.put("dataSource", "webpc");
                j.put("businessInfo", "编辑用户");

                messageSender.simpleSend(j.toString());
            }
            return error("用户名或密码错误");
        }

        userLogin.setLoginIp(RequestUtils.getRemoteAddr(request));
        userLogin.setLoginDate(new Date());
        userLogin.setLoginFailureCount(0);
        userLoginService.updateUserLogin(userLogin);

        if (!StringUtils.isEmpty(societyId)
                && !StringUtils.isEmpty(societyType)) {
            UserThird _ut = userThirdService.getUserThirdBySocietyId(Integer.valueOf(societyType), societyId);
            if (_ut != null) {
                // 此账号已绑定
                return error("账号已绑定");
            }
            UserThird userThird = new UserThird();
            userThird.setThirdType(Integer.valueOf(societyType));
            userThird.setThirdUserid(societyId);
            userThird.setUserId(user.getUserId());
            userThirdService.insertUserThird(userThird);
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
        Map<String, Object> date = new HashMap<String, Object>();
        date.put("nickName", user.getLoginName());
        date.put("memberId", user.getUserId());
        date.put("avatar", student.getAvatar());
        date.put("token", token);
        date.put("isAvatar", student.isAvatarUploaded());
        // 成功返回
        return success(date);

    }

    /**
     * 检验手机号／邮箱／昵称是否已存在
     *
     * @param request
     * @param checkname
     * @param response
     * @return
     */
    @RequestMapping(value = "/api/edu/zbids/member/checkexist")
    @ResponseBody
    public Map<String, Object> checkexist(HttpServletRequest request, String checkname, HttpServletResponse response) {
        String checktype = request.getParameter("checktype");
        String memberId = request.getParameter("memberId");
        if (StringUtils.isEmpty(checkname) || StringUtils.isEmpty(checktype)) {
            return error("参数不能为空！");
        }
        User p = null;
        if (!StringUtils.isEmpty(memberId)) {
            p = userService.getUserById(memberId);
            if (p == null) {
                return error("用户不存在!");
            }
        }

        //手机号
        if (checktype.equals("1")) {
            User _m = userService.getUserByPhone(checkname);
            if (_m != null) {
                if (StringUtils.isEmpty(memberId)) {
                    return success("true");
                } else {
                    if (!checkname.equals(p.getMobile())) {
                        return success("true");
                    } else {
                        return success("false");
                    }
                }
            } else {
                return success("false");
            }
        } else if (checktype.equals("2")) {
            User _e = userService.getUserByEmail(checkname);
            if (_e != null) {
                if (StringUtils.isEmpty(memberId)) {
                    return success("true");
                } else {
                    if (!checkname.equals(p.getEmail())) {
                        return success("true");
                    } else {
                        return success("false");
                    }
                }
            } else {
                return success("false");
            }
        } else if (checktype.equals("3")) {
//            if(appMemberService.isNameKeep(checkname)){
//                return success("true");
//            }
            User _n = userService.getUserByNickname(checkname);
            if (_n != null) {
                if (StringUtils.isEmpty(memberId)) {
                    return success("true");
                } else {
                    if (!checkname.equals(p.getLoginName())) {
                        return success("true");
                    } else {
                        return success("false");
                    }
                }
            } else {
                return success("false");
            }
        } else {
            return error("无效类型！");
        }
    }

    @RequestMapping(value = "/api/edu/zbids/outboundSystem/createStudent")
    @ResponseBody
    public Map<String, Object> createStudentByOutboundSystem(HttpServletRequest request,
                                                             String token, StudentCreateReqDMO studentReqDMO) {
        try {
            studentReqDMO = setNullValue(studentReqDMO);
        } catch (Exception e) {
            return error("参数错误");
        }
        String memberId = getMemberId(token);
        if (StringUtils.isEmpty(memberId)) {
            return error("nologin");
        }
        User user = userService.getUserByNickname(memberId);
        if (user == null){
            return error("查询不到登陆信息");
        }
        studentReqDMO.setCreateUid(user.getUserId());
        if (StringUtils.isBlank(studentReqDMO.getQq()) &&
                StringUtils.isBlank(studentReqDMO.getWeixin()) &&
                StringUtils.isBlank(studentReqDMO.getMobile()) &&
                StringUtils.isBlank(studentReqDMO.getCertificateNo())) {
            return error("证件号、QQ、手机号、微信号至少选填一项");
        }
        if (StringUtils.isBlank(studentReqDMO.getRegRegionId())) {
            return error("用户所属区域必选");
        }
        studentReqDMO.setRegSource(7);
        JsonResult<String> result = studentEduService.createStudentByOutboundSystem(studentReqDMO);
        if (result.isSuccess()) {
            return success(result.getData());
        } else {
            return error(result.getMsg());
        }
    }

    /***********************************************************************************
     **********************   公共私有方法            *********************************************
     ***********************************************************************************/


    private void setex(String key, int seconds, String value) {
        //获取所有key
        Set<String> keySet = redisService.keys("*");
        if (keySet != null && !keySet.isEmpty()) {
            for (String ks : keySet) {
                byte[] o = redisRawService.get(ks);
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


    private String getUserId(String token, int seconds) {
        String memberId = "";
        if (!StringUtils.isEmpty(token)) {
            byte[] object = redisRawService.get(token);
            if (object != null) {
                memberId = new String(object);
                if (seconds > 0) {
                    redisRawService.set(token, memberId, Long.valueOf(seconds));
                }
            }
        }
        return memberId;
    }


    private boolean getCheckCode(String token, String type, String client, String code) {
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


    private Boolean checkToken(String token, String guestIP) {
        if (StringUtils.isEmpty(token)) {
            return false;
        }
        byte[] o = redisRawService.get(token);
        if (o == null) {
            return false;
        }
        String appid = new String(o);
        redisRawService.set(token, appid, Long.valueOf(10 * 60));

        App app = appService.selectAppById(appid.toString());
        //认证是否启动
        if (app != null && "1".equals(app.getState())) {
            //安全级别判断
            if (!app.getAppLevl().isEmpty() && Integer.valueOf(app.getAppLevl()) > 3) {
                if (StringUtils.isEmpty(guestIP)) {
                    return false;
                }
                if (app.getWhiteIpList().indexOf(guestIP) != -1) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
