package com.imooc.coupon.executor.impl;

import com.alibaba.fastjson.JSON;
import com.imooc.coupon.constant.CouponCategory;
import com.imooc.coupon.constant.RuleFlag;
import com.imooc.coupon.executor.AbstractExecutor;
import com.imooc.coupon.executor.RuleExecutor;
import com.imooc.coupon.vo.GoodsInfo;
import com.imooc.coupon.vo.SettlementInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 先满减后折扣
 */
@Component
@Slf4j
public class ManJianZheKouExecutor extends AbstractExecutor implements RuleExecutor {
    @Override
    public RuleFlag ruleConfig() {
        return RuleFlag.MANJIAN_ZHEKOU;
    }


    // 校验商品类型与优惠券是否匹配(多品类)
    protected boolean isGoodsTypeSatisfy(SettlementInfo settlement){

        // 获取结算信息中所有商品的类型,goodsTypes去重
        // 如果想要使用多类优惠券, 则必须要所有的商品类型都包含在优惠券适用的商品类型内, 即差集为空,
        // 举个例子（a,a,b）-(a,b,c)不为空，按照你的业务思想来说，a和b都包含在优惠券适用的商品类型内，应该返回true，我觉得取差集之间应该对list去重
        List<Integer> goodsTypes = settlement.getGoodsInfos().stream().map(GoodsInfo::getType).distinct().collect(Collectors.toList());
        // 获取结算信息中优惠券模板中适用的商品类型
        List<Integer> templateGoodsTypes = new ArrayList<>();
        settlement.getCouponAndTemplateInfos().stream().forEach(m ->
                templateGoodsTypes.addAll(JSON.parseObject(m.getTemplateSDK().getRule().getUsage().getGoodsType(), List.class)));
        // 返回二者是否存在差集CollectionUtils.subtract(1,2),必须所有商品类型包含在优惠券适用的商品类型中

        return CollectionUtils.isEmpty(CollectionUtils.subtract(goodsTypes, templateGoodsTypes));
    }

    // 规则的计算，返回修正过的结算信息
    public SettlementInfo computeRule(SettlementInfo settlement){

        // 计算商品总价并保留两位小数，使用抽象类中的通用方法
        double sum = retain2Decimals(goodsCostSum(settlement.getGoodsInfos()));
        // 查看商品类型与优惠券限制是否匹配，结果不为空则return返回结果
        if(processGoodsTypeNotSatisfy(settlement, sum) != null){
            return processGoodsTypeNotSatisfy(settlement, sum);
        }
        // 识别两张优惠券并赋值给变量
        SettlementInfo.CouponAndTemplateInfo manJian = null;
        SettlementInfo.CouponAndTemplateInfo zheKou = null;

        for(SettlementInfo.CouponAndTemplateInfo info : settlement.getCouponAndTemplateInfos()){
            if(info.getTemplateSDK().getCategory().equals(CouponCategory.ZHEKOU.getCode())){
                zheKou = info;
            }else{
                manJian = info;
            }
        }


        // 当两者不能一起使用时，清空优惠券，设置商品的原价后return
        if(!isTemplateCanShared(manJian, zheKou)){
            settlement.setCost(sum);
            settlement.setCouponAndTemplateInfos(Collections.emptyList());
            return settlement;
        }
        // 对优惠规则进行计算（先满减后折扣），用一个List<SettlementInfo.CouponAndTemplateInfo> ctInfos记录使用了的优惠券
        List<SettlementInfo.CouponAndTemplateInfo> ctInfos = new ArrayList<>();
        double base = (double)manJian.getTemplateSDK().getRule().getDiscount().getBase();
        double quota = (double)manJian.getTemplateSDK().getRule().getDiscount().getQuota();

        if(sum >= base){
            ctInfos.add(manJian);
        }

        double afterSum = sum-quota;

        quota = (double)zheKou.getTemplateSDK().getRule().getDiscount().getQuota();

        if(afterSum > 0){
            ctInfos.add(zheKou);
        }

        // 设置结算后的价格到cost，不能小于minCost
        settlement.setCost((retain2Decimals(afterSum*quota/100) < minCost()) ? minCost() : retain2Decimals(afterSum*quota/100));
        settlement.setCouponAndTemplateInfos(ctInfos);

        log.debug("Use manJian and zheKou From coupon {},{} to user {}", manJian.getId(), zheKou.getId(), settlement.getUserId());
        return settlement;

    }

    // 去校验当前的两张优惠券是否可用共用,即weight是否满足条件
    // weight是一个json字符串包含了所有能与其共用的优惠券模板的编码
    private boolean isTemplateCanShared(SettlementInfo.CouponAndTemplateInfo manJian, SettlementInfo.CouponAndTemplateInfo zheKou){

        // 获取优惠券模板的编码
        String manJianKey = manJian.getTemplateSDK().getKey() + String.format("%04d", manJian.getTemplateSDK().getId());
        String zheKouKey = manJian.getTemplateSDK().getKey() + String.format("%04d", manJian.getTemplateSDK().getId());

        // List<String>保存manJianKey，包括其weight中的key
        List<String> manJianList = Collections.singletonList(manJianKey);
        manJianList.addAll(JSON.parseObject(manJian.getTemplateSDK().getRule().getWeight(), List.class));
        // List<String>保存zheKouKey，包括其weight中的key
        List<String> zheKouList = Collections.singletonList(zheKouKey);
        zheKouList.addAll(JSON.parseObject(zheKou.getTemplateSDK().getRule().getWeight(), List.class));
        // 返回manJianKey和zheKouKey是否分别是List<String>元素的子集
        return CollectionUtils.isSubCollection(Arrays.asList(manJianKey, zheKouKey), manJianList) ||
                CollectionUtils.isSubCollection(Arrays.asList(manJianKey, zheKouKey), zheKouList);

    }
}
