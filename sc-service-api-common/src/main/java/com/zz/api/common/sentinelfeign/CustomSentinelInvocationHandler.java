package com.zz.api.common.sentinelfeign;

import com.alibaba.cloud.sentinel.feign.SentinelContractHolder;
import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.zz.api.common.protocal.ApiResponse;
import com.zz.sccommon.common.FeignDataThreadLocal;
import com.zz.sccommon.exception.ErrorCode;
import feign.Feign;
import feign.InvocationHandlerFactory;
import feign.MethodMetadata;
import feign.Target;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.LinkedHashMap;
import java.util.Map;

import static feign.Util.checkNotNull;

/**
 * ************************************
 * create by Intellij IDEA
 * 定制sentinel feign降级处理
 *
 * @author Francis.zz
 * @date 2020-05-19 10:45
 * ************************************
 */
@Slf4j
public class CustomSentinelInvocationHandler implements InvocationHandler {
    private final Target<?> target;
    
    private final Map<Method, InvocationHandlerFactory.MethodHandler> dispatch;
    
    private FallbackFactory fallbackFactory;
    
    private Map<Method, Method> fallbackMethodMap;
    
    CustomSentinelInvocationHandler(Target<?> target, Map<Method, InvocationHandlerFactory.MethodHandler> dispatch,
                              FallbackFactory fallbackFactory) {
        this.target = checkNotNull(target, "target");
        this.dispatch = checkNotNull(dispatch, "dispatch");
        this.fallbackFactory = fallbackFactory;
        this.fallbackMethodMap = toFallbackMethod(dispatch);
    }
    
    CustomSentinelInvocationHandler(Target<?> target, Map<Method, InvocationHandlerFactory.MethodHandler> dispatch) {
        this.target = checkNotNull(target, "target");
        this.dispatch = checkNotNull(dispatch, "dispatch");
    }
    
    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args)
            throws Throwable {
        if ("equals".equals(method.getName())) {
            try {
                Object otherHandler = args.length > 0 && args[0] != null
                        ? Proxy.getInvocationHandler(args[0]) : null;
                return equals(otherHandler);
            }
            catch (IllegalArgumentException e) {
                return false;
            }
        }
        else if ("hashCode".equals(method.getName())) {
            return hashCode();
        }
        else if ("toString".equals(method.getName())) {
            return toString();
        }
        
        Object result;
        InvocationHandlerFactory.MethodHandler methodHandler = this.dispatch.get(method);
        // only handle by HardCodedTarget
        if (target instanceof Target.HardCodedTarget) {
            Target.HardCodedTarget hardCodedTarget = (Target.HardCodedTarget) target;
            MethodMetadata methodMetadata = SentinelContractHolder.METADATA_MAP
                    .get(hardCodedTarget.type().getName()
                            + Feign.configKey(hardCodedTarget.type(), method));
            // resource default is HttpMethod:protocol://url
            if (methodMetadata == null) {
                result = methodHandler.invoke(args);
            }
            else {
                // 自定义资源名组织规则 HttpMethod:protocol://serverId/path
                String serverId = FeignDataThreadLocal.get() != null ? FeignDataThreadLocal.get().getServerId() : null;
                String cardCode = FeignDataThreadLocal.get() != null ? FeignDataThreadLocal.get().getCardCode() : null;
                String targetUrl = StringUtils.isNotEmpty(serverId) ? serverId : hardCodedTarget.url();
                if (!StringUtils.startsWith(targetUrl, "http")) {
                    targetUrl = "http://" + targetUrl;
                }
                if(StringUtils.isNotBlank(cardCode)) {
                    targetUrl = targetUrl + "/" + cardCode;
                }
                
                String resourceName = methodMetadata.template().method().toUpperCase()
                        + ":" + targetUrl + methodMetadata.template().path();
                Entry entry = null;
                try {
                    ContextUtil.enter(resourceName);
                    entry = SphU.entry(resourceName, EntryType.OUT, 1, args);
                    result = methodHandler.invoke(args);
                }
                catch (Throwable ex) {
                    // fallback handle
                    if (!BlockException.isBlockException(ex)) {
                        Tracer.trace(ex);
                    }
                    // BlockException时，如果实现了fallback则调用fallback，否则在这里实现默认的fallback
                    if (BlockException.isBlockException(ex)) {
                        if (fallbackFactory != null) {
                            try {
                                Object fallbackResult = fallbackMethodMap.get(method)
                                        .invoke(fallbackFactory.create(ex), args);
                                return fallbackResult;
                            } catch (IllegalAccessException e) {
                                // shouldn't happen as method is public due to being an
                                // interface
                                throw new AssertionError(e);
                            } catch (InvocationTargetException e) {
                                throw new AssertionError(e.getCause());
                            }
                        } else {
                            log.info("serverId:[" + FeignDataThreadLocal.get().getServerId() + "]服务已降级");
                            return ApiResponse.ofFail(ErrorCode.SERVER_DEGRADE.getErrorCode(), ErrorCode.SERVER_DEGRADE.getMessage());
                        }
                    } else{
                        // 可以响应默认信息
                        throw ex;
                    }
                }
                finally {
                    if (entry != null) {
                        entry.exit(1, args);
                    }
                    ContextUtil.exit();
                }
            }
        }
        else {
            // other target type using default strategy
            result = methodHandler.invoke(args);
        }
        
        return result;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CustomSentinelInvocationHandler) {
            CustomSentinelInvocationHandler other = (CustomSentinelInvocationHandler) obj;
            return target.equals(other.target);
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return target.hashCode();
    }
    
    @Override
    public String toString() {
        return target.toString();
    }
    
    static Map<Method, Method> toFallbackMethod(Map<Method, InvocationHandlerFactory.MethodHandler> dispatch) {
        Map<Method, Method> result = new LinkedHashMap<>();
        for (Method method : dispatch.keySet()) {
            method.setAccessible(true);
            result.put(method, method);
        }
        return result;
    }
}
