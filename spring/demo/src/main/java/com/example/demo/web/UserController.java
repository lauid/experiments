package com.example.demo.web;

import com.example.demo.model.User;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.swing.plaf.synth.SynthTextAreaUI;

@RestController
public class UserController {
    @RequestMapping(method = RequestMethod.POST, path = "/check")
    public String check(@RequestBody @Valid User user, BindingResult result) {
        String name = user.getName();
        if (result.hasErrors()) {
            for (ObjectError err : result.getAllErrors()) {
                System.out.println(err.getCode() + ":" + err.getDefaultMessage());
            }
        }

        return name;
    }
}
