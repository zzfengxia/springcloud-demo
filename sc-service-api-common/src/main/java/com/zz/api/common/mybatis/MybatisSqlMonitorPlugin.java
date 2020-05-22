package com.zz.api.common.mybatis;

import com.alibaba.csp.sentinel.AsyncEntry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.ResourceTypeConstants;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import org.apache.ibatis.binding.MapperMethod.ParamMap;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import sun.plugin2.main.server.ResultHandler;

import java.sql.Statement;
import java.util.List;
import java.util.Properties;

/**
 * ************************************
 * create by Intellij IDEA
 * mybatis插件
 * 拦截StatementHandler.query/update方法。统计执行时间和调用频率，支持对其限流、降级
 * 针对具体执行sql语句
 *
 * @author Francis.zz
 * @date 2020-05-21 15:05
 * ************************************
 */
@Intercepts({@Signature(method = "query", type = StatementHandler.class, args = {Statement.class, ResultHandler.class}
), @Signature(method = "update", type = StatementHandler.class, args = {Statement.class})})
public class MybatisSqlMonitorPlugin implements Interceptor {
    private static final Object[] EMPTY_ARR = new Object[0];
    
    public MybatisSqlMonitorPlugin() {
    }
    
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        StatementHandler stat = (StatementHandler)invocation.getTarget();
        BoundSql boundSql = stat.getBoundSql();
        String executeSql = boundSql.getSql().trim();
        if (executeSql.indexOf(10) != -1) {
            executeSql = executeSql.replace("\r", "");
            executeSql = executeSql.replace('\n', ' ');
        }
        
        AsyncEntry entry = null;
        
        Object result;
        try {
            // sourceType:4 mybatis
            entry = SphU.asyncEntry(executeSql, ResourceTypeConstants.COMMON_DB_SQL, EntryType.OUT, EMPTY_ARR);
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
    
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }
    
    public void setProperties(Properties properties) {
    }
    
    private Object[] parseParams(BoundSql boundSql) {
        Object[] params = EMPTY_ARR;
        if (boundSql.getParameterObject() instanceof ParamMap) {
            ParamMap paramMap = (ParamMap)boundSql.getParameterObject();
            List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
            int size = parameterMappings.size();
            params = new Object[size];
            
            for(int i = 0; i < size; ++i) {
                ParameterMapping p = (ParameterMapping)parameterMappings.get(i);
                if (!p.getProperty().contains("__frch_")) {
                    params[i] = paramMap.get(p.getProperty());
                }
            }
        }
        
        return params;
    }
}
