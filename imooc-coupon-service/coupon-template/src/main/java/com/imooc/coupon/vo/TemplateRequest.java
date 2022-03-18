package com.imooc.coupon.vo;

import com.imooc.coupon.constant.CouponCategory;
import com.imooc.coupon.constant.DistributeTarget;
import com.imooc.coupon.constant.ProductLine;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * 对于每个请求有一个 Request 和 Response，所以编写了一个 TemplateRequest。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TemplateRequest {

    private String name;

    private String logo;

    private String desc;

    private String category;

    private Integer productLine;

    private Integer count;

    private Long userId;

    private Integer target;

    private TemplateRule rule;

    // 检验对象的合法性
    public boolean validate(){
        boolean stringValid = StringUtils.isNotEmpty(name) && StringUtils.isNotEmpty(logo) &&
                StringUtils.isNotEmpty(desc);
        boolean numValid = count > 0 && userId > 0;
        boolean enumValid = null != CouponCategory.of(category) && null != ProductLine.of(productLine) && null != DistributeTarget.of(target);
        return stringValid && numValid && enumValid && rule.validate();
    }



}
