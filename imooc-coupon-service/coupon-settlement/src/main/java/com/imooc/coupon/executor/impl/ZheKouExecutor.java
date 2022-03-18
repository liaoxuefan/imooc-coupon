package com.imooc.coupon.executor.impl;


import com.imooc.coupon.constant.RuleFlag;
import com.imooc.coupon.executor.AbstractExecutor;
import com.imooc.coupon.executor.RuleExecutor;
import com.imooc.coupon.vo.SettlementInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ZheKouExecutor extends AbstractExecutor implements RuleExecutor {


    @Override
    public RuleFlag ruleConfig() {
        return RuleFlag.ZHEKOU;
    }

    @Override
    public SettlementInfo computeRule(SettlementInfo settlement) {


        // 计算商品总价并保留两位小数，使用抽象类中的通用方法
        double sum = retain2Decimals(goodsCostSum(settlement.getGoodsInfos()));

        // 查看商品类型与优惠券限制是否匹配，结果不为空则return返回结果
        if(processGoodsTypeNotSatisfy(settlement, sum) != null){
            return processGoodsTypeNotSatisfy(settlement,sum);
        }

        // 判断折扣是否符合折扣标准
        // 1、获取结算信息中的优惠券模板，get(0)
        // 2、sum*quota/100 判断是否大于最小支付费用，否则返回最小支付费用
        SettlementInfo.CouponAndTemplateInfo couponAndTemplateInfo = settlement.getCouponAndTemplateInfos().get(0);
        double base = (double)couponAndTemplateInfo.getTemplateSDK().getRule().getDiscount().getBase();
        double quota = (double)couponAndTemplateInfo.getTemplateSDK().getRule().getDiscount().getQuota();



        settlement.setCost((retain2Decimals(sum*quota/100) < minCost()) ? minCost() : retain2Decimals(sum*quota/100));
        log.debug("Use zheKou From coupon {} to user {}", couponAndTemplateInfo.getId(), settlement.getUserId());
        return settlement;
    }
}
