package com.imooc.coupon.service.impl;

import com.imooc.coupon.dao.CouponTemplateDao;
import com.imooc.coupon.entity.CouponTemplate;
import com.imooc.coupon.exception.CouponException;
import com.imooc.coupon.service.IAsyncService;
import com.imooc.coupon.service.IBuildTemplateService;
import com.imooc.coupon.vo.TemplateRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BuildTemplateServiceImpl implements IBuildTemplateService {
    @Autowired
    private IAsyncService asyncService;

    @Autowired
    private CouponTemplateDao templateDao;

    @Override
    public CouponTemplate buildTemplate(TemplateRequest request) throws CouponException {
        // 参数合法性校验
        if(!request.validate()){
            throw new CouponException("BuildTemplate Param Is Not Valid!");
        }
        // 判断同名的优惠券模板是否存在
        if(null != templateDao.findByName(request.getName())){
            throw new CouponException("Template Name Already Exists!");
        }
        CouponTemplate template = requestToTemplate(request);
        // 无id时插入数据库
        // 生成一条template数据
        template = templateDao.save(template);

        // 异步生成优惠券码
        asyncService.asyncConstructCouponByTemplate(template);

        return template;
    }
    private CouponTemplate requestToTemplate(TemplateRequest request){
        return new CouponTemplate(request.getName(), request.getLogo(), request.getDesc(),
                request.getCategory(), request.getProductLine(), request.getCount(), request.getUserId(),
                request.getTarget(), request.getRule());

    }


}
