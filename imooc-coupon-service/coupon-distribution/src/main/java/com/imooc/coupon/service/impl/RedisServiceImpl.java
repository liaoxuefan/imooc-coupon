package com.imooc.coupon.service.impl;

import com.alibaba.fastjson.JSON;
import com.imooc.coupon.constant.Constant;
import com.imooc.coupon.constant.CouponStatus;
import com.imooc.coupon.entity.Coupon;
import com.imooc.coupon.exception.CouponException;
import com.imooc.coupon.service.IRedisService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RedisServiceImpl implements IRedisService {

    @Autowired
    private StringRedisTemplate redisTemplate;


    @Override
    public List<Coupon> getCacheCoupon(Long userId, Integer status) {
        String redisKey = status2RedisKey(status, userId);
        List<String> couponStrs = redisTemplate.opsForHash().values(redisKey)
                .stream()
                .map(o -> Objects.toString(o, null))
                .collect(Collectors.toList());
        // 放入无效的优惠券
        if (CollectionUtils.isEmpty(couponStrs)) {
            saveEmptyCouponListToCache(userId,
                    Collections.singletonList(status));
            return Collections.emptyList();
        }
        return couponStrs.stream()
                .map(o -> JSON.parseObject(o, Coupon.class))
                .collect(Collectors.toList());
    }

    @Override
    public void saveEmptyCouponListToCache(Long userId, List<Integer> status) {

        log.info("Save Empty Coupon List To Cache");

        // key为优惠券id，value为序列化的优惠券信息，相当于在redis中存hash结构（类似map）
        Map<String, String> invalidCouponMap = new HashMap<>();

        invalidCouponMap.put("-1", JSON.toJSONString(Coupon.invalidCoupon()));

        // 使用SessionCallback把数据命令放到redis的pipeline中批量执行命令
        SessionCallback<Object> sessionCallback = new SessionCallback<Object>() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {

                status.forEach(s -> {
                    String redisKey = status2RedisKey(s, userId);
                    // 往cache中put，每种状态的都会有一个空的优惠券信息
                    operations.opsForHash().putAll(redisKey, invalidCouponMap);
                });

                return null;
            }
        };
        // 打印execute的回调结果
        log.info(JSON.toJSONString(redisTemplate.executePipelined(sessionCallback)));
    }

    // 根据优惠券状态拼接rediskey
    private String status2RedisKey(Integer status, Long userId){
        String redisKey = null;
        CouponStatus couponStatus = CouponStatus.of(status);
        switch (couponStatus){
            case USABLE:
                redisKey = String.format("%s%s", Constant.RedisPrefix.USER_COUPON_USABLE, userId);
                break;
            case USED:
                redisKey = String.format("%s%s", Constant.RedisPrefix.USER_COUPON_USED, userId);
                break;
            case EXPIRED:
                redisKey = String.format("%s%s", Constant.RedisPrefix.USER_COUPON_EXPIRED, userId);
        }
        return redisKey;
    }

    @Override
    public String tryToAcquireCouponCodeFromCache(Integer templateId) {
        String redisKey = String.format("%s%s",
                Constant.RedisPrefix.COUPON_TEMPLATE, templateId.toString());
        String couponCode = redisTemplate.opsForList().leftPop(redisKey);
        log.info("Acquire Coupon Code: {}", couponCode);

        return couponCode;
    }

    /**
     * 将用户的优惠券保存到cache中
     * @param userId 用户id
     * @param coupons 优惠券list
     * @param status 指定优惠券的状态
     * @return
     */
    @Override
    public Integer addCouponToCache(Long userId, List<Coupon> coupons, Integer status) throws CouponException {
        Integer result = -1;// 个数

        CouponStatus couponStatus = CouponStatus.of(status);
        switch (couponStatus){
            case USABLE:
                result = addCouponToCacheForUsable(userId, coupons);
                break;
            case USED:
                result = addCouponToCacheForUsed(userId, coupons);
                break;
            case EXPIRED:
                result = addCouponToCacheForExpired(userId, coupons);
                break;
        }

        return result;

    }

    // 新增加优惠券到cache中
    private Integer addCouponToCacheForUsable(Long userId, List<Coupon> coupons){
        // 如果status是usable的，只会影响一个cache
        Map<String, String> needCachedObject = new HashMap<>();
        coupons.forEach(c -> needCachedObject.put(c.getId().toString(), JSON.toJSONString(c)));

        String redisKey = status2RedisKey(CouponStatus.USABLE.getCode(), userId);
        redisTemplate.opsForHash().putAll(redisKey, needCachedObject);

        log.info("Add Coupon To Cache For Usable,userId is {}, coupons is {}",userId, coupons);

        //重置过期时间,避免缓存雪崩：缓存在同一时间失效,1-2小时
        redisTemplate.expire(redisKey, getRandomExpirationTime(1, 2), TimeUnit.SECONDS);
        return coupons.size();
    }

    // 返回[min,max]之间的随机秒数
    private Long getRandomExpirationTime(Integer min, Integer max){
        return RandomUtils.nextLong(
                min * 60 * 60,
                max * 60 * 60
        );
    }


    // 已使用的优惠券加入到cache，同时更新可用的优惠券cache
    private Integer addCouponToCacheForUsed(Long userId, List<Coupon> coupons) throws CouponException{

        Map<String, String> needCachedForUsed = new HashMap<>();
        Map<String, String> needCachedForUsable = new HashMap<>();

        coupons.forEach(c -> needCachedForUsed.put(c.getId().toString(), JSON.toJSONString(c)));

        String redisKeyForUsed = status2RedisKey(CouponStatus.USED.getCode(), userId);
        String redisKeyForUsable = status2RedisKey(CouponStatus.USABLE.getCode(), userId);

        // 获取当前用户可用的优惠券
        List<Coupon> curUsableCoupons = getCacheCoupon(userId, CouponStatus.USABLE.getCode());

        List<Integer> curIds = curUsableCoupons.stream().map(c -> c.getId()).collect(Collectors.toList());
        List<Integer> paramIds = coupons.stream().map(c -> c.getId()).collect(Collectors.toList());

        // 将cache中可用的优惠券与用户现在已使用的优惠券作对比
        // list1是否为list2的子集
        if(!CollectionUtils.isSubCollection(paramIds, curIds)){
            log.error("Used Coupons not exist Usable Cache,Coupons is {}, Usable Cache is {}, userId is {}",paramIds, curIds, userId);
            throw new CouponException("Used Coupons not exist Usable Cache");
        }

        List<String> needCleanKey = coupons.stream().map(c -> c.getId().toString()).collect(Collectors.toList());

        // 批量执行redis命令
        SessionCallback<Object> sessionCallback = new SessionCallback<Object>() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                redisTemplate.opsForHash().putAll(redisKeyForUsed, needCachedForUsed);
                redisTemplate.opsForHash().delete(redisKeyForUsable, needCleanKey.toArray());

                //重置过期时间,避免缓存雪崩：缓存在同一时间失效,1-2小时
                redisTemplate.expire(redisKeyForUsed, getRandomExpirationTime(1, 2), TimeUnit.SECONDS);
                redisTemplate.expire(redisKeyForUsable, getRandomExpirationTime(1, 2), TimeUnit.SECONDS);
                return null;
            }
        };
        log.info("Add Coupon To Cache For Used,userId is {}, coupons is {}",userId, coupons);

        log.info("Pipeline Exe Result:{}", redisTemplate.executePipelined(sessionCallback));
        // 返回受影响的优惠券的个数
        return coupons.size();
    }

    // 已过期的优惠券加入到cache，同时更新可用的优惠券cache
    private Integer addCouponToCacheForExpired(Long userId, List<Coupon> coupons) throws CouponException{

        Map<String, String> needCachedForExpired = new HashMap<>();
        Map<String, String> needCachedForUsable = new HashMap<>();

        coupons.forEach(c -> needCachedForExpired.put(c.getId().toString(), JSON.toJSONString(c)));

        String redisKeyForExpired = status2RedisKey(CouponStatus.EXPIRED.getCode(), userId);
        String redisKeyForUsable = status2RedisKey(CouponStatus.USABLE.getCode(), userId);

        // 获取当前用户可用的优惠券
        List<Coupon> curUsableCoupons = getCacheCoupon(userId, CouponStatus.USABLE.getCode());

        List<Integer> curIds = curUsableCoupons.stream().map(c -> c.getId()).collect(Collectors.toList());
        List<Integer> paramIds = coupons.stream().map(c -> c.getId()).collect(Collectors.toList());

        // 将cache中可用的优惠券与用户现在已过期的优惠券作对比
        // list1是否为list2的子集
        if(!CollectionUtils.isSubCollection(paramIds, curIds)){
            log.error("Expired Coupons not exist Usable Cache,Coupons is {}, Usable Cache is {}, userId is {}",paramIds, curIds, userId);
            throw new CouponException("Expired Coupons not exist Usable Cache");
        }

        List<String> needCleanKey = coupons.stream().map(c -> c.getId().toString()).collect(Collectors.toList());

        // 批量执行redis命令
        SessionCallback<Object> sessionCallback = new SessionCallback<Object>() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                redisTemplate.opsForHash().putAll(redisKeyForExpired, needCachedForExpired);
                redisTemplate.opsForHash().delete(redisKeyForUsable, needCleanKey.toArray());

                //重置过期时间,避免缓存雪崩：缓存在同一时间失效,1-2小时
                redisTemplate.expire(redisKeyForExpired, getRandomExpirationTime(1, 2), TimeUnit.SECONDS);
                redisTemplate.expire(redisKeyForUsable, getRandomExpirationTime(1, 2), TimeUnit.SECONDS);
                return null;
            }
        };
        log.info("Add Coupon To Cache For Expired,userId is {}, coupons is {}",userId, coupons);

        log.info("Pipeline Exe Result:{}", redisTemplate.executePipelined(sessionCallback));
        // 返回受影响的优惠券的个数
        return coupons.size();
    }



}
