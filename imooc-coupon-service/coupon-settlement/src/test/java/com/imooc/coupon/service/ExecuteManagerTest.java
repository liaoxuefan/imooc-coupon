package com.imooc.coupon.service;


import com.alibaba.fastjson.JSON;
import com.imooc.coupon.constant.GoodsType;
import com.imooc.coupon.constant.PeriodType;
import com.imooc.coupon.exception.CouponException;
import com.imooc.coupon.executor.ExecuteManager;
import com.imooc.coupon.vo.CouponTemplateSDK;
import com.imooc.coupon.vo.GoodsInfo;
import com.imooc.coupon.vo.SettlementInfo;
import com.imooc.coupon.vo.TemplateRule;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;

@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
public class ExecuteManagerTest {

    private Long fakeUserId = 20001L;
    @Autowired
    private ExecuteManager manager;

    @Test
    public void testComputeRule() throws CouponException {

        log.info(JSON.toJSONString(manager.computeRule(fakeManJianCouponSettlement())));

    }
    // 构造结算信息,分别测试满减、折扣、立减、满减&折扣
    private SettlementInfo fakeManJianCouponSettlement(){
        // 结算信息
        SettlementInfo settlementInfo = new SettlementInfo();

        // 商品信息
        GoodsInfo goodsOne = new GoodsInfo();
        goodsOne.setCount(2);
        goodsOne.setPrice(25.3);
        goodsOne.setType(GoodsType.JIADIAN.getCode());
        settlementInfo.setGoodsInfos(Collections.singletonList(goodsOne));

        // 优惠券信息
        SettlementInfo.CouponAndTemplateInfo couponAndTemplateInfoOne = new SettlementInfo.CouponAndTemplateInfo();

        // 优惠券模板
        CouponTemplateSDK couponTemplateSDKOne = new CouponTemplateSDK();
        couponTemplateSDKOne.setId(1);
        couponTemplateSDKOne.setName("满减优惠券模板");
        couponTemplateSDKOne.setLogo("www.imooc.com");
        couponTemplateSDKOne.setDesc("我是满减优惠券模板");
        couponTemplateSDKOne.setCategory("001");

        // 优惠券规则(discount/usage等)
        TemplateRule ruleOne = new TemplateRule();
        TemplateRule.Expiration expirationOne = new TemplateRule.Expiration(PeriodType.REGULAR.getCode(), 0, 1669530928000l);
        TemplateRule.Discount discountOne = new TemplateRule.Discount(10, 50);
        TemplateRule.Usage usageOne = new TemplateRule.Usage("江西省", "瑞金市", JSON.toJSONString(Collections.singletonList(GoodsType.JIADIAN.getCode())));
        ruleOne.setExpiration(expirationOne);
        ruleOne.setDiscount(discountOne);
        ruleOne.setUsage(usageOne);
        ruleOne.setLimitation(1);
        couponTemplateSDKOne.setRule(ruleOne);
        couponAndTemplateInfoOne.setTemplateSDK(couponTemplateSDKOne);
        couponAndTemplateInfoOne.setId(27);

        settlementInfo.setCouponAndTemplateInfos(Collections.singletonList(couponAndTemplateInfoOne));
        settlementInfo.setUserId(fakeUserId);
        settlementInfo.setEmploy(false);
        return settlementInfo;
    }

    private SettlementInfo fakeZheKouCouponSettlement(){
        return null;
    }

}
