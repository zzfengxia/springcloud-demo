package com.zz.eureka.controller;

import com.zz.eureka.domain.User;
import com.zz.eureka.service.SequenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Created by Francis.zz on 2018/3/1.
 */
@RestController
public class SequenceController {
    @Autowired
    private SequenceService sequenceService;

    @GetMapping("getUser/{id}")
    public User test(@PathVariable("id") Integer id, HttpServletRequest request) throws IOException {
        User u = new User();
        u.setId(id);
        u.setName("Tom");
        u.setUrl(request.getRequestURL().toString());
        return u;
        /*System.out.println("heelo");
        String val = sequenceService.getNextVal();
        Writer writer = response.getWriter();
        writer.write(val);
        writer.flush();
        writer.close();*/
    }
}
