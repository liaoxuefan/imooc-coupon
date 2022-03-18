package com.imooc.coupon.service;

import com.imooc.coupon.entity.CouponTemplate;
import com.imooc.coupon.exception.CouponException;
import com.imooc.coupon.vo.TemplateRequest;

public interface IBuildTemplateService {
    // 创建优惠券模板
    CouponTemplate buildTemplate(TemplateRequest request) throws CouponException;
}
