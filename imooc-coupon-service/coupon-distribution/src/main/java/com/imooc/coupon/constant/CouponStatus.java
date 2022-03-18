package com.imooc.coupon.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;
import java.util.stream.Stream;

@Getter
@AllArgsConstructor
public enum CouponStatus {

    USABLE("可用的", 1),
    USED("已使用的", 2),
    EXPIRED("过期的（未被使用的）", 3),
    ;

    private String description;

    private Integer code;

    public static CouponStatus of(Integer code){
        Objects.requireNonNull(code);
        return Stream.of(values())
                .filter(c -> code.equals(c.getCode()))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("优惠券状态码错误"));
    }

}
