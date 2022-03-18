package com.imooc.coupon.controller;

import com.imooc.coupon.entity.CouponTemplate;
import com.imooc.coupon.exception.CouponException;
import com.imooc.coupon.service.IBuildTemplateService;
import com.imooc.coupon.service.ITemplateBaseService;
import com.imooc.coupon.vo.CouponTemplateSDK;
import com.imooc.coupon.vo.TemplateRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
public class CouponTemplateController {
    @Autowired
    private IBuildTemplateService buildTemplateService;
    @Autowired
    private ITemplateBaseService templateBaseService;

    /**
     * 创建优惠券模板
     * @param request
     */
    @PostMapping("/template/build")
    public CouponTemplate buildTemplate(@RequestBody TemplateRequest request) throws CouponException {
        log.info("CouponTemplate Creating");
        return buildTemplateService.buildTemplate(request);
    }

    @GetMapping("/template/info")
    public CouponTemplate buildTemplateInfo(@RequestParam("id") Integer id) throws CouponException {

        log.info("Get Template Info By Id: " + id);
        return templateBaseService.buildTemplateInfo(id);
    }

    /**
     * 查找所有可用的优惠券模板
     * @return
     */
    @GetMapping("/template/sdk/all")
    public List<CouponTemplateSDK> findAllUsableTemplate(){
        log.info("Get All Usable CouponTemplate Info");
        return templateBaseService.findAllUsableTemplate();
    }

    /**
     * 获取模板id到sdk的映射
     * @return
     */
    @GetMapping("/template/sdk/infos")
    public Map<Integer, CouponTemplateSDK> findIds2TemplateSDK(@RequestParam("ids") Collection<Integer> ids){
        log.info("ids -> CouponTemplateSDK");
        return templateBaseService.findIds2TemplateSDK(ids);
    }


}
