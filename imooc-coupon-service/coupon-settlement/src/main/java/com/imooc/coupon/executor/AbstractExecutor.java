package com.imooc.coupon.executor;

import com.alibaba.fastjson.JSON;
import com.imooc.coupon.vo.GoodsInfo;
import com.imooc.coupon.vo.SettlementInfo;
import org.apache.commons.collections4.CollectionUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 规则处理抽象类，定义通用方法
 */
public abstract class AbstractExecutor {

    // 校验商品类型与优惠券是否匹配
    // 这里校验单品类的优惠券，多品类的自行重载
    protected boolean isGoodsTypeSatisfy(SettlementInfo settlement){

        // 获取结算信息中所有商品的类型
        List<Integer> goodsTypes = settlement.getGoodsInfos().stream().map(GoodsInfo::getType).collect(Collectors.toList());

        // 获取结算信息中优惠券模板中适用的商品类型，单品类只有一张优惠券
        List<Integer> templateGoodsType = JSON.parseObject(
                settlement.getCouponAndTemplateInfos().get(0).getTemplateSDK()
                        .getRule().getUsage().getGoodsType(),
                List.class
        );

        // 返回二者是否存在交集CollectionUtils.intersection
        return !CollectionUtils.intersection(goodsTypes, templateGoodsType).isEmpty();

    }

    // 处理商品类型与优惠券限制不匹配的情况
    // 返回修改过的优惠券结算信息
    protected SettlementInfo processGoodsTypeNotSatisfy(SettlementInfo settlement, double goodsSum){

        // 校验商品类型与优惠券是否匹配
        if(!isGoodsTypeSatisfy(settlement)){
            // 不匹配则将cost设置为goodsSum,优惠券列表置空Collections.emptyList(),return
            settlement.setCost(goodsSum);
            settlement.setCouponAndTemplateInfos(Collections.emptyList());
            return settlement;
        }

        return null;

    }

    // 计算商品的总价
    protected double goodsCostSum(List<GoodsInfo> goodsInfos){

        return goodsInfos.stream().mapToDouble(g -> g.getPrice() * g.getCount()).sum();
    }

    // 保留两位小数
    protected double retain2Decimals(double value) {
        return new BigDecimal(value).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    // 返回最小支付费用
    protected double minCost(){
        return 0.0;
    }

}
