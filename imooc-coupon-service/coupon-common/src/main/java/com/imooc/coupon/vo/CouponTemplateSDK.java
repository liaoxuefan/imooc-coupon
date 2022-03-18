package com.imooc.coupon.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 微服务之间调用的优惠券模板信息，用于外部展示相较于CouponTemplate，也可以直接用CouponTemplate
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CouponTemplateSDK {
    private Integer id;

    private String name;

    private String logo;

    private String desc;

    private String category;

    private Integer productLine;

    private String key;

    private Integer target;

    private TemplateRule rule;


}
