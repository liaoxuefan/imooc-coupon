package com.imooc.coupon.schedule;

import com.imooc.coupon.dao.CouponTemplateDao;
import com.imooc.coupon.entity.CouponTemplate;
import com.imooc.coupon.vo.TemplateRule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class ScheduledTask {
    @Autowired
    private CouponTemplateDao templateDao;

    /**
     * 清理过期的优惠券模板
     */
    @Scheduled(fixedRate = 60 * 60 * 1000)// 每小时执行一次
    public void offlineCouponTemplate(){
        log.info("Start To Expire CouponTemplate");
        // 查询未过期的
        List<CouponTemplate> templates = templateDao.findAllByExpired(false);
        if(CollectionUtils.isEmpty(templates)){
            log.info("No Template Can Expire");
            return;
        }
        Date cur = new Date();
        List<CouponTemplate> expiredTemplates = new ArrayList<>(templates.size());

        templates.forEach(t -> {
            // 根据优惠券模板中的过期规则校验模板是否过期
            TemplateRule rule = t.getRule();
            if(rule.getExpiration().getDeadline() < cur.getTime()){
                t.setExpired(true);
                expiredTemplates.add(t);
            }
        });

        if(!CollectionUtils.isEmpty(expiredTemplates)){
            templateDao.saveAll(expiredTemplates);
        }
        log.info("End To Expire CouponTemplate");
    }
}
