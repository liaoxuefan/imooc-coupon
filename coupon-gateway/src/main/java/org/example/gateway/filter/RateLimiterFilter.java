package org.example.gateway.filter;

import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 限流过滤器
 */
@Slf4j
@Component
public class RateLimiterFilter extends AbstractPreZuulFilter{
    // 每秒投入两个令牌
    RateLimiter rateLimiter = RateLimiter.create(2.0);

    @Override
    protected Object cRun() {
        // 尝试获取令牌失败
        if(!rateLimiter.tryAcquire()){
            return fail(402, "error: rate limited");
        }
        return success();
    }

    /**
     * 过滤器优先级，数字越小优先级越高
     * @return
     */
    @Override
    public int filterOrder() {
        return 2;
    }
}
