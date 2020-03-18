package com.zz.eureka.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.util.HashMap;
import java.util.Map;

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
    public JsonErrorWebExceptionHandler(ErrorAttributes errorAttributes,
                                        ResourceProperties resourceProperties,
                                        ErrorProperties errorProperties,
                                        ApplicationContext applicationContext) {
        super(errorAttributes, resourceProperties, errorProperties, applicationContext);
    }
    
    @Override
    protected Map<String, Object> getErrorAttributes(ServerRequest request, boolean includeStackTrace) {
        // 这里其实可以根据异常类型进行定制化逻辑
        Throwable error = super.getError(request);
        Map<String, Object> errorAttributes = new HashMap<>();
        errorAttributes.put("returnDesc", error.getMessage());
        errorAttributes.put("returnCode", "1002");
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
        // 这里其实可以根据errorAttributes里面的属性定制HTTP响应码
        return HttpStatus.INTERNAL_SERVER_ERROR.value();
    }
    
    @Override
    protected void logError(ServerRequest request, ServerResponse response, Throwable throwable) {
        log.warn(request.exchange().getLogPrefix() + formatError(throwable, request));
        log.error(String.format("%s %s Server Error for %s", request.exchange().getLogPrefix(), response.rawStatusCode(), formatRequest(request)),
                throwable);
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
}
