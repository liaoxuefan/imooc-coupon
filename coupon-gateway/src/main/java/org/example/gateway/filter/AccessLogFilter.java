package org.example.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * 记录请求花费的时间
 */
@Slf4j
@Component
public class AccessLogFilter extends AbstractPostZuulFilter{

    @Override
    protected Object cRun() {
        HttpServletRequest request = requestContext.getRequest();
        Long startTime = (Long) requestContext.get("startTime");
        Long duration = System.currentTimeMillis() - startTime;
        String uri = request.getRequestURI();
        log.info("uri: {},duration: {}", uri, duration);
        return success();
    }

    /**
     * 过滤器优先级，数字越小优先级越高
     * @return
     */
    @Override
    public int filterOrder() {
        return FilterConstants.SEND_RESPONSE_FILTER_ORDER-1;
    }
}
