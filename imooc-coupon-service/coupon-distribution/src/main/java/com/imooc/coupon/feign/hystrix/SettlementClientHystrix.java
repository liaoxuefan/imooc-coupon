package com.imooc.coupon.feign.hystrix;

import com.imooc.coupon.feign.SettlementClient;
import com.imooc.coupon.vo.CommonResponse;
import com.imooc.coupon.vo.SettlementInfo;
import org.springframework.stereotype.Component;

@Component
public class SettlementClientHystrix implements SettlementClient {

    @Override
    public CommonResponse<SettlementInfo> computeRule(SettlementInfo settlementInfo) {
        settlementInfo.setEmploy(false);
        settlementInfo.setCost(-1.0);// 使用了优惠券，最终价格也不可能小于0
        return new CommonResponse<>(
                -1, "[eureka-client-coupon-settlement] request error", settlementInfo
        );
    }
}
