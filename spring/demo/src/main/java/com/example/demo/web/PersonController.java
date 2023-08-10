package com.example.demo.web;

import com.example.demo.model.GroupA;
import com.example.demo.model.Person;
import jakarta.validation.Valid;
import jakarta.validation.groups.Default;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.fastjson.JSON;

@RestController
public class PersonController {
    @PostMapping("/save")
    public String save(@RequestBody @Validated({GroupA.class, Default.class}) Person person, BindingResult result) {
        System.out.println(JSON.toJSONString(result.getAllErrors()));

        return "success";
    }

    @PostMapping(path = "/person")
    public ResponseEntity<Person> getPerson(@RequestBody @Valid Person person){
        return ResponseEntity.ok().body(person);
    }
}
