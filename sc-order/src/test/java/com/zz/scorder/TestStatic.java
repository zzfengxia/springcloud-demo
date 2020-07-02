package com.zz.scorder;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-06-16 16:51
 * ************************************
 */
public class TestStatic {
    public static String initA = "1";
    static {
        System.out.println("static block exec...");
    }
}
