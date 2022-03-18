package com.imooc.coupon.service.impl;

import com.alibaba.fastjson.JSON;
import com.imooc.coupon.constant.Constant;
import com.imooc.coupon.constant.CouponStatus;
import com.imooc.coupon.dao.CouponDao;
import com.imooc.coupon.entity.Coupon;
import com.imooc.coupon.exception.CouponException;
import com.imooc.coupon.feign.SettlementClient;
import com.imooc.coupon.feign.TemplateClient;
import com.imooc.coupon.service.IRedisService;
import com.imooc.coupon.service.IUserService;
import com.imooc.coupon.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserServiceImpl implements IUserService {

    @Autowired
    private IRedisService redisService;

    @Autowired
    private CouponDao couponDao;

    @Resource
    private TemplateClient templateClient;

    @Resource
    private SettlementClient settlementClient;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;


    /*
        1.从缓存中查询优惠券信息
        2.缓存中没有则从db中取
            1.没有则返回空集合，这时cache中已经加入了一张无效的优惠券
            2.有则将结果写回cache
        3.将无效优惠券剔除
        4.因为优惠券状态的更新是有延迟的，如果当前获取的是可用优惠券,还需要剔除已过期的优惠券(新增过期cache+减少可用cache+通过kafka处理db)
        5.返回优惠券列表
     */
    @Override
    public List<Coupon> findCouponByStatus(Long userId, Integer status) throws CouponException {
        // 在缓存中查询优惠券信息
        // key:status+userId,value:Map<coupon_id,coupon>
        List<Coupon> coupons = redisService.getCacheCoupon(userId, status);

        List<Coupon> result = new ArrayList<>();

        // 如果信息不为空，记录优惠券信息
        if(!CollectionUtils.isEmpty(coupons)){
            result = coupons;
        }else{
            // 否则从db中取,没有记录直接返回空的list，这时cache中已经加入了一张无效的优惠券
            result = couponDao.findAllByUserIdAndStatus(userId, CouponStatus.of(status));

            if(result.size() == 0){
                return result;
            }
            // 有记录则填充list的templateSDK字段，通过模板微服务
            Map<Integer, CouponTemplateSDK> map = templateClient.findIds2TemplateSDK(coupons.stream().map(coupon -> coupon.getId()).collect(Collectors.toList())).getData();


            result.stream().map(coupon -> {
                coupon.setTemplateSDK(map.get(coupon.getId()));
                return null;
            });

            // 将记录写回cache，记录优惠券信息
            redisService.addCouponToCache(userId, result, status);

        }


        // 将无效优惠券剔除，id不等于-1
        result = result.stream().filter(coupon -> coupon.getId() != -1).collect(Collectors.toList());

        // 如果当前获取的是可用优惠券，还需要对已过期的优惠券进行延迟处理，因为优惠券状态的更新是有延迟的
        if(CouponStatus.USABLE.getCode() == status){

            // 用CouponClassify判断已过期的集合是否为空,不为空就加入到缓存，以及db耗时操作发送到kafka中作异步处理
            CouponClassify classify = CouponClassify.classify(result);
            if(!CollectionUtils.isEmpty(classify.getExpired())){
                redisService.addCouponToCache(userId, classify.getExpired(), CouponStatus.EXPIRED.getCode());
                kafkaTemplate.send(Constant.TOPIC, JSON.toJSONString(
                   new CouponKafkaMessage(CouponStatus.EXPIRED.getCode(), classify.getExpired().stream().map(Coupon::getId).collect(Collectors.toList()))
                ));
            }
            result = classify.getUsable();
        }

        // 返回优惠券信息
        return result;
    }


    /*
        查看用户当前可以领取的优惠券模板
        1.获取所有可用的优惠券模板
        2.获取用户可用的优惠券,将用户可用的优惠券collect为模板id到优惠券信息的映射
        3.如果用户领取过了此优惠券模板且数量达到上限，则过滤掉
     */
    @Override
    public List<CouponTemplateSDK> findAvailableTemplate(Long userId) throws CouponException {
        // 获取当前时间
        Date curDate = new Date();
        // 获取所有可用的优惠券模板
        List<CouponTemplateSDK> couponTemplates = templateClient.findAllUsableTemplate().getData();
        // 过滤过期的优惠券模板，定时任务会有延迟需要自己过滤,这里只是一个简单的根据截至日期的判断
        couponTemplates = couponTemplates.stream().filter(t -> t.getRule().getExpiration().getDeadline() > curDate.getTime()).collect(Collectors.toList());

        // Pair相当于存一个键值对
        // Map<Integer, Pair<Integer, CouponTemplateSDK>> templateId,<template limitation(领取次数),template>
        Map<Integer, Pair<Integer, CouponTemplateSDK>> templateMap = new HashMap<>();
        // foreach填充上面的map
        couponTemplates.forEach(t -> templateMap.put(t.getId(), Pair.of(t.getRule().getLimitation(), t)));

        // 获取用户可用的优惠券
        List<Coupon> coupons = findCouponByStatus(userId, CouponStatus.USABLE.getCode());


        // Map<Integer, List<Coupon>> 将用户可用的优惠券collect为模板id到优惠券信息的映射 Collectors.groupingBy(模板id)
        Map<Integer, List<Coupon>> mapToCouponList = coupons.stream().collect(Collectors.groupingBy(Coupon::getTemplateId));

        // 根据template的rule判断是否可用领取优惠券模板
        // 如果用户领取过某种优惠券且次数达到上限,这里的业务逻辑是只对用户现在持有的优惠券作数量的判断，则return，否则返回List<CouponTemplateSDK>
        List<CouponTemplateSDK> result = new ArrayList<>(couponTemplates.size());
        templateMap.forEach((k,v) ->{
            if(mapToCouponList.containsKey(k) && mapToCouponList.get(k).size() >= v.getKey()){

                return;
            }
            result.add(v.getValue());
        });


        return result;
    }

    /*
        用户领取优惠券
        1.获取用户当前可以领取的优惠券模板
        2.尝试去缓存中获取优惠券码，领完了就抛出异常
        3.有就new一个coupon插入数据库，再更新缓存
        4.返回优惠券对象
     */
    @Override
    public Coupon acquireTemplate(AcquireTemplateRequest request) throws CouponException {

        // 从TemplateClient拿到当前请求对应的优惠券模板 Collections.singletonList返回一个只有1个元素的集合
        Map<Integer, CouponTemplateSDK> templateSDKS = templateClient.findIds2TemplateSDK(Collections.singletonList(request.getTemplateSDK().getId())).getData();

        // 上面的大小需要>0
        if(templateSDKS.size() <= 0){
            throw new CouponException("CouponTemplate id is not exist");
        }

        // 判断用户是否可用领取这张优惠券,如果用户领取过某种优惠券且次数达到上限
        List<CouponTemplateSDK> sdks = findAvailableTemplate(request.getUserId());

        List<Integer> ids = sdks.stream().map(CouponTemplateSDK::getId).collect(Collectors.toList());

        String code = "";
        // 尝试去获取优惠券码，领完了就抛出异常
        if(ids.contains(request.getTemplateSDK().getId())){
            code = redisService.tryToAcquireCouponCodeFromCache(request.getTemplateSDK().getId());
            if(StringUtils.isBlank(code)){
                throw new CouponException("Coupon code is exhausted");
            }
        }
        // new一个coupon,插入到数据库
        Coupon coupon = new Coupon(request.getTemplateSDK().getId(), code, request.getUserId(), CouponStatus.USABLE);
        coupon = couponDao.save(coupon);
        // 填充coupon对象的couponTemplateSDK后放入缓存
        coupon.setTemplateSDK(request.getTemplateSDK());
        redisService.addCouponToCache(request.getUserId(), Collections.singletonList(coupon), CouponStatus.USABLE.getCode());
        return coupon;
    }

    /*
        1.当没有传递优惠券时直接返回商品总价
        2.通过结算微服务获取结算信息
        3.如果需要核销且优惠券信息为空，这里就认为需要核销，更新优惠券缓存为已使用，kafka延迟更新db
     */
    @Override
    public SettlementInfo settlement(SettlementInfo info) throws CouponException {

        // 判断couponAndTemplateInfos是否为空，当没有传递优惠券时直接返回商品总价（保留两位小数）
        List<SettlementInfo.CouponAndTemplateInfo> list = info.getCouponAndTemplateInfos();
        if(CollectionUtils.isEmpty(list)){
            double sum = info.getGoodsInfos().stream().mapToDouble(o -> o.getPrice() * o.getCount()).sum();
            info.setCost(retain2Decimals(sum));
            return info;
        }
        // 获取用户可用的优惠券，拼接map<优惠券id,优惠券>
        List<Coupon> coupons = couponDao.findAllByUserIdAndStatus(info.getUserId(), CouponStatus.USABLE);
        Map<Integer, Coupon> couponMap = new HashMap<>();
        coupons.stream().map(coupon -> couponMap.put(coupon.getId(),coupon));

        // 如果没有优惠券MapUtils， 或者用来结算的优惠券不是用户可用优惠券的子集则抛出异常CollectionUtils.isSubCollection(a,b) a是否为b的子集
        if(MapUtils.isNotEmpty(couponMap) || CollectionUtils.isSubCollection(list.stream().map(o -> o.getId()).collect(Collectors.toList()), couponMap.keySet())){
            throw new CouponException("Settlement coupon is not exist");
        }
        // 通过couponAndTemplateInfos中的优惠券id去第二步的结果里获取优惠券信息
        List<Coupon> couponList = coupons.stream().filter(o ->
                list.stream().map(obj -> obj.getId()).collect(Collectors.toList()).contains(o.getId())
        ).collect(Collectors.toList());
        // 通过结算微服务获取结算信息
        SettlementInfo result = settlementClient.computeRule(info).getData();

        // TODO 疑问：这里更新的优惠券不一定是用户传过来的全部优惠券，我认为不一定所有优惠券都会被使用
        // 如果需要核销且优惠券信息为空，这里就认为需要核销，更新优惠券缓存为已使用，kafka延迟更新db
        if(result.getEmploy() && CollectionUtils.isEmpty(result.getCouponAndTemplateInfos())){
            List<SettlementInfo.CouponAndTemplateInfo> res = result.getCouponAndTemplateInfos();
            redisService.addCouponToCache(info.getUserId(), couponList, CouponStatus.USED.getCode());
            kafkaTemplate.send(Constant.TOPIC,
                    JSON.toJSONString(new CouponKafkaMessage(CouponStatus.USED.getCode(), couponList.stream().map(Coupon::getId).collect(Collectors.toList()))));
        }
        // 返回结算信息
        return result;
    }

    // 保留两位小数
    private double retain2Decimals(double value){
        return new BigDecimal(value).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();// 四舍五入
    }
}
