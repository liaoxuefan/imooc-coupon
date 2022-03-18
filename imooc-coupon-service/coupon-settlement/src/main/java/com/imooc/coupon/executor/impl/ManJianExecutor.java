package com.imooc.coupon.executor.impl;

import com.imooc.coupon.constant.RuleFlag;
import com.imooc.coupon.executor.AbstractExecutor;
import com.imooc.coupon.executor.RuleExecutor;
import com.imooc.coupon.vo.SettlementInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@Slf4j
public class ManJianExecutor extends AbstractExecutor implements RuleExecutor {

    // 标记使用哪种规则
    public RuleFlag ruleConfig(){
        return RuleFlag.MANJIAN;
    }

    // 规则的计算，返回修正过的结算信息
    public SettlementInfo computeRule(SettlementInfo settlement){

        // 计算商品总价并保留两位小数，使用抽象类中的通用方法
        double sum = retain2Decimals(goodsCostSum(settlement.getGoodsInfos()));

        // 查看商品类型与优惠券限制是否匹配，结果不为空则return返回结果
        if(processGoodsTypeNotSatisfy(settlement, sum) != null){
            return processGoodsTypeNotSatisfy(settlement,sum);
        }

        // 判断满减是否符合满减标准
        // 1、获取结算信息中的优惠券模板，get(0)
        // 2、如果商品总价<base则直接返回商品总价并将优惠券模板置空
        // 3、符合base则减去quota，判断是否大于最小支付费用，否则返回最小支付费用
        SettlementInfo.CouponAndTemplateInfo couponAndTemplateInfo = settlement.getCouponAndTemplateInfos().get(0);
        double base = (double)couponAndTemplateInfo.getTemplateSDK().getRule().getDiscount().getBase();
        double quota = (double)couponAndTemplateInfo.getTemplateSDK().getRule().getDiscount().getQuota();

        if(sum < base){
            settlement.setCost(sum);
            settlement.setCouponAndTemplateInfos(Collections.emptyList());
            return settlement;
        }

        settlement.setCost((sum-quota < minCost()) ? minCost() : retain2Decimals(sum-quota));
        log.debug("Use manJian From coupon {} to user {}", couponAndTemplateInfo.getId(), settlement.getUserId());
        return settlement;
    }
}
