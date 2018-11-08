package com.caicui.course.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.caicui.app.service.UserService;
import com.caicui.commons.api.controller.ApiCommonController;
import com.caicui.commons.utils.JmsQueueSender;
import com.caicui.commons.dubbo.entity.Result;
import com.caicui.course.local.dubbo.service.CourseRestructureApiService;
import com.caicui.mail.dubbo.service.MailApiService;
import com.edu.commons.utils.JsonResult;
import com.edu.dubbo.course.service.CourseActiveService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Controller
public class CourseActiveController extends ApiCommonController {

    @Autowired
    private JmsQueueSender messageSender;
    @Reference
    private CourseActiveService courseActiveService;
    //    @Autowired
//    private AppMemberService appMemberService;
    @Reference
    private CourseRestructureApiService courseRestructureApiService;
    @Reference
    private MailApiService mailApiService;

    private static final String KEY = "newApiCourse";


    /**
     * 课程激活
     * 激活存放的是versionId+orderitem信息
     *
     * @param request
     * @param token           验证用户登录
     * @param courseId        课程id
     * @param examinationDate 考试时间
     * @return
     */
    @RequestMapping(value = "/api/edu/business/order/courseActive", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> active(HttpServletRequest request, String token, String courseId, Date examinationDate, String userId) {

        String memberId = getMemberId(token);
        if (StringUtils.isEmpty(memberId)) {
            return error("nologin");
        }
//        AppMember member = appMemberService.getMemberById(memberId);
//        if(member == null ){
//            return error("nologin");
//        }
//        if(StringUtils.isEmpty(orderItemId)){
//            return error("参数错误");
//        }
        if (StringUtils.isEmpty(courseId)) {
            return error("参数错误");
        }

//        String mobile = request.getParameter("mobile");
//        String address = request.getParameter("address");
//        String examTime = request.getParameter("examTime");
        if (StringUtils.isBlank(userId)) {
            userId = memberId;
        }
        JsonResult<Map<String, Object>> jsonResult = courseActiveService.activeCourseByCourseId(userId, courseId, memberId, examinationDate);
        if (!jsonResult.isSuccess()) {
            return error(jsonResult.getMsg());
        }
        Map<String, Object> map = jsonResult.getData();
        if (map != null) {
            String state = (String) map.get("state");
            if (!StringUtils.isEmpty(state) && state.equals("success")) {
                String orderId = (String) map.get("data");
                String categoryName = (String) map.get("categoryName");
                String nickName = (String) map.get("nickName");
                String email = (String) map.get("email");
                String courseTitle = (String) map.get("courseTitle");
                Long expiryDate = (Long) map.get("expiryDate");

                //添加同步数据代码
                JSONObject j = new JSONObject();
                j.put("businessSign", "activeCourse");
                j.put("recordId", orderId);
                j.put("recordOpId", courseId);
                j.put("dataSource", "webpc");
                j.put("businessInfo", "课程激活");

                messageSender.simpleSend(j.toString());
                //发激活邮件
                Result tt = mailApiService.sendActiveOrderMail(email, nickName, categoryName, courseTitle, expiryDate.toString());
                return success("");
            } else {
                return error((String) map.get("msg"));
            }
        }

        return error("激活失败");
    }

    /**
     *
     * @param hash
     * @param data
     * @return
     */
    @RequestMapping(value = "/api/edu/business/order/syncCourseActive", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> syncCourseActive(String hash,String data){
        if (StringUtils.isBlank(hash)){
            return error("参数错误");
        }
        Calendar now = Calendar.getInstance();
        now.setTime(new Date());
        now.set(Calendar.SECOND, 0);
        now.set(Calendar.MILLISECOND, 0);

        long time = now.getTime().getTime();
        String pass = (DigestUtils.md5Hex(KEY + time));
        if (!pass.equals(hash)){
            return error("参数错误");
        }
        if (data == null){
            return error("参数错误");
        }
        data = data.replaceAll("&quot;", "'").replaceAll("&#39;", "'");


        JSONArray jsonArray = JSONArray.fromObject(data);
        List<List<String>> param = new ArrayList<>(jsonArray.size());
        for (Object obj : jsonArray) {
            List list = (List)obj;
            List<String> stringList = new ArrayList<>();
            for (Object o : list) {
                String str = (String)o;
                stringList.add(str);
            }
            param.add(stringList);
        }

        return success(courseActiveService.syncStudentCourseActive(null,param).getData());
    }

//    /**
//     * 获取未激活课程信息
//     *
//     * @param request
//     * @param response
//     * @return
//     */
//    @RequestMapping(value = "/api/business/learning/noActivecourse/v1.0", method = RequestMethod.GET)
//    @ResponseBody
//    public Map<String, Object> getNoActiveCourse(String token,
//                                                 @RequestParam(required = false, defaultValue = "1", value = "pageNo") Integer pageNo,
//                                                 @RequestParam(required = false, defaultValue = "10", value = "pageSize") Integer pageSize,
//                                                 HttpServletRequest request) {
//
//        Map<String, Object> map = new HashMap<String, Object>();
//        map.put("pageNo", pageNo);
//        map.put("pageSize", pageSize);
//        map.put("courselist", null);
//        map.put("total", 0);
//
//        String memberId = getMemberId(token);
//        if (StringUtils.isEmpty(memberId)) {
//            return error("nologin");
//        }
////        AppMember member = appMemberService.getMemberById(memberId);
////        if(member == null ){
////            return error("nologin");
////        }
//
//        PageResult pageResult = courseActiveService.getNotActiveCourseByStudentId(memberId, pageNo, pageSize);
//        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
//        List<CourseActive> msgList = pageResult.getPageResult();
//        if (msgList != null && !msgList.isEmpty()) {
//            for (CourseActive m : msgList) {
//                Map<String, Object> p = new HashMap<String, Object>();
////                    p.put("orderID_item_id",m.getOrderItemId()); //订单子项id
////                    p.put("lock_status", m.getLockStatus());
////                    p.put("lock_date", m.getLockDate()==null?null:m.getLockDate().getTime()/1000);
//
////                    if(!StringUtils.isEmpty(m.getCourseGroupId())){
////                        CourseGroup g = courseGroupService.selectCourseGroupById(m.getCourseGroupId());
////                        if(g!=null){
////                            p.put("courseGroupId", g.getId());
////                            p.put("isU", g.getIsU());
////                            p.put("actimode", g.getActimode()==null?1:g.getActimode());
////                            p.put("expirationDate", g.getExpirationDate());
////                            p.put("shopCategoryExtendId", g.getShopCategoryExtendId());
////                            p.put("shopCategoryExtendName", g.getShopCategoryExtendName());
////                        }
////                    }
//                //补全新信息
//                p.put("courseActiveId", m.getCourseActiveId());
//                p.put("courseId", m.getCourseId());
//                p.put("courseSourceType", m.getCourseSourceType());
//                p.put("courseSourceId", m.getCourseSourceId());
//                p.put("courseCategoryId", m.getCourseCategoryId());
//                p.put("itemId", m.getItemId());
//                p.put("branchId", m.getBranchId());
//                p.put("orderCourseProductId", m.getOrderCourseProductId());
//                p.put("studentCourseCategoryDetailId", m.getStudentCourseCategoryDetailId());
//                p.put("courseProductId", m.getCourseProductId());
//                p.put("activeTime", m.getActiveTime());
//                p.put("activeState", m.getActiveState());
//                p.put("endTime", m.getEndTime());
//                p.put("studyEndDate", m.getStudyEndDate());
//                p.put("teachMode", m.getTeachMode());
//                p.put("isRehear", m.getIsRehear());
//                p.put("pauseNum", m.getPauseNum());
//                p.put("cancelActiveNum", m.getCancelActiveNum());
//                p.put("rehearNum", m.getRehearNum());
//                p.put("courseState", m.getCourseState());
//                p.put("isGive", m.getIsGive());
//
////                    boolean isOnline = false;
//                Map<String, Object> course = courseRestructureApiService.getCourseBaseInfo(m.getCourseId(), 1);
//                if (course != null) {
//                    String teachingType = course.get("teachingType") == null ? "" : course.get("teachingType").toString();
////                        if(teachingType.isEmpty()||teachingType.equals("onlinecourse")){
////                            isOnline = true;
////                        }
//                    p.put("categoryId", course.get("categoryId")); //课程所属证书分类
//                    p.put("categoryIndex", course.get("categoryIndex")); //证书序号
//                    p.put("categoryName", course.get("categoryName"));  //课程所属证书分类名称
//                    p.put("subjectID", course.get("subjectId")); //课程所属科目id
//                    p.put("subjectIndex", course.get("subjectIndex"));   //科目序号
//                    p.put("subjectName", course.get("subjectName")); //课程所属科目名称
//                    //p.put("courseId",course.get("courseId")); ////课程id
//                    p.put("versionId", course.get("versionId"));
//                    p.put("courseIndex", course.get("courseIndex"));     //课程序号
//                    p.put("courseName", course.get("courseName")); //课程名称
//                    p.put("courseBkImage", course.get("courseBackgroundImage")); //课程图片
//                    p.put("outline", course.get("outline"));                //大纲变更
//                    p.put("teacherName", course.get("teacherName"));  //教师姓名
//                    p.put("teacherImage", course.get("teacherImage"));  //教师头像
//                    p.put("taskTotal", course.get("taskTotal")); //课程总任务数
//                    p.put("teacherHonor", course.get("teacherHonor"));   //教师荣誉（个人签名）
//                    p.put("courseSource", course.get("courseSource")); //课程来源
//                    p.put("availability", course.get("availability"));   //课程动态
//
//                } else {
//                    System.out.println(".........没有查询出对应的课程........VersionId..........." + m.getCourseId());
//                }
////                    if(isOnline)
//                list.add(p);
//            }
//        }
//        map.put("courselist", list);
//        map.put("total", pageResult.getTotalRecord());
//
//
//        return success(map);
//    }
//
//
//    /**
//     * 在学的课程列表
//     *
//     * @param request
//     * @param response
//     * @return
//     */
//    @RequestMapping(value = "/api/business/learning/learningcourse/v1.0", method = RequestMethod.GET)
//    @ResponseBody
//    public Map<String, Object> getLearningCourse(String token,
//                                                 @RequestParam(required = false, defaultValue = "1", value = "pageNo") Integer pageNo,
//                                                 @RequestParam(required = false, defaultValue = "10", value = "pageSize") Integer pageSize
//            , HttpServletRequest request) {
//        Map<String, Object> map = new HashMap<String, Object>();
//        map.put("pageNo", pageNo);
//        map.put("pageSize", pageSize);
//        map.put("courselist", null);
//        map.put("total", 0);
//
//        String memberId = getMemberId(token);
//        if (StringUtils.isEmpty(memberId)) {
//            return error("nologin");
//        }
////        AppMember member = appMemberService.getMemberById(memberId);
////        if(member == null ){
////            return error("nologin");
////        }
//
//        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        String enddate = df.format(new Date());
//
//        PageResult mesMap = courseActiveService.getActiveCourseByStudentId(memberId, pageNo, pageSize);
//
//        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
//        List<CourseActive> msgList = mesMap.getPageResult();
//        if (msgList != null && !msgList.isEmpty()) {
//            for (CourseActive m : msgList) {
//                Map<String, Object> p = new HashMap<String, Object>();
////                    p.put("orderID_item_id",m.getOrderItemId()); //订单子项id
//                p.put("expirationTime", m.getEndTime() == null ? null : m.getEndTime().getTime() / 1000);
////                    p.put("lock_status", m.getLockStatus());
////                    p.put("lock_date", m.getLockDate()==null?null:m.getLockDate().getTime()/1000);
////                    if(!StringUtils.isEmpty(m.getCourseGroupId())){
////                        CourseGroup g = courseGroupService.selectCourseGroupById(m.getCourseGroupId());
////                        if(g!=null){
////                            p.put("courseGroupId", g.getId());
////                            p.put("isU", g.getIsU());
////                            p.put("shopCategoryExtendId", g.getShopCategoryExtendId());
////                            p.put("shopCategoryExtendName", g.getShopCategoryExtendName());
////                        }
////                    }
//
//                //补全新信息
//                p.put("courseActiveId", m.getCourseActiveId());
//                p.put("courseId", m.getCourseId());
//                p.put("courseSourceType", m.getCourseSourceType());
//                p.put("courseSourceId", m.getCourseSourceId());
//                p.put("courseCategoryId", m.getCourseCategoryId());
//                p.put("itemId", m.getItemId());
//                p.put("branchId", m.getBranchId());
//                p.put("orderCourseProductId", m.getOrderCourseProductId());
//                p.put("studentCourseCategoryDetailId", m.getStudentCourseCategoryDetailId());
//                p.put("courseProductId", m.getCourseProductId());
//                p.put("activeTime", m.getActiveTime());
//                p.put("activeState", m.getActiveState());
//                p.put("endTime", m.getEndTime());
//                p.put("studyEndDate", m.getStudyEndDate());
//                p.put("teachMode", m.getTeachMode());
//                p.put("isRehear", m.getIsRehear());
//                p.put("pauseNum", m.getPauseNum());
//                p.put("cancelActiveNum", m.getCancelActiveNum());
//                p.put("rehearNum", m.getRehearNum());
//                p.put("courseState", m.getCourseState());
//                p.put("isGive", m.getIsGive());
//
////                    boolean isOnline = false;
//                Map<String, Object> course = courseRestructureApiService.getCourseBaseInfo(m.getCourseId(), 1);
//                if (course != null) {
//                    String teachingType = course.get("teachingType") == null ? "" : course.get("teachingType").toString();
////                        if(teachingType.isEmpty()||teachingType.equals("onlinecourse")){
////                            isOnline = true;
////                        }
//                    p.put("categoryId", course.get("categoryId")); //课程所属证书分类
//                    p.put("categoryIndex", course.get("categoryIndex")); //证书序号
//                    p.put("categoryName", course.get("categoryName"));  //课程所属证书分类名称
//                    p.put("subjectID", course.get("subjectId")); //课程所属科目id
//                    p.put("subjectIndex", course.get("subjectIndex"));   //科目序号
//                    p.put("subjectName", course.get("subjectName")); //课程所属科目名称
//                    //p.put("courseId",course.get("courseId")); ////课程id
//                    p.put("versionId", course.get("versionId"));
//                    p.put("courseIndex", course.get("courseIndex"));     //课程序号
//                    p.put("courseName", course.get("courseName")); //课程名称
//                    p.put("courseBkImage", course.get("courseBackgroundImage")); //课程图片
//                    p.put("outline", course.get("outline"));                //大纲变更
//                    p.put("teacherName", course.get("teacherName"));  //教师姓名
//                    p.put("teacherImage", course.get("teacherImage"));  //教师头像
//                    p.put("taskTotal", course.get("taskTotal")); //课程总任务数
//                    p.put("teacherHonor", course.get("teacherHonor"));   //教师荣誉（个人签名）
//                    p.put("courseSource", course.get("courseSource")); //课程来源
//                    p.put("availability", course.get("availability"));   //课程动态
//                } else {
//                    System.out.println(".........没有查询出对应的课程........VersionId..........." + m.getCourseId());
//                }
////                    if(isOnline)
//                list.add(p);
//            }
//        }
//        map.put("courselist", list);
//        map.put("total", mesMap.getTotalRecord());
//
//        return success(map);
//    }
//
//    /**
//     * 已过期课程
//     *
//     * @param request
//     * @param response
//     * @return
//     */
//    @RequestMapping(value = "api/business/learning/expirationcourse/v1.0", method = RequestMethod.GET)
//    @ResponseBody
//    public Map<String, Object> getExpirationCourse(String token,
//                                                   @RequestParam(required = false, defaultValue = "1", value = "pageNo") Integer pageNo,
//                                                   @RequestParam(required = false, defaultValue = "10", value = "pageSize") Integer pageSize,
//                                                   HttpServletRequest request) {
//        Map<String, Object> map = new HashMap<String, Object>();
//
//        String memberId = getMemberId(token);
//        if (StringUtils.isEmpty(memberId)) {
//            return error("nologin");
//        }
////        AppMember member = appMemberService.getMemberById(memberId);
////        if(member == null ){
////            return error("nologin");
////        }
//
//        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        String enddate = df.format(new Date());
//
//        PageResult mesMap = courseActiveService.getOverdueCourse(memberId, pageNo, pageSize);
//        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
//        List<CourseActive> msgList = mesMap.getPageResult();
//        if (msgList != null && !msgList.isEmpty()) {
//            for (CourseActive m : msgList) {
//                Map<String, Object> p = new HashMap<String, Object>();
////                    p.put("orderID_item_id",m.getOrderItemId()); //订单子项id
//                p.put("expirationTime", m.getEndTime() == null ? null : m.getEndTime().getTime() / 1000);
////                    p.put("lock_status", m.getLockStatus());
////                    p.put("lock_date", m.getLockDate()==null?null:m.getLockDate().getTime()/1000);
//
////                    if(!StringUtils.isEmpty(m.getCourseGroupId())){
////                        CourseGroup g = courseGroupService.selectCourseGroupById(m.getCourseGroupId());
////                        if(g!=null){
////                            p.put("courseGroupId", g.getId());
////                            p.put("isU", g.getIsU());
////                            p.put("actimode", g.getActimode()==null?1:g.getActimode());
////                            p.put("expirationDate", g.getExpirationDate());
////                            p.put("shopCategoryExtendId", g.getShopCategoryExtendId());
////                            p.put("shopCategoryExtendName", g.getShopCategoryExtendName());
////                        }
////                    }
//
//                //补全新信息
//                p.put("courseActiveId", m.getCourseActiveId());
//                p.put("courseId", m.getCourseId());
//                p.put("courseSourceType", m.getCourseSourceType());
//                p.put("courseSourceId", m.getCourseSourceId());
//                p.put("courseCategoryId", m.getCourseCategoryId());
//                p.put("itemId", m.getItemId());
//                p.put("branchId", m.getBranchId());
//                p.put("orderCourseProductId", m.getOrderCourseProductId());
//                p.put("studentCourseCategoryDetailId", m.getStudentCourseCategoryDetailId());
//                p.put("courseProductId", m.getCourseProductId());
//                p.put("activeTime", m.getActiveTime());
//                p.put("activeState", m.getActiveState());
//                p.put("endTime", m.getEndTime());
//                p.put("studyEndDate", m.getStudyEndDate());
//                p.put("teachMode", m.getTeachMode());
//                p.put("isRehear", m.getIsRehear());
//                p.put("pauseNum", m.getPauseNum());
//                p.put("cancelActiveNum", m.getCancelActiveNum());
//                p.put("rehearNum", m.getRehearNum());
//                p.put("courseState", m.getCourseState());
//                p.put("isGive", m.getIsGive());
//
//
////                    boolean isOnline = false;
//                Map<String, Object> course = courseRestructureApiService.getCourseBaseInfo(m.getCourseId(), 1);
//                if (course != null) {
//                    String teachingType = course.get("teachingType") == null ? "" : course.get("teachingType").toString();
////                        if(teachingType.isEmpty()||teachingType.equals("onlinecourse")){
////                            isOnline = true;
////                        }
//                    p.put("categoryId", course.get("categoryId")); //课程所属证书分类
//                    p.put("categoryIndex", course.get("categoryIndex")); //证书序号
//                    p.put("categoryName", course.get("categoryName"));  //课程所属证书分类名称
//                    p.put("subjectID", course.get("subjectId")); //课程所属科目id
//                    p.put("subjectIndex", course.get("subjectIndex"));   //科目序号
//                    p.put("subjectName", course.get("subjectName")); //课程所属科目名称
//                    //p.put("courseId",course.get("courseId")); ////课程id
//                    p.put("versionId", course.get("versionId"));
//                    p.put("courseIndex", course.get("courseIndex"));     //课程序号
//                    p.put("courseName", course.get("courseName")); //课程名称
//                    p.put("courseBkImage", course.get("courseBackgroundImage")); //课程图片
//                    p.put("outline", course.get("outline"));                //大纲变更
//                    p.put("teacherName", course.get("teacherName"));  //教师姓名
//                    p.put("teacherImage", course.get("teacherImage"));  //教师头像
//                    p.put("taskTotal", course.get("taskTotal")); //课程总任务数
//                    p.put("teacherHonor", course.get("teacherHonor"));   //教师荣誉（个人签名）
//                    p.put("courseSource", course.get("courseSource")); //课程来源
//                    p.put("availability", course.get("availability"));   //课程动态
//
//                } else {
//                    System.out.println(".........没有查询出对应的课程........VersionId..........." + m.getCourseId());
//                }
////                    if(isOnline)
//                list.add(p);
//            }
//        }
//        map.put("courselist", list);
//        map.put("total", mesMap.getTotalRecord());
//        map.put("isdisplay", false);
//
//        return success(map);
//    }
}
