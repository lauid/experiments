package com.example.demo.web;

import com.example.demo.model.User;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
public class User2Controller {
    @RequestMapping("/query")
    public String query(@Length(min = 2, max = 10, message = "name长度2-10") @RequestParam(name = "name", required = true) String name, @Min(value = 1, message = "最小值1") @Max(value = 100, message = "最大值100") @RequestParam(name = "age", required = true) int age) {
        System.out.println(name + ":" + age);

        return name;
    }
}
