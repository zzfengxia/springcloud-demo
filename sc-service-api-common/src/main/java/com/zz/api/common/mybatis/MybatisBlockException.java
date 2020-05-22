package com.zz.api.common.mybatis;

import org.apache.ibatis.exceptions.PersistenceException;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-05-21 15:04
 * ************************************
 */
public class MybatisBlockException extends PersistenceException {
    public MybatisBlockException() {
    }
    
    public MybatisBlockException(String message) {
        super(message);
    }
    
    public MybatisBlockException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public MybatisBlockException(Throwable cause) {
        super(cause);
    }
}
