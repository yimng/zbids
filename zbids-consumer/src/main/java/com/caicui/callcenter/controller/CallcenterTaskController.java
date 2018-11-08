package com.caicui.callcenter.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.caicui.app.entity.User;
import com.caicui.app.service.UserService;
import com.caicui.commons.api.controller.ApiCommonController;
import com.edu.callcenter.dmo.CallcenterTaskReqDMO;
import com.edu.dubbo.callcenter.service.CallcenterTaskEduService;
import com.edu.dubbo.student.service.StudentEduService;
import com.edu.student.commons.CommonsStudentAskConstants;
import com.edu.student.dmo.StudentAskCreateDMO;
import com.edu.student.dmo.StudentAskReqDMO;
import com.edu.student.dmo.StudentInfoUpdateDMO;
import com.edu.student.dmo.StudentInviteCreateReqDMO;
import com.google.common.base.Splitter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Arrays;
import java.util.List;

@Controller
public class CallcenterTaskController extends ApiCommonController {
    @Reference
    private CallcenterTaskEduService callcenterTaskEduService;
    @Reference
    private UserService userService;
    @Reference
    private StudentEduService studentEduService;

    /**
     * 查询任务
     *
     * @param token
     * @param reqDMO
     * @param pageSize
     * @param currentPage
     * @return
     * @author wangwenjun
     */
    @RequestMapping(value = "/api/edu/business/callcenter/queryCenterTask")
    @ResponseBody
    public Object queryCallCenterTaskByCallcenterTaskReqDMO(String token, CallcenterTaskReqDMO reqDMO,
                                                            @RequestParam(required = false, defaultValue = "10", value = "pageSize") Integer pageSize,
                                                            @RequestParam(required = false, defaultValue = "1", value = "currentPage") Integer currentPage) {
        String memberId = getMemberId(token);
        if (org.apache.commons.lang.StringUtils.isEmpty(memberId)) {
            return error("nologin");
        }
        return callcenterTaskEduService.queryCallCenterTaskByCallcenterTaskReqDMO(reqDMO, pageSize, currentPage);
    }

    /**
     * 查询任务学生
     *
     * @param token
     * @param callcenterTaskId
     * @param pageSize
     * @param currentPage
     * @return
     * @author wangwenjun
     */
    @RequestMapping("/api/edu/business/callcenter/queryTaskStudentByTaskId")
    @ResponseBody
    public Object queryCallCenterTaskStudentByTaskId(String token, String callcenterTaskId,
                                                     @RequestParam(required = false, defaultValue = "10", value = "pageSize") Integer pageSize,
                                                     @RequestParam(required = false, defaultValue = "1", value = "currentPage") Integer currentPage) {
        String memberId = getMemberId(token);
        if (org.apache.commons.lang.StringUtils.isEmpty(memberId)) {
            return error("nologin");
        }
        if (StringUtils.isBlank(callcenterTaskId)) {
            return error("参数错误");
        }
        return callcenterTaskEduService.queryCallCenterTaskStudentByTaskId(callcenterTaskId, pageSize, currentPage);
    }

    @RequestMapping("/api/edu/business/callcenter/queryAllTaskStudentByTaskId")
    @ResponseBody
    public Object queryAllCallCenterTaskStudentByTaskId(String token, String callcenterTaskId) {
        String memberId = getMemberId(token);
        if (org.apache.commons.lang.StringUtils.isEmpty(memberId)) {
            return error("nologin");
        }
        if (StringUtils.isBlank(callcenterTaskId)) {
            return error("参数错误");
        }
        return callcenterTaskEduService.queryAllCallCenterTaskStudentByTaskId(callcenterTaskId);
    }

    /**
     * 创建 更新邀约
     *
     * @param token
     * @param reqDMO
     * @return
     * @author wangwenjun
     */
    @RequestMapping(value = "/api/edu/business/callcenter/createOrUpdateInvite", method = RequestMethod.POST)
    @ResponseBody
    public Object createOrUpdateStudentInvite(String token, StudentInviteCreateReqDMO reqDMO) {
        String memberId = getMemberId(token);
        if (StringUtils.isEmpty(memberId)) {
            return error("nologin");
        }
        User user = userService.getUserByNickname(memberId);
        if (user == null) {
            return error("nologin");
        }
        if (StringUtils.isBlank(reqDMO.getStudentId()) ||
                (StringUtils.isBlank(reqDMO.getInviteSourceId()) && StringUtils.isBlank(reqDMO.getCallcenterTaskId())) ||
                reqDMO.getInviteSource() == null ||
                StringUtils.isBlank(reqDMO.getBranchId()) ||
                StringUtils.isBlank(reqDMO.getRegionId()) ||
                reqDMO.getInviteDate() == null) {
            return error("参数错误");
        }
        if (StringUtils.isBlank(reqDMO.getStudentInviteId())) {
            reqDMO.setCreateUid(user.getUserId());
            reqDMO.setCallcenterUserId(user.getUserId());
        } else {
            reqDMO.setCallcenterUserId(user.getUserId());
        }
        return callcenterTaskEduService.createOrUpdateStudentInvite(reqDMO);
    }

    /**
     * 获取咨询记录 暂时未使用
     *
     * @param studentAskReqDMO
     * @return
     */
    @RequestMapping(value = "/api/edu/business/callcenter/queryStudentAskPage")
    @ResponseBody
    public Object queryStudentAskPageByStudentAskReqDMO(String token, StudentAskReqDMO studentAskReqDMO,
                                                        @RequestParam(required = false, defaultValue = "10", value = "pageSize") Integer pageSize,
                                                        @RequestParam(required = false, defaultValue = "1", value = "currentPage") Integer currentPage) {
        String memberId = getMemberId(token);
        if (StringUtils.isEmpty(memberId)) {
            return error("nologin");
        }
        if (StringUtils.isBlank(studentAskReqDMO.getStudentId())) {
            return error("参数错误");
        }
        return callcenterTaskEduService.queryStudentAskPageByStudentAskReqDMO(studentAskReqDMO, pageSize, currentPage);
    }

    /**
     * 查询学生详情 - 基本信息
     *
     * @param token
     * @param studentId
     * @return
     */
    @RequestMapping(value = "/api/edu/business/callcenter/queryStudentInfoById")
    @ResponseBody
    public Object queryStudentInfoById(String token, String studentId) {
        String memberId = getMemberId(token);
        if (StringUtils.isEmpty(memberId)) {
            return error("nologin");
        }
        if (StringUtils.isBlank(studentId)) {
            return error("参数错误");
        }
        return studentEduService.queryStudentInfoById(studentId);
    }

    /**
     * 查询学生详情 - 参加活动
     *
     * @param token
     * @param studentId
     * @return
     */
    @RequestMapping(value = "/api/edu/business/callcenter/queryStudentActivityByStudentId")
    @ResponseBody
    public Object queryStudentActivityByStudentId(String token, String studentId) {
        String memberId = getMemberId(token);
        if (StringUtils.isEmpty(memberId)) {
            return error("nologin");
        }
        if (StringUtils.isBlank(studentId)) {
            return error("参数错误");
        }
        return studentEduService.queryStudentActivityByStudentId(studentId);
    }

    /**
     * 查询学生线下电咨情况
     *
     * @param token
     * @param askReqDMO
     * @return
     */
    @RequestMapping(value = "/api/edu/business/callcenter/queryStudentOfflinePconsult")
    @ResponseBody
    public Object queryStudentOfflinePconsult(String token, StudentAskReqDMO askReqDMO,
                                              @RequestParam(required = false, defaultValue = "10", value = "pageSize") Integer pageSize,
                                              @RequestParam(required = false, defaultValue = "1", value = "currentPage") Integer currentPage) {
        String memberId = getMemberId(token);
        if (StringUtils.isEmpty(memberId)) {
            return error("nologin");
        }
        if (StringUtils.isBlank(askReqDMO.getStudentId())) {
            return error("参数错误");
        }
        askReqDMO.setAskSource(CommonsStudentAskConstants.ASK_SOURCE_BRANCH);
        askReqDMO.setAskType(CommonsStudentAskConstants.ASK_TYPE_PCONSULT);
        return callcenterTaskEduService.queryStudentAskPageByStudentAskReqDMO(askReqDMO, pageSize, currentPage);
    }

    /**
     * 查询学生线下面咨情况
     *
     * @param token
     * @param askReqDMO
     * @return
     */
    @RequestMapping(value = "/api/edu/business/callcenter/queryStudentFconsult")
    @ResponseBody
    public Object queryStudentFconsult(String token, StudentAskReqDMO askReqDMO,
                                       @RequestParam(required = false, defaultValue = "10", value = "pageSize") Integer pageSize,
                                       @RequestParam(required = false, defaultValue = "1", value = "currentPage") Integer currentPage) {
        String memberId = getMemberId(token);
        if (StringUtils.isEmpty(memberId)) {
            return error("nologin");
        }
        if (StringUtils.isBlank(askReqDMO.getStudentId())) {
            return error("参数错误");
        }
        askReqDMO.setAskSource(CommonsStudentAskConstants.ASK_SOURCE_BRANCH);
        askReqDMO.setAskType(CommonsStudentAskConstants.ASK_TYPE_FCONSULT);
        return callcenterTaskEduService.queryStudentAskPageByStudentAskReqDMO(askReqDMO, pageSize, currentPage);
    }

    /**
     * 查询学生呼叫中心电咨情况
     *
     * @param token
     * @param askReqDMO
     * @return
     */
    @RequestMapping(value = "/api/edu/business/callcenter/queryStudentCallCenterConsult")
    @ResponseBody
    public Object queryStudentCallCenterConsult(String token, StudentAskReqDMO askReqDMO,
                                                @RequestParam(required = false, defaultValue = "10", value = "pageSize") Integer pageSize,
                                                @RequestParam(required = false, defaultValue = "1", value = "currentPage") Integer currentPage) {
        String memberId = getMemberId(token);
        if (StringUtils.isEmpty(memberId)) {
            return error("nologin");
        }
        if (StringUtils.isBlank(askReqDMO.getStudentId())) {
            return error("参数错误");
        }
        List<Integer> askSourceIn = Arrays.asList(CommonsStudentAskConstants.ASK_SOURCE_CALL_CENTER, CommonsStudentAskConstants.ASK_SOURCE_OUTBOUND_TASK);
        askReqDMO.setAskSourceIn(askSourceIn);
        askReqDMO.setAskType(CommonsStudentAskConstants.ASK_TYPE_PCONSULT);
        return callcenterTaskEduService.queryStudentAskPageByStudentAskReqDMO(askReqDMO, pageSize, currentPage);
    }

    /**
     * 外呼系统创建电资
     *
     * @param token
     * @param studentAskCreateDMO
     * @return
     * @author wangwenjun
     */
    @RequestMapping(value = "/api/edu/business/callcenter/createCallCenterPconsult", method = RequestMethod.POST)
    @ResponseBody
    public Object createCallCenterPconsult(String token, StudentAskCreateDMO studentAskCreateDMO) {
        String memberId = getMemberId(token);
        if (StringUtils.isEmpty(memberId)) {
            return error("nologin");
        }
        User user = userService.getUserByNickname(memberId);
        if (user == null) {
            return error("nologin");
        }
        if (StringUtils.isBlank(studentAskCreateDMO.getStudentId())) {
            return error("学生id不能为空");
        }
        studentAskCreateDMO.setCreateUid(user.getUserId());
        return callcenterTaskEduService.createCallCenterPconsult(studentAskCreateDMO);
    }

    /**
     * 外呼系统更新电资
     *
     * @param token
     * @param studentAskCreateDMO
     * @return
     * @author wangwenjun
     */
    @RequestMapping(value = "/api/edu/business/callcenter/updateCallCenterPconsult", method = RequestMethod.POST)
    @ResponseBody
    public Object updateCallCenterPconsult(String token, StudentAskCreateDMO studentAskCreateDMO) {
        String memberId = getMemberId(token);
        if (StringUtils.isEmpty(memberId)) {
            return error("nologin");
        }
        User user = userService.getUserByNickname(memberId);
        if (user == null) {
            return error("nologin");
        }
        if (StringUtils.isBlank(studentAskCreateDMO.getStudentAskId())) {
            return error("参数错误");
        }
        studentAskCreateDMO.setCreateUid(null);
        return callcenterTaskEduService.updateCallCenterPconsult(studentAskCreateDMO);
    }

    /**
     * 查询学生科目学习情况
     *
     * @param token
     * @param studentId
     * @param pageSize
     * @param currentPage
     * @return
     */
    @RequestMapping(value = "/api/edu/business/callcenter/queryStudentStudySituationPage", method = RequestMethod.GET)
    @ResponseBody
    public Object queryStudentCourseCategoryStudySituation(String token, String studentId,
                                                           @RequestParam(required = false, defaultValue = "10", value = "pageSize") Integer pageSize,
                                                           @RequestParam(required = false, defaultValue = "1", value = "currentPage") Integer currentPage) {
        String memberId = getMemberId(token);
        if (StringUtils.isEmpty(memberId)) {
            return error("nologin");
        }
        if (StringUtils.isBlank(studentId)) {
            return error("参数错误");
        }
        return studentEduService.queryStudentCourseCategoryStudySituation(studentId, pageSize, currentPage);
    }

    /**
     * 查询学生邀约记录
     *
     * @param token
     * @param studentId
     * @param pageSize
     * @param currentPage
     * @return
     */
    @RequestMapping(value = "/api/edu/business/callcenter/queryStudentInvitePage", method = RequestMethod.GET)
    @ResponseBody
    public Object queryStudentInvitePage(String token, String studentId,
                                         @RequestParam(required = false, defaultValue = "10", value = "pageSize") Integer pageSize,
                                         @RequestParam(required = false, defaultValue = "1", value = "currentPage") Integer currentPage) {
        String memberId = getMemberId(token);
        if (StringUtils.isEmpty(memberId)) {
            return error("nologin");
        }
        if (StringUtils.isBlank(studentId)) {
            return error("参数错误");
        }
        return callcenterTaskEduService.queryStudentInvitePage(studentId, pageSize, currentPage);
    }

    /**
     * 外呼任务学生分配外呼人员
     *
     * @param token
     * @param ids
     * @param userId
     * @return
     */
    @RequestMapping(value = "/api/edu/business/callcenter/assignStudentUser", method = RequestMethod.POST)
    @ResponseBody
    public Object assignCenterTaskStudentUser(String token, String ids, String userId) {
        String memberId = getMemberId(token);
        if (StringUtils.isEmpty(memberId)) {
            return error("nologin");
        }
        if (StringUtils.isBlank(userId) || StringUtils.isBlank(ids)) {
            return error("参数错误");
        }
        List<String> idList = Splitter.onPattern(",").omitEmptyStrings().splitToList(ids);
        if (idList == null || idList.isEmpty()) {
            return error("参数错误");
        }
        return callcenterTaskEduService.assignCenterTaskStudentUser(idList, userId);
    }

    /**
     * 外呼任务学生添加备注
     * @param token
     * @param callcenterTaskStudentId
     * @param remark
     * @return
     */
    @RequestMapping(value = "/api/edu/business/callcenter/updateCenterTaskStudentRemark", method = RequestMethod.POST)
    @ResponseBody
    public Object updateCenterTaskStudentRemark(String token, String callcenterTaskStudentId, String remark) {
        String memberId = getMemberId(token);
        if (StringUtils.isEmpty(memberId)) {
            return error("nologin");
        }
        if (StringUtils.isBlank(remark) || StringUtils.isBlank(callcenterTaskStudentId)) {
            return error("参数错误");
        }
        return callcenterTaskEduService.updateCenterTaskStudentRemark(callcenterTaskStudentId, remark);
    }

    /**
     * 外呼系统更新学生
     * @param token
     * @param studentInfoUpdateDMO
     * @return
     */
    @RequestMapping(value = "/api/edu/business/callcenter/updateStudentInfo", method = RequestMethod.POST)
    @ResponseBody
    public Object updateStudentInfo(String token, StudentInfoUpdateDMO studentInfoUpdateDMO) {
        if (StringUtils.isBlank(studentInfoUpdateDMO.getStudentId())) {
            return error("参数错误");
        }
        try {
            studentInfoUpdateDMO = setNullValue(studentInfoUpdateDMO);
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
        studentInfoUpdateDMO.setModifyUid(user.getUserId());
        return studentEduService.updateStudentInfo(studentInfoUpdateDMO);
    }
}
