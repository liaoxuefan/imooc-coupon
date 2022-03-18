package org.example.gateway.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;


public abstract class AbstractZuulFilter extends ZuulFilter {

    // 用于在过滤器之间传递消息
    RequestContext requestContext;
    // 过滤掉next为false的请求
    private final static String NEXT = "next";

    /**
     * 拦截需要过滤的请求，当前是过滤掉next为false的请求
     * @return
     */
    @Override
    public boolean shouldFilter() {
        RequestContext ctx = RequestContext.getCurrentContext();
        return (boolean)ctx.getOrDefault(NEXT, true);
    }

    /**
     * 过滤需要做的处理
     * @return
     * @throws ZuulException
     */
    @Override
    public Object run() throws ZuulException {
        requestContext = requestContext.getCurrentContext();
        return cRun();
    }

    protected abstract Object cRun();

    public Object fail(int code, String msg){
        requestContext.set(NEXT, false);
        requestContext.setSendZuulResponse(false);// 不去执行其他过滤器
        requestContext.getResponse().setContentType("text/html;charset=UTF-8");
        requestContext.setResponseStatusCode(code);
        // json格式
        requestContext.setResponseBody(String.format("{\"result\": \"%s!\"}",msg));
        return null;
    }

    public Object success(){
        requestContext.set(NEXT, true);
        return null;
    }
}
