package com.imooc.coupon.executor;


import com.imooc.coupon.constant.RuleFlag;
import com.imooc.coupon.vo.SettlementInfo;

/**
 * 优惠券模板规则处理器
 */
public interface RuleExecutor {

    // 标记使用的规则
    RuleFlag ruleConfig();

    // 规则的计算，返回修正的结算信息
    SettlementInfo computeRule(SettlementInfo settlementInfo);
}
