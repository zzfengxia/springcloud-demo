package com.zz.eureka.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2019-10-28 11:55
 * ************************************
 */
@Controller
public class HystrixTest {
    Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping("/hystrix")
    @ResponseBody
    public String info() throws InterruptedException {
        logger.info("info execute...");

        //Thread.sleep(5000);
        String a = null;
        if(a.equals("1")) {

        }
        return "Hello hystrix!";
    }
}
