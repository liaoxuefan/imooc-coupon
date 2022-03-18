package com.imooc.coupon.service.impl;

import com.imooc.coupon.dao.CouponTemplateDao;
import com.imooc.coupon.entity.CouponTemplate;
import com.imooc.coupon.exception.CouponException;
import com.imooc.coupon.service.ITemplateBaseService;
import com.imooc.coupon.vo.CouponTemplateSDK;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TemplateBaseServiceImpl implements ITemplateBaseService {
    @Autowired
    private CouponTemplateDao templateDao;


    @Override
    public CouponTemplate buildTemplateInfo(Integer id) throws CouponException {
        Optional<CouponTemplate> template = templateDao.findById(id);
        // template是否存在
        if(!template.isPresent()){
            throw new CouponException(String.format("CouponTemplate Is Not Exists,id is %s", id));
        }
        return template.get();
    }


    @Override
    public List<CouponTemplateSDK> findAllUsableTemplate() {
        List<CouponTemplate> list = templateDao.findAllByAvailableAndExpired(true, false);
        return list.stream().map(this::template2TemplateSDK).collect(Collectors.toList());
    }

    @Override
    public Map<Integer, CouponTemplateSDK> findIds2TemplateSDK(Collection<Integer> ids) {
        List<CouponTemplate> templates = templateDao.findAllById(ids);

        return templates.stream().map(this::template2TemplateSDK).collect(Collectors.toMap(
                CouponTemplateSDK::getId, Function.identity()//类名：：方法名，identity获取元素本身
        ));
    }

    private CouponTemplateSDK template2TemplateSDK(CouponTemplate template){
        return new CouponTemplateSDK(template.getId(), template.getName(), template.getLogo(),
                template.getDesc(), template.getCategory().getCode(), template.getProductLine().getCode(),
                template.getKey(), template.getTarget().getCode(), template.getRule());
    }
}
