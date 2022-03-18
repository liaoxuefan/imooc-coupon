package com.imooc.coupon.service.impl;

import com.imooc.coupon.constant.Constant;
import com.imooc.coupon.dao.CouponTemplateDao;
import com.imooc.coupon.entity.CouponTemplate;
import com.imooc.coupon.service.IAsyncService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AsyncServiceImpl implements IAsyncService {

    @Autowired
    private CouponTemplateDao templateDao;
    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 创建优惠券码并更新模板的available状态
     * @param template
     */
    @Override
    @Async("getAsyncExecutor")
    public void asyncConstructCouponByTemplate(CouponTemplate template) {
        //计时器
        StopWatch watch = StopWatch.createStarted();

        Set<String> couponCodes = buildCouponCode(template);

        String redisKey = String.format("%s%s", Constant.RedisPrefix.COUPON_TEMPLATE, template.getId().toString());

        // 往redis中push优惠券码
        log.info("Push Coupon To Redis: {}",
                redisTemplate.opsForList().rightPushAll(redisKey, couponCodes));// 操作list

        // 之前插入过，有了id主键，这里是更新
        template.setAvailable(true);
        templateDao.save(template);
        watch.stop();

        log.info("Async Construct Coupon Code Cost Time: {}ms", watch.getTime(TimeUnit.MILLISECONDS));
    }

    /**
     * 构造优惠券码
     * @param template
     * @return
     */
    private Set<String> buildCouponCode(CouponTemplate template){
        StopWatch watch = StopWatch.createStarted();
        Set<String> result = new HashSet<String>(template.getCount());

        String prefix4 = template.getProductLine().getCode().toString() +
                template.getCategory().getCode();
        String date = new SimpleDateFormat("yyMMdd").format(template.getCreateTime());

        for(int i = 0; i != template.getCount(); ++i){
            result.add(prefix4 + buildCouponCodeSuffix14(date));
        }
        // 防止重复的code造成个数不够，一开始不用while是因为for性能更好
        while(result.size() < template.getCount()){
            result.add(prefix4 + buildCouponCodeSuffix14(date));
        }
        watch.stop();
        //记录生成时间
        log.info("Create Coupon Code Cost: {}ms",
                watch.getTime(TimeUnit.MILLISECONDS));
        return result;
    }

    // 构造优惠券码后14位,第一个数字不为0
    private String buildCouponCodeSuffix14(String date){
        char[] bases = new char[]{'1','2','3','4','5','6','7','8','9'};
        // 中间六位
        List<Character> chars = date.chars().mapToObj(e -> (char)e).collect(Collectors.toList());
        // 洗牌算法,将chars中的值打乱顺序
        Collections.shuffle(chars);
        // 将字符组成字符串
        // .map用来转换元素的值
        // Objects::toString 类名::方法名，相当于object -> object.toString,作用：stream中的每个元素都执行一遍该方法
        String mid6 = chars.stream().map(object -> object.toString()).collect(Collectors.joining());

        // 后八位
        // 在bases中随机出一位+在数字类型中随机出7位
        String suffix8 = RandomStringUtils.random(1, bases)+
                RandomStringUtils.randomNumeric(7);
        return mid6 + suffix8;
    }

}
