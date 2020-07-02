package com.zz.scorder;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

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
    
    @Test
    public void testStatic() {
        System.out.println(TestStatic.initA);
        System.out.println("testStatic");
    }
    
    final TicketLock lock = new TicketLock();
    @Test
    public void testLock() {
        for (int i = 0; i < 3; i++) {
            new Thread(() -> {
                lock.lock();
                AtomicInteger ticketNum = new AtomicInteger();
                AtomicInteger serviceNum = new AtomicInteger();
                int current = ticketNum.incrementAndGet();
                System.out.println(current);
                System.out.println(ticketNum.get());
                System.out.println(serviceNum.get());
                lock.unlock();
            }).start();
        }
        
    }
}
