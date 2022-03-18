package com.imooc.coupon.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * 优惠券有效期
 */
@Getter
@AllArgsConstructor
public enum PeriodType {

    REGULAR("固定的（固定日期）", 1),
    SHIFT("变动的（以领取之日开始计算）", 2),
    ;

    // 描述
    private String description;
    // 编码
    private Integer code;

    public static PeriodType of(Integer code){
        Objects.requireNonNull(code);
        return Stream.of(values())// 用枚举的静态方法构造stream
                .filter(bean -> bean.code.equals(code))// 中间操作，过滤重新生成一个stream
                .findAny()// 终端操作，匹配所有值
                .orElseThrow(() -> new IllegalArgumentException(code + " not exists"));// 为null则抛出异常
    }


}
