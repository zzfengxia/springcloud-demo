package com.zz.scgatewaynew.handler;

import com.google.common.collect.Sets;
import com.zz.scgatewaynew.respdefine.ResponseFactoryService;
import com.zz.scgatewaynew.respdefine.UpstreamResponse;
import com.zz.scgatewaynew.util.GatewayUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.cloud.gateway.handler.predicate.ReadBodyPredicateFactory;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.context.ApplicationContext;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR;

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
    
    private static final String ATTR_CODE = "returnCode";
    private static final String ATTR_MSG = "returnDesc";
    
    public JsonErrorWebExceptionHandler(ErrorAttributes errorAttributes,
                                        ResourceProperties resourceProperties,
                                        ErrorProperties errorProperties,
                                        ApplicationContext applicationContext) {
        super(errorAttributes, resourceProperties, errorProperties, applicationContext);
    }
    
    @Autowired
    private ReadBodyPredicateFactory readBodyPredicateFactory;
    @Autowired
    private ResponseFactoryService responseFactoryService;
    
    /**
     * 异常情况：
     * 1. 后台服务未开启
     * 2. 网关与后台服务通信超时
     * 3. 网关找不到后台服务的路由，predicate false
     * 4. 后台服务报错500或连接拒绝
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
    protected Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
        // http status一律 200
        int code = 200;
        String message = "服务器开小差啦";
        Throwable error = super.getError(request);
        if(error instanceof ResponseStatusException) {
            if(isNotFoundException(error)) {
                message = "接口暂未开放";
            } /*else {
                message = ((ResponseStatusException) error).getStatus().name();
            }*/
        } /*else if(error instanceof SocketException) {
            // 可以响应给客户端200，使用自定义的returnCode标识错误
            message = error.getMessage();
        }*/
        Route route = request.exchange().getAttribute(GATEWAY_ROUTE_ATTR);
        if(route != null) {
            log.info(String.format("匹配到的路由信息：{id:%s, routeUrl:%s}", route.getId(), route.getUri()));
        }
        Object cachedBody = GatewayUtils.fetchBody(readBodyPredicateFactory, request.exchange());
        if(cachedBody != null) {
            log.info("[路由转发失败] request data:" + cachedBody);
        }
        
        // 原始错误响应信息
        log.info("origin error msg:" + super.getErrorAttributes(request, options));
        
        Map<String, Object> errorAttributes = new HashMap<>();
        errorAttributes.put(ATTR_CODE, code);
        // returnCode 可以转换为自定义的code
        errorAttributes.put(ATTR_MSG, message);
        
        return errorAttributes;
    }
    
    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
    }
    
    @Override
    protected Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
        Map<String, Object> error = getErrorAttributes(request, getErrorAttributeOptions(request, MediaType.ALL));
        int httpStatus = getHttpStatus(error);
    
        UpstreamResponse.Response failResponseInfo = responseFactoryService.failResponseInfo(request.exchange(), error.get(ATTR_MSG) + "", null);
        
        // 响应json格式给客户端
        return ServerResponse.status(failResponseInfo.getCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(failResponseInfo.getMsg()));
    }
    
    @Override
    protected int getHttpStatus(Map<String, Object> errorAttributes) {
        return NumberUtils.toInt(errorAttributes.get(ATTR_CODE) + "", HttpStatus.INTERNAL_SERVER_ERROR.value());
    }
    
    @Override
    protected void logError(ServerRequest request, ServerResponse response, Throwable throwable) {
        log.error(formatError(throwable, request));
        log.error(String.format("%s Server Error for %s", response.rawStatusCode(), GatewayUtils.formatRequest(request.exchange().getRequest())));
    }
    
    private String formatError(Throwable ex, ServerRequest request) {
        String reason = ex.getClass().getSimpleName() + ": " + ex.getMessage();
        return "Resolved [" + reason + "] for HTTP " + request.methodName() + " " + request.path();
    }
    
    private boolean isNotFoundException(Throwable ex) {
        String msg = NestedExceptionUtils.getMostSpecificCause(ex).getMessage();
        msg = msg == null ? "" : msg;
        
        return NOTFOUND_CLIENT_EXCEPTIONS.contains(ex.getClass().getSimpleName())
                || msg.contains("404");
    }
}
