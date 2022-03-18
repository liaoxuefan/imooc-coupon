package org.example.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 记录请求访问时间
 */
@Slf4j
@Component
public class PreRequestFilter extends AbstractPreZuulFilter{

    @Override
    protected Object cRun() {
        //在过滤器中存储客户端发起请求的时间戳
        requestContext.set("startTime", System.currentTimeMillis());
        return success();
    }

    /**
     * 过滤器优先级，数字越小优先级越高
     * @return
     */
    @Override
    public int filterOrder() {
        return 0;
    }
}
