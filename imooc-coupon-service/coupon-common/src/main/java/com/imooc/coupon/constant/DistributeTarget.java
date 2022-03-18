package com.imooc.coupon.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * 分发目标
 */
@Getter
@AllArgsConstructor
public enum DistributeTarget {

    SINGLE("单用户", 1),// 需要自己去领取
    MULTI("多用户", 2),// 系统发放
    ;

    // 描述
    private String description;
    // 编码
    private Integer code;

    public static DistributeTarget of(Integer code){
        Objects.requireNonNull(code);
        return Stream.of(values())// 用枚举的静态方法构造stream
                .filter(bean -> bean.code.equals(code))// 中间操作，过滤重新生成一个stream
                .findAny()// 终端操作，匹配所有值
                .orElseThrow(() -> new IllegalArgumentException(code + " not exists"));// 为null则抛出异常
    }


}
