package com.imooc.coupon.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;
import java.util.stream.Stream;

@Getter
@AllArgsConstructor
public enum ProductLine {

    SHOUJI("手机", 1),
    DIANNAO("电脑", 2),
    ;

    // 产品描述
    private String description;
    // 编码
    private Integer code;

    public static ProductLine of(Integer code){
        Objects.requireNonNull(code);
        return Stream.of(values())// 用枚举的静态方法构造stream
                .filter(bean -> bean.code.equals(code))// 中间操作，过滤重新生成一个stream
                .findAny()// 终端操作，匹配所有值
                .orElseThrow(() -> new IllegalArgumentException(code + " not exists"));// 为null则抛出异常
    }


}
