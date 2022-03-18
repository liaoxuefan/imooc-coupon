package com.imooc.coupon.service;

import com.alibaba.fastjson.JSON;
import com.imooc.coupon.constant.CouponStatus;
import com.imooc.coupon.exception.CouponException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class UserServiceTest {

    private Long fakeUserId = 20001L;

    @Autowired
    private IUserService userService;


    @Test
    public void testFindCouponByStatus() throws CouponException{

        userService.findCouponByStatus(1l, CouponStatus.EXPIRED.getCode());
    }


    @Test
    public void testFindAvailableTemplate() throws CouponException{

        System.out.println(JSON.toJSONString(userService.findAvailableTemplate(10001l)));
    }

}
