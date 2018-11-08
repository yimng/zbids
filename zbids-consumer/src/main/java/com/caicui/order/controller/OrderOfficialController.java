package com.caicui.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.caicui.commons.api.controller.ApiCommonController;
import com.caicui.order.commons.CommonsOrderConstants;
import com.edu.commons.utils.JsonResult;
import com.edu.commons.utils.PageResult;
import com.edu.dubbo.order.service.OrderOfficialService;
import com.edu.order.dmo.OrderListRspDMO;
import com.edu.order.vo.OrderCostVo;
import com.edu.order.vo.OrderResultVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 官网订单
 * Title
 *
 * @author: xuJianFeng
 * @date: 2018/8/31 14:22
 **/
@Controller
public class OrderOfficialController extends ApiCommonController {

    @Reference
    private OrderOfficialService orderOfficialService;

    /**
     * 创建官网订单
     *
     * @param token                      用户信息
     * @param isNeedInvoice              是否需要发票，1是、0否
     * @param totalAmount                订单实际金额
     * @param totalProductDiscountAmount 订单折扣优惠金额
     * @param totalDiscountAmount        订单整单优惠金额
     * @param orderProduct               商品信息json，包含
     *                                   productId商品id
     *                                   productPriceId商品定价id
     *                                   productAmount商品实际金额
     *                                   discountRateAmount商品折扣优惠金额
     *                                   productDiscountAmount商品整单优惠金额（整单优惠等比例折扣后金额）
     * @param invoiceAmount              发票金额
     * @param invoiceName                发票名称
     * @param isPerInvoice               是否个人，1是、0否
     * @param invoiceConent              发票内容
     * @param consigneeProvince          收货人省份
     * @param consigneeCity              收货人城市
     * @param consigneeAddress           收货人详细地址
     * @param consigneeName              收货人名称
     * @param consigneeMobile            收货人电话
     * @param remark                     备注，用于存放订单所使用的优惠规则名称
     * @return 是否创建成功
     */
    @RequestMapping(value = "/api/edu/order/createOrderByOfficial", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> createOrderByOfficial(String token,
                                                     Integer isNeedInvoice,
                                                     BigDecimal totalAmount,
                                                     BigDecimal totalProductDiscountAmount,
                                                     BigDecimal totalDiscountAmount,
                                                     String orderProduct,
                                                     BigDecimal invoiceAmount,
                                                     String invoiceName,
                                                     Integer isPerInvoice,
                                                     String invoiceConent,
                                                     String consigneeProvince,
                                                     String consigneeCity,
                                                     String consigneeAddress,
                                                     String consigneeName,
                                                     String consigneeMobile,
                                                     String remark) {
        String memberId = getMemberId(token);
        if (StringUtils.isBlank(memberId)) {
            return error("未登录");
        }
        //数据判断，用户、分期、商品信息必填
        if (StringUtils.isBlank(orderProduct) ||
                isNeedInvoice == null ||
                totalAmount == null ||
                totalProductDiscountAmount == null ||
                totalDiscountAmount == null ||
                (isNeedInvoice == CommonsOrderConstants.IS_NEED_INVOICE_YES &&
                        (invoiceAmount == null ||
                                StringUtils.isBlank(invoiceName) ||
                                isPerInvoice == null ||
                                StringUtils.isBlank(invoiceConent) ||
                                StringUtils.isBlank(consigneeProvince) ||
                                StringUtils.isBlank(consigneeCity) ||
                                StringUtils.isBlank(consigneeAddress) ||
                                StringUtils.isBlank(consigneeName) ||
                                StringUtils.isBlank(consigneeMobile)))) {
            return error("参数错误");
        }
        JsonResult<Map<String, Object>> jsonResult = orderOfficialService.createOrderByOfficial(memberId,
                isNeedInvoice,
                totalAmount,
                totalProductDiscountAmount,
                totalDiscountAmount,
                orderProduct,
                invoiceAmount,
                invoiceName,
                isPerInvoice,
                invoiceConent,
                consigneeProvince,
                consigneeCity,
                consigneeAddress,
                consigneeName,
                consigneeMobile,
                remark,
                memberId);
        if (!jsonResult.isSuccess()) {
            return error(jsonResult.getMsg());
        }
        return success(jsonResult.getData());
    }

    /**
     * 官网订单-废弃订单
     *
     * @param token   用户信息
     * @param orderId 需要废弃的订单id
     * @return 废弃是否成功
     */
    @RequestMapping(value = "/api/edu/order/abandonedOrderByOfficial", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> abandonedOrderByOfficial(String token,
                                                        String orderId) {
        String memberId = getMemberId(token);
        if (StringUtils.isBlank(memberId)) {
            return error("未登录");
        }
        if (StringUtils.isBlank(orderId)) {
            return error("参数错误");
        }
        JsonResult<Integer> jsonResult = orderOfficialService.abandonedOrderByOfficial(orderId);
        if (!jsonResult.isSuccess()) {
            return error(jsonResult.getMsg());
        }
        return success(jsonResult.getData());
    }

    /**
     * 官网个人订单列表
     *
     * @param token       用户信息
     * @param status      订单状态，暂定状态为：0全部，1未支付，2部分支付，3已支付，4废弃，5关闭。
     * @param orderSn     订单编号
     * @param startAmount 金额范围，开始范围
     * @param endAmount   金额范围，结束范围
     * @param startDate   下单时间，开始时间
     * @param endDate     下单时间，结束时间
     * @param dateScope   日期条件(1,一周；2一个月；3三个月；4六个月；5一年)
     * @param productName 商品名称
     * @param currentPage 页索引
     * @param pageSize    页大小
     * @return 订单列表
     */
    @RequestMapping(value = "/api/edu/order/queryListByOfficial", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> queryListByOfficial(String token,
                                                   Integer status,
                                                   String orderSn,
                                                   BigDecimal startAmount,
                                                   BigDecimal endAmount,
                                                   Date startDate,
                                                   Date endDate,
                                                   Integer dateScope,
                                                   String productName,
                                                   int currentPage,
                                                   int pageSize) {
        String memberId = getMemberId(token);
        if (StringUtils.isBlank(memberId)) {
            return error("未登录");
        }
        JsonResult<PageResult> jsonResult = orderOfficialService.queryListByOfficial(status,
                memberId,
                orderSn,
                startAmount,
                endAmount,
                startDate,
                endDate,
                dateScope,
                productName,
                currentPage,
                pageSize);
        if (!jsonResult.isSuccess()) {
            return error(jsonResult.getMsg());
        }
        return success(jsonResult.getData());
    }

    /**
     * 官网订单-订单详情
     *
     * @param token   用户信息
     * @param orderSn 订单编号
     * @return 订单信息
     */
    @RequestMapping(value = "/api/edu/order/getOrderInforByOfficial", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> getOrderInforByOfficial(String token,
                                                       String orderSn) {
        String memberId = getMemberId(token);
        if (StringUtils.isBlank(memberId)) {
            return error("未登录");
        }
        if (StringUtils.isBlank(orderSn)) {
            return error("参数错误");
        }
        JsonResult<OrderResultVo> jsonResult = orderOfficialService.getOrderInforByOfficial(orderSn);
        if (!jsonResult.isSuccess()) {
            return error(jsonResult.getMsg());
        }
        return success(jsonResult.getData());
    }

    /**
     * 官网订单转账
     *
     * @param token       用户信息
     * @param orderIds    订单ids
     * @param money       转账金额
     * @param file        附件
     * @param remark      备注
     * @param payDate     转账日期
     * @param transferMan 转账人
     * @return 转账状态
     */
    @RequestMapping(value = "/api/edu/order/transferByOfficial", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> transferByOfficial(String token,
                                                  String orderIds,
                                                  BigDecimal money,
                                                  String file,
                                                  String remark,
                                                  Date payDate,
                                                  String transferMan) {
        String memberId = getMemberId(token);
        if (StringUtils.isBlank(memberId)) {
            return error("未登录");
        }
        JsonResult<Integer> jsonResult = orderOfficialService.transferByOfficial(orderIds,
                money,
                file,
                remark,
                payDate,
                transferMan,
                memberId
        );
        if (!jsonResult.isSuccess()) {
            return error(jsonResult.getMsg());
        }
        return success(jsonResult.getData());
    }

    /**
     * 根据订单商品信息获取在学科目信息
     *
     * @param token      用户信息
     * @param productIds 订单商品ids
     * @return 在学科目提示
     */
    @RequestMapping(value = "/api/edu/order/checkCourseIsStudyByOfficial", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> checkCourseIsStudyByOfficial(String token,
                                                            String productIds) {
        String memberId = getMemberId(token);
        if (StringUtils.isBlank(memberId)) {
            return error("未登录");
        }
        if (StringUtils.isBlank(productIds)) {
            return error("参数错误");
        }
        JsonResult<Map<String, Object>> jsonResult = orderOfficialService.checkCourseIsStudyByOfficial(memberId,
                productIds);
        if (!jsonResult.isSuccess()) {
            return error(jsonResult.getMsg());
        }
        return success(jsonResult.getData());
    }

    /**
     * 订单缴费详情
     *
     * @param token   用户信息
     * @param orderId 订单id
     * @return 缴费详情
     */
    @RequestMapping(value = "/api/edu/order/queryOrderByOfficialPayment", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> queryOrderByOfficialPayment(String token,
                                                           String orderId) {
        String memberId = getMemberId(token);
        if (StringUtils.isBlank(memberId)) {
            return error("未登录");
        }
        if (StringUtils.isBlank(orderId)) {
            return error("参数错误");
        }
        JsonResult<OrderCostVo> jsonResult = orderOfficialService.queryOrderByOfficialPayment(orderId,
                memberId);
        if (!jsonResult.isSuccess()) {
            return error(jsonResult.getMsg());
        }
        return success(jsonResult.getData());
    }

    /**
     * @author wangwenjun
     * @param token
     * @param orderId
     * @return
     */
    @RequestMapping(value = "/api/edu/order/queryOrderInfoById", method = RequestMethod.GET)
    @ResponseBody
    public Object queryOrderInfoRspDMOById(String token,String orderId){
        String memberId = getMemberId(token);
        if (StringUtils.isBlank(memberId)) {
            return error("未登录");
        }
        if (StringUtils.isBlank(orderId)) {
            return error("参数错误");
        }
        return orderOfficialService.queryOrderInfoRspDMOById(orderId);
    }


    @RequestMapping(value = "/api/edu/order/selectOrderListByStudentId", method = RequestMethod.GET)
    @ResponseBody
    public Object  selectOrderListByStudentId(String token,String studentId){
        String memberId = getMemberId(token);
        if (StringUtils.isBlank(memberId)) {
            return error("未登录");
        }
        if (StringUtils.isBlank(studentId)) {
            return error("参数错误");
        }
        return orderOfficialService.selectOrderListByStudentId(studentId);
    }
}
