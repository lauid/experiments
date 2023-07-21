package com.example.sgm.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller()
public class JSPController {
    //    @RequestMapping({"/jsp"})
//    @RequestMapping(value = "/jsp", method = RequestMethod.GET)
    @RequestMapping("/jsp")
    public String hello(Model model) {
        model.addAttribute("info", "Spring Boot 3.x 整合 JSP");
        return "hello";
    }
}
