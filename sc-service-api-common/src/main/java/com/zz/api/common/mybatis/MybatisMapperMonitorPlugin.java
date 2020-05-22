package com.zz.api.common.mybatis;

import com.alibaba.csp.sentinel.AsyncEntry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.ResourceTypeConstants;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.util.StringUtil;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.util.Properties;

/**
 * ************************************
 * create by Intellij IDEA
 * mybatis插件
 * 拦截Executor.query/update方法。统计执行时间和调用频率，支持对其限流、降级
 * 针对mapper的类名.方法名
 *
 * @author Francis.zz
 * @date 2020-05-21 15:04
 * ************************************
 */
@Intercepts({@Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})})
public class MybatisMapperMonitorPlugin implements Interceptor {
    public static final String MYBATIS_PREFIX = "mybatis:";
    private static final Object[] EMPTY_ARR = new Object[0];
    
    public MybatisMapperMonitorPlugin() {
    }
    
    public Object intercept(Invocation invocation) throws Throwable {
        MappedStatement mappedStatement = (MappedStatement)invocation.getArgs()[0];
        String mapperId = mappedStatement.getId();
        if (StringUtil.isBlank(mapperId)) {
            return invocation.proceed();
        } else {
            AsyncEntry entry = null;
            
            Object result;
            try {
                // 4:Mybatis
                entry = SphU.asyncEntry("mybatis:" + mapperId, ResourceTypeConstants.COMMON_DB_SQL, EntryType.OUT, EMPTY_ARR);
                result = invocation.proceed();
            } catch (BlockException e) {
                throw new MybatisBlockException(e);
            } catch (Throwable throwable) {
                Tracer.traceEntry(throwable, entry);
                throw throwable;
            } finally {
                if (entry != null) {
                    entry.exit();
                }
                
            }
            
            return result;
        }
    }
    
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }
    
    public void setProperties(Properties properties) {
    }
}
