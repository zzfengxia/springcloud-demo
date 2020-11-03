package com.zz.scgateway.config;

import org.junit.Test;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-10-29 11:43
 * ************************************
 */
public class ReadMetricFileTest {
    @Test
    public void testReadIdxFile() {
        String fileName = "E:\\app\\biz_logs\\sc-gateway\\csp\\sc-gateway-metrics.log.2020-10-29.idx";
        try(DataInputStream dis = new DataInputStream(new FileInputStream(new File(fileName)))) {
            byte[] a = new byte[1024];
            StringBuilder sb = new StringBuilder();
            while(true) {
                System.out.println(dis.readLong());
            }
    
        }catch (Exception e) {
            //e.printStackTrace();
        }
    }
    
}
