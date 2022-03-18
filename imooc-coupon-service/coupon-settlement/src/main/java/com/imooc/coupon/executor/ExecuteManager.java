package com.imooc.coupon.executor;

import com.imooc.coupon.constant.CouponCategory;
import com.imooc.coupon.constant.RuleFlag;
import com.imooc.coupon.exception.CouponException;
import com.imooc.coupon.vo.SettlementInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 根据结算信息中的结算规则获取对应的规则执行器
 */
@Component
@Slf4j
// 后置处理器，所有bean创建后执行
public class ExecuteManager implements BeanPostProcessor {

    // 规则执行器映射
    private static Map<RuleFlag, RuleExecutor> executorMap = new HashMap<>();

    /**
     * bean初始化前执行
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {

        if(!(bean instanceof RuleExecutor)){
            return bean;
        }
        RuleExecutor executor = (RuleExecutor)bean;
        RuleFlag ruleFlag = executor.ruleConfig();
        // 如果存在规则执行器则抛出异常避免重复注册
        if(executorMap.containsKey(ruleFlag)){
            throw new IllegalStateException(ruleFlag.getDescription()+" already exist");
        }
        executorMap.put(ruleFlag, executor);
        return null;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

        return bean;
    }

    /**
     * 优惠券结算规则入口，传过来的优惠券个数不能为0
     * @param settlement
     * @return
     * @throws CouponException
     */
    public SettlementInfo computeRule(SettlementInfo settlement) throws CouponException{
        SettlementInfo result = null;
        // 单类优惠券
        if(settlement.getCouponAndTemplateInfos().size() == 1){
            // 获取优惠券类别
            CouponCategory category = CouponCategory.of(settlement.getCouponAndTemplateInfos().get(0).getTemplateSDK().getCategory());
            // 根据类别switch进入case给result赋值executorIndex.get(RuleFlag.LIJIAN).computeRule(settlement);
            switch (category) {
                case MANJIAN:
                    result = executorMap.get(RuleFlag.MANJIAN).computeRule(settlement);
                    break;
                case ZHEKOU:
                    result = executorMap.get(RuleFlag.ZHEKOU).computeRule(settlement);
                    break;
                case LIJIAN:
                    result = executorMap.get(RuleFlag.LIJIAN).computeRule(settlement);
                    break;
                default:
            }
        }else{

            // 结算信息中的优惠券种类List<CouponCategory>
            List<CouponCategory> categoryList = new ArrayList<>();
            for(SettlementInfo.CouponAndTemplateInfo info : settlement.getCouponAndTemplateInfos()){
                categoryList.add(CouponCategory.of(info.getTemplateSDK().getCategory()));
            }
            // 种类不等于2张则抛出异常
            if(categoryList.size() != 2){
                throw new CouponException("Coupon category count <> 2");
            }
            // 否则判断list中是否同时包含满减和折扣
            if(categoryList.contains(CouponCategory.MANJIAN) && categoryList.contains(CouponCategory.ZHEKOU)){
                result = executorMap.get(RuleFlag.MANJIAN_ZHEKOU).computeRule(settlement);
            }
        }

        return result;
    }
}
