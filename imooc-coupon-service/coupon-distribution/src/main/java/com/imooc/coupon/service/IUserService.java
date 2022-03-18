package com.imooc.coupon.service;

import com.imooc.coupon.entity.Coupon;
import com.imooc.coupon.exception.CouponException;
import com.imooc.coupon.vo.AcquireTemplateRequest;
import com.imooc.coupon.vo.CouponTemplateSDK;
import com.imooc.coupon.vo.SettlementInfo;

import java.util.List;

public interface IUserService {

    /**
     * 查找优惠券信息
     * @param userId 用户id
     * @param status 优惠券状态
     * @return
     */
    List<Coupon> findCouponByStatus(Long userId, Integer status) throws CouponException;

    /**
     * 查找当前用户可以领取的优惠券模板
     * @param userId
     * @return
     */
    List<CouponTemplateSDK> findAvailableTemplate(Long userId) throws CouponException;

    /**
     * 用户领取优惠券
     * @param request 请求参数
     * @return
     */
    Coupon acquireTemplate(AcquireTemplateRequest request) throws CouponException;

    /**
     * 用户消费优惠券
     * @param info
     * @return
     * @throws CouponException
     */
    SettlementInfo settlement(SettlementInfo info) throws CouponException;
}
