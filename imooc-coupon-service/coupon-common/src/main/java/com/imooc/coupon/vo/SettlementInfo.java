package com.imooc.coupon.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SettlementInfo {

    private Long userId;

    // 要结算的商品
    private List<GoodsInfo> goodsInfos;

    // 要使用的优惠券及模板
    private List<CouponAndTemplateInfo> couponAndTemplateInfos;

    // 结算金额
    private Double cost;

    // 是否核销
    private Boolean employ;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CouponAndTemplateInfo{

        private Integer id; // 优惠券id
        //
        private CouponTemplateSDK templateSDK;
    }
}
