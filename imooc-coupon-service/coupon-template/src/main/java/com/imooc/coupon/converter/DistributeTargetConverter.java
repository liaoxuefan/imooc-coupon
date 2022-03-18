package com.imooc.coupon.converter;

import com.imooc.coupon.constant.DistributeTarget;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class DistributeTargetConverter implements AttributeConverter<DistributeTarget, Integer> {
    @Override
    // 将实体属性CouponCategory转换成String，插入和更新时
    public Integer convertToDatabaseColumn(DistributeTarget target) {
        return target.getCode();
    }

    @Override
    public DistributeTarget convertToEntityAttribute(Integer i) {
        return DistributeTarget.of(i);
    }
//
}
