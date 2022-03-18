package com.imooc.coupon.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * 商品类型
 */
@Getter
@AllArgsConstructor
public enum GoodsType{
    WENYU("文娱", 1),
    JIADIAN("家电", 2),
    OTHERS("其他", 3),
    ALL("全品类", 4),

    ;

    private String description;

    private Integer code;

    public static GoodsType of(Integer code){
        Objects.requireNonNull(code);
        return Stream.of(values())
                .filter(c -> code.equals(c.getCode()))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("商品类型错误"));
    }
}
