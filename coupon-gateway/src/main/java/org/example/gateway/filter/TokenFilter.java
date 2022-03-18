package org.example.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * Token校验过滤器
 */
@Slf4j
@Component
public class TokenFilter extends AbstractPreZuulFilter{

    @Override
    protected Object cRun() {
        HttpServletRequest request = requestContext.getRequest();
        Object token = request.getParameter("token");
        if(null == token){
            return fail(401, "error: token is empty");
        }
        return success();
    }

    /**
     * 过滤器优先级，数字越小优先级越高
     * @return
     */
    @Override
    public int filterOrder() {
        return 1;
    }
}
