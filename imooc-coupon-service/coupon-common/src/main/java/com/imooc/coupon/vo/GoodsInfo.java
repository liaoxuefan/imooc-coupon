package com.imooc.coupon.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商品信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GoodsInfo {

    // 关联GoodsType
    private Integer type;

    private Double price;

    private Integer count;
}
