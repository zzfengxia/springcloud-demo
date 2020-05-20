package com.zz.scorder;

import org.junit.Test;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-05-15 11:25
 * ************************************
 */
public class ExceptionTest {
    @Test
    public void testException() {
        try {
            if(true) {
                throw new TestException("test");
            }
        }catch (IllegalArgumentException e) {
            System.out.println("1");
        }
    }
}
