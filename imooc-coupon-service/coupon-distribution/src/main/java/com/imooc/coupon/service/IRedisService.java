package com.imooc.coupon.service;

import com.imooc.coupon.entity.Coupon;
import com.imooc.coupon.exception.CouponException;

import java.util.List;

public interface IRedisService {

    /**
     * 在cache中查找优惠券信息
     * @param userId 用户id
     * @param status 优惠券状态
     * @return
     */
    List<Coupon> getCacheCoupon(Long userId, Integer status);

    /**
     * 保存空的优惠券信息到cache，避免缓存穿透：查完缓存没有数据，再到数据库查还是没有数据
     * @param userId 用户id
     * @param status 优惠券状态
     */
    void saveEmptyCouponListToCache(Long userId, List<Integer> status);

    /**
     * 尝试从cache中获取优惠券码
     * @param templateId 优惠券模板id
     * @return
     */
    String tryToAcquireCouponCodeFromCache(Integer templateId);

    /**
     * 将优惠券信息放入缓存
     * @param userId 用户id
     * @param coupons 优惠券list
     * @param status 指定优惠券的状态
     * @return
     */
    Integer addCouponToCache(Long userId, List<Coupon> coupons, Integer status) throws CouponException;
}
