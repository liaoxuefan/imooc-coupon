package com.imooc.coupon.controller;

import com.imooc.coupon.entity.Coupon;
import com.imooc.coupon.exception.CouponException;
import com.imooc.coupon.service.IUserService;
import com.imooc.coupon.vo.AcquireTemplateRequest;
import com.imooc.coupon.vo.CouponTemplateSDK;
import com.imooc.coupon.vo.SettlementInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@Slf4j
public class UserController {

    @Resource
    private IUserService userService;

    @GetMapping("/coupons")
    private List<Coupon> findCouponsByStatus(@RequestParam("userId") Long userId, @RequestParam("status") Integer status) throws CouponException {
        return userService.findCouponByStatus(userId, status);
    }


    @GetMapping("/templates")
    private List<CouponTemplateSDK> findAvailableTemplate(@RequestParam("userId") Long userId) throws CouponException {
        return userService.findAvailableTemplate(userId);
    }

    @PostMapping("/acquire/template") // @RequestBody将json字符串转成对象，不能用于get请求
    private Coupon acquireTemplate(@RequestBody AcquireTemplateRequest request) throws CouponException {
        return userService.acquireTemplate(request);
    }

    @PostMapping("/settlement")
    private SettlementInfo settlement(@RequestBody SettlementInfo info) throws CouponException {
        return userService.settlement(info);
    }
}
