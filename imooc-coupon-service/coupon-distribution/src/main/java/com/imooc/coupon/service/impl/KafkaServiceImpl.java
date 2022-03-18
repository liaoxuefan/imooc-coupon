package com.imooc.coupon.service.impl;

import com.alibaba.fastjson.JSON;
import com.imooc.coupon.constant.Constant;
import com.imooc.coupon.constant.CouponStatus;
import com.imooc.coupon.dao.CouponDao;
import com.imooc.coupon.entity.Coupon;
import com.imooc.coupon.service.IKafkaService;
import com.imooc.coupon.vo.CouponKafkaMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class KafkaServiceImpl implements IKafkaService {

    @Autowired
    private CouponDao couponDao;

    /**
     * 将cache中的coupon的状态同步到db中
     * @param record
     */
    @Override
    @KafkaListener(topics = {Constant.TOPIC}, groupId = "imooc-coupon-1")
    public void consumerCouponKafkaMessage(ConsumerRecord<?, ?> record) {
        // 避免空指针异常，如果为空则返回一个空的Optional<T>对象
        Optional<?> kafkaMessage = Optional.ofNullable(record.value());
        // 如果有消息存在
        if(kafkaMessage.isPresent()){

            Object message = kafkaMessage.get();
            // 转换成我们定义的消息对象
            CouponKafkaMessage couponInfo = JSON.parseObject(message.toString(), CouponKafkaMessage.class);
            // 根据状态更新相应的db
            CouponStatus status = CouponStatus.of(couponInfo.getStatus());

            switch (status){
                // 不需要对可用的优惠券进行同步，因为这种优惠券更新到db前是没有id的
                case USABLE:
                    break;
                case USED:
                    processUsedCoupons(couponInfo, status);
                    break;
                case EXPIRED:
                    processExpiredCoupons(couponInfo, status);
                    break;
            }

        }

    }
    private void processUsedCoupons(CouponKafkaMessage kafkaMessage, CouponStatus status){
        processCouponByStatus(kafkaMessage, status);
    }

    private void processExpiredCoupons(CouponKafkaMessage kafkaMessage, CouponStatus status){
        processCouponByStatus(kafkaMessage, status);
    }


    private void processCouponByStatus(CouponKafkaMessage kafkaMessage, CouponStatus status){

        List<Coupon> coupons = couponDao.findAllById(kafkaMessage.getIds());
        if(CollectionUtils.isEmpty(coupons) || coupons.size() != kafkaMessage.getIds().size()){
            log.error("Coupon Count Is Incorrect During Kafka:coupons is {}, kafka is {}",coupons, kafkaMessage.getIds());
            return;
        }
        coupons.forEach(c -> c.setStatus(status));
        log.info("Number Of Coupons Processed In The Kafka is {}", String.valueOf(couponDao.saveAll(coupons).size()));
    }
}

