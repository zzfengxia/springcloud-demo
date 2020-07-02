package com.zz.scorder;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-06-17 10:40
 * ************************************
 */
public class TicketLock {
    /**
     * 服务号
     */
    private AtomicInteger serviceNum = new AtomicInteger();
    /**
     * 排队号
     */
    private AtomicInteger ticketNum = new AtomicInteger();
    /**
     * 新增一个ThreadLocal，用于存储每个线程的排队号
     */
    private ThreadLocal<Integer> ticketNumHolder = new ThreadLocal<Integer>();
    public void lock() {
        int currentTicketNum = ticketNum.getAndIncrement();
        // 获取锁的时候，将当前线程的排队号保存起来
        ticketNumHolder.set(currentTicketNum);
        while (currentTicketNum != serviceNum.get()) {
            // Do nothing
        }
    }
    public void unlock() {
        // 释放锁，从ThreadLocal中获取当前线程的排队号
        Integer currentTickNum = ticketNumHolder.get();
        serviceNum.compareAndSet(currentTickNum, currentTickNum + 1);
    }
}
