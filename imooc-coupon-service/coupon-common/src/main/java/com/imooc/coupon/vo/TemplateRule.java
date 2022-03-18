package com.imooc.coupon.vo;

import com.imooc.coupon.constant.PeriodType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * 优惠券规则
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateRule {
    // 优惠券过期规则
    private Expiration expiration;
    // 折扣
    private Discount discount;
    // 每人最多可以领取多少张
    private Integer limitation;
    // 使用范围
    private Usage usage;
    // 权重（可以和哪些优惠券叠加使用，同一类优惠券一定不能叠加） List<String>的json串,包含了所有能与其共用的优惠券模板的编码key
    private String weight;

    public boolean validate(){
        return expiration.validate() && discount.validate() && limitation > 0 && usage.validate() && StringUtils.isNotEmpty(weight);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Expiration{
        // 有效期规则，对应PeriodType中的code
        private Integer period;
        // 有效期间隔，只对可变期限有效
        private Integer gap;
        // 优惠券的失效日期,针对固定期限
        private Long deadline;
        // 验证规则是否有效
        boolean validate(){
            return null != PeriodType.of(period) && gap > 0 && deadline > 0;
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Discount{
        // 优惠额度：满减、折扣、立减
        private Integer quota;
        // 基准，针对满减
        private Integer base;

        boolean validate(){
            return quota > 0 && base > 0;
        }
    }

    /**
     * 使用范围
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Usage{
        // 省份
        private String province;
        // 城市
        private String city;
        // 适用的商品类型 List<Integer>的json串
        private String goodsType;

        boolean validate(){
            return StringUtils.isNotEmpty(province) && StringUtils.isNotEmpty(city) && StringUtils.isNotEmpty(goodsType);
        }

    }

}
