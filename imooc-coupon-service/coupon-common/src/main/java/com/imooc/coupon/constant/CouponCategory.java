package com.imooc.coupon.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;
import java.util.stream.Stream;

@Getter
@AllArgsConstructor
public enum CouponCategory {

    MANJIAN("满减券", "001"),
    ZHEKOU("折扣券", "002"),
    LIJIAN("立减券", "003")
    ;

    // 优惠券描述
    private String description;
    // 分类编码
    private String code;


    public static CouponCategory of(String code){
        Objects.requireNonNull(code);
        return Stream.of(values())// 用枚举的静态方法构造stream
                .filter(bean -> bean.code.equals(code))// 中间操作，过滤重新生成一个stream
                .findAny()// 终端操作，匹配所有值
                .orElseThrow(() -> new IllegalArgumentException(code + " not exists"));// 为null则抛出异常
    }

}
