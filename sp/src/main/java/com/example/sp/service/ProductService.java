package com.example.sp.service;

import com.example.sp.entity.Product;
import com.example.sp.mapper.ProductMapper;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductService {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    public Map<Long, Product> getProductsByIds(List<Long> productIds) {
        List<String> cacheKeys = new ArrayList<>();
        for (Long productId : productIds) {
            cacheKeys.add("product:" + productId);
        }

        // 从缓存中批量获取商品数据
        List<Object> cachedProducts = redisTemplate.opsForValue().multiGet(cacheKeys);

        Map<Long, Product> resultMap = new HashMap<>();
        List<Long> notCachedIds = new ArrayList<>();

        for (int i = 0; i < cachedProducts.size(); i++) {
            Object cachedProduct = cachedProducts.get(i);
            if (cachedProduct != null) {
                resultMap.put(productIds.get(i), (Product) cachedProduct);
            } else {
                notCachedIds.add(productIds.get(i));
            }
        }

        // 如果有未命中缓存的商品数据，则从数据库中查询
        if (!notCachedIds.isEmpty()) {
            Map<Long, Product> databaseProducts = getProductsByIdsFromDB(notCachedIds);

            // 将未命中缓存的商品数据存入Redis缓存
            for (Map.Entry<Long, Product> entry : databaseProducts.entrySet()) {
                Long productId = entry.getKey();
                Product product = entry.getValue();

                String cacheKey = "product:" + productId;
                redisTemplate.opsForValue().set(cacheKey, product);

                resultMap.put(productId, product);
            }
        }

        return resultMap;
    }

    @Resource
    private ProductMapper productMapper;

    public Map<Long, Product> getProductsByIdsFromDB(List<Long> productIds) {
        List<Product> products = productMapper.selectBatchIds(productIds);
        Map<Long, Product> productMap = new HashMap<>();
        for (Product product : products) {
            productMap.put(product.getId(), product);
        }
        return productMap;
    }
    public Map<Long, Product> getProductsByIdsFromDB1(List<Long> productIds) {
        List<Product> products = productMapper.selectByIds(productIds);
        Map<Long, Product> productMap = new HashMap<>();
        for (Product product : products) {
            productMap.put(product.getId(), product);
        }
        return productMap;
    }

    public int createProduct() {
        Product product = new Product();
//        product.setId(21L);
        product.setName("test1");
        product.setPrice(BigDecimal.valueOf(11.11));
        int res = productMapper.insert(product);

        System.out.println(product);
        return res;
    }
}

