package com.imooc.coupon.service;

import com.alibaba.fastjson.JSON;
import com.imooc.coupon.vo.TemplateRequest;
import com.imooc.coupon.vo.TemplateRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

@SpringBootTest
@RunWith(SpringRunner.class)
public class BuildTemplateTest {

    @Autowired
    private IBuildTemplateService buildTemplateService;

    @Test
    public void testBuildTemplate() throws Exception{

        System.out.println(JSON.toJSONString(
                buildTemplateService.buildTemplate(fakeTemplateRequest())
        ));
        Thread.sleep(5000);
    }

    private TemplateRequest fakeTemplateRequest(){
        TemplateRequest request = new TemplateRequest();
        request.setName("优惠券模板-"+ new Date().getTime());
        request.setLogo("http://www.imooc.com");
        request.setDesc("我是优惠券模板描述");
        request.setCategory("001");
        request.setProductLine(1);
        request.setCount(2);
        request.setUserId(001l);
        request.setTarget(1);
        TemplateRule.Expiration expiration = new TemplateRule.Expiration(2, 7, 1640966400000l);
        TemplateRule.Discount discount = new TemplateRule.Discount(1, 100);
        TemplateRule.Usage usage = new TemplateRule.Usage("江西省", "瑞金市", "显卡");
        request.setRule(new TemplateRule(expiration, discount, 2, usage, "不可叠加"));

        return request;
    }
}
