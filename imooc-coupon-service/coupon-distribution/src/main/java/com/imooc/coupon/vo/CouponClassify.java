package com.imooc.coupon.vo;

import com.imooc.coupon.constant.CouponStatus;
import com.imooc.coupon.constant.PeriodType;
import com.imooc.coupon.entity.Coupon;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.time.DateUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 根据优惠券状态对优惠券进行分类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CouponClassify {

    private List<Coupon> usable;

    private List<Coupon> used;

    private List<Coupon> expired;

    public static CouponClassify classify(List<Coupon> coupons){
        List<Coupon> usable = new ArrayList<>(coupons.size());
        List<Coupon> used = new ArrayList<>(coupons.size());
        List<Coupon> expired = new ArrayList<>(coupons.size());

        coupons.forEach(c -> {

            boolean isTimeExpire;
            long curTime = new Date().getTime();
            // 判断优惠券是否过期
            if(c.getTemplateSDK().getRule().getExpiration().getPeriod().equals(PeriodType.REGULAR.getCode())){
                isTimeExpire = c.getTemplateSDK().getRule().getExpiration().getDeadline() < curTime;
            }else{
                // 发放时间 + 期限 < 当前时间
                isTimeExpire = DateUtils.addDays(c.getAssignTime(), c.getTemplateSDK().getRule().getExpiration().getGap()).getTime() < curTime;
            }

            // 将优惠券加入到对应的集合中
            // 因为优惠券中的状态是有延迟的，所以isTimeExpire为false的也要加入到expired集合中
            if(c.getStatus().getCode() == CouponStatus.EXPIRED.getCode() || isTimeExpire){
                expired.add(c);
            }else if(c.getStatus().getCode() == CouponStatus.USED.getCode()){
                used.add(c);
            }else{
                usable.add(c);
            }
        });

        return new CouponClassify(usable, used, expired);
    }
}
