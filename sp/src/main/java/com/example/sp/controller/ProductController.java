package com.example.sp.controller;

import com.example.sp.entity.Product;
import com.example.sp.service.ProductService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/products")
public class ProductController {
    @Resource
    private ProductService productService;

    @GetMapping("/{ids}")
    public Map<Long,Product> getProducts(@PathVariable List<Long> ids) {
        return productService.getProductsByIds(ids);
    }
}