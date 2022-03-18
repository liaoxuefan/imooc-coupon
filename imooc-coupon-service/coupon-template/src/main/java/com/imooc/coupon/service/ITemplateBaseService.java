package com.imooc.coupon.service;

import com.imooc.coupon.entity.CouponTemplate;
import com.imooc.coupon.exception.CouponException;
import com.imooc.coupon.vo.CouponTemplateSDK;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 优惠券模板基础服务
 */
public interface ITemplateBaseService {

    // 根据id返回优惠券模板信息
    CouponTemplate buildTemplateInfo(Integer id) throws CouponException;

    // 查找所有可用的优惠券模板
    List<CouponTemplateSDK> findAllUsableTemplate();

    // 获取模板id到SDK的映射
    Map<Integer, CouponTemplateSDK> findIds2TemplateSDK(Collection<Integer> ids);

}