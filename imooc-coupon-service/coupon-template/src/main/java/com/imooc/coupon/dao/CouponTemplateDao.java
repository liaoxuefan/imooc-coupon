package com.imooc.coupon.dao;

import com.imooc.coupon.entity.CouponTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CouponTemplateDao extends JpaRepository<CouponTemplate, Integer> {

    // 根据模板名称查询
    CouponTemplate findByName(String name);

    // where available=xxx and expired=xxx
    List<CouponTemplate> findAllByAvailableAndExpired(Boolean available, Boolean expired);

    // where expired=xxx
    List<CouponTemplate> findAllByExpired(Boolean expired);
}
