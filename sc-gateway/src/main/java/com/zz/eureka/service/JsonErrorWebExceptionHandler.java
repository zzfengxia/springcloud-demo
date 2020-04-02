package com.zz.eureka.service;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;

import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * ************************************
 * create by Intellij IDEA
 * 默认的错误处理器在{@link org.springframework.boot.autoconfigure.web.reactive.error.ErrorWebFluxAutoConfiguration}注入
 * RouterFunction.route断言成功则响应后面的处理方法，否则响应空。默认错误处理是响应HTML页面信息
 *
 * 重写 DefaultErrorWebExceptionHandler.getRoutingFunction
 *
 * @author Francis.zz
 * @date 2020-03-17 11:40
 * ************************************
 */
@Slf4j
public class JsonErrorWebExceptionHandler extends DefaultErrorWebExceptionHandler {
    private static final Set<String> NOTFOUND_CLIENT_EXCEPTIONS = Sets.newHashSet("NotFoundException");
    private static final String CACHE_REQUEST_BODY_OBJECT_KEY = "cachedRequestBodyObject";
    
    public JsonErrorWebExceptionHandler(ErrorAttributes errorAttributes,
                                        ResourceProperties resourceProperties,
                                        ErrorProperties errorProperties,
                                        ApplicationContext applicationContext) {
        super(errorAttributes, resourceProperties, errorProperties, applicationContext);
    }
    
    /**
     * 异常情况：
     * 1. 后台服务未开启
     * 2. 网关与后台服务通信超时
     * 3. 网关找不到后台服务的路由
     * 4. 后台服务报错500
     * 5. 后台服务地址错误404。错误3是找不到网关路由规则，而这里是找到了路由规则，但是转发后台服务404
     *
     * 只有网关路由失败，或者与转发的后台服务器通信失败（tcp错误、连接超时、read timeout等）才会走到这里异常处理(错误1,2,3)
     * 路由后台服务未启动会报AnnotatedConnectException
     *
     * 如果是后台服务抛出的异常(500)，或者后台服务地址未找到(404)则不会走到这里，会直接响应到客户端。可以使用全局过滤器修改响应信息(错误4,5)
     *
     * @param request
     * @param includeStackTrace
     * @return
     */
    @Override
    protected Map<String, Object> getErrorAttributes(ServerRequest request, boolean includeStackTrace) {
        Object cachedBody = request.exchange().getAttribute(CACHE_REQUEST_BODY_OBJECT_KEY);
        if(cachedBody != null) {
            log.info("request json:{}", JSON.toJSONString(cachedBody));
        }
        // 这里其实可以根据异常类型进行定制化逻辑
        int code = 500;
        String message = "系统错误";
        Throwable error = super.getError(request);
        if(error instanceof ResponseStatusException) {
            if(isNotFoundException(error)) {
                message = "接口暂未开放";
                code = 404;
            } else {
                code = ((ResponseStatusException) error).getStatus().value();
                message = ((ResponseStatusException) error).getStatus().name();
            }
        } else if(error instanceof SocketException) {
            // 可以响应给客户端200，使用自定义的returnCode标识错误
            code = 200;
            message = error.getMessage();
        }
        
        log.info("error msg:{}", super.getErrorAttributes(request, includeStackTrace));
        
        Map<String, Object> errorAttributes = new HashMap<>();
        errorAttributes.put("returnDesc", message);
        // returnCode 可以转换为自定义的code
        errorAttributes.put("returnCode", code);
        errorAttributes.put("transactionid", "1234567890");
        errorAttributes.put("signType", null);
        errorAttributes.put("sign", null);
        return errorAttributes;
    }
    
    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
    }
    
    @Override
    protected int getHttpStatus(Map<String, Object> errorAttributes) {
        return (int) errorAttributes.get("returnCode");
    }
    
    @Override
    protected void logError(ServerRequest request, ServerResponse response, Throwable throwable) {
        log.warn(request.exchange().getLogPrefix() + formatError(throwable, request));
        log.error(String.format("%s %s Server Error for %s", request.exchange().getLogPrefix(), response.rawStatusCode(), formatRequest(request)));
    }
    
    private String formatError(Throwable ex, ServerRequest request) {
        String reason = ex.getClass().getSimpleName() + ": " + ex.getMessage();
        return "Resolved [" + reason + "] for HTTP " + request.methodName() + " " + request.path();
    }
    
    private String formatRequest(ServerRequest request) {
        String rawQuery = request.uri().getRawQuery();
        String query = StringUtils.hasText(rawQuery) ? "?" + rawQuery : "";
        return "HTTP " + request.methodName() + " \"" + request.path() + query + "\"";
    }
    
    private boolean isNotFoundException(Throwable ex) {
        String msg = NestedExceptionUtils.getMostSpecificCause(ex).getMessage();
        msg = msg == null ? "" : msg;
        
        return NOTFOUND_CLIENT_EXCEPTIONS.contains(ex.getClass().getSimpleName())
                || msg.contains("404");
    }
}
