package com.imooc.coupon.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;

public interface IKafkaService {

    /**
     * 消费kafka中的消息
     *
     * @param record
     */
    void consumerCouponKafkaMessage(ConsumerRecord<?, ?> record);
}
