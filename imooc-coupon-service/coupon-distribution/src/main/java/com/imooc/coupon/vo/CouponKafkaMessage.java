package com.imooc.coupon.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CouponKafkaMessage {

    // 要保存的状态
    private Integer status;

    // 需要消费的优惠券id
    private List<Integer> ids;
}
