package com.example.sp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.sp.entity.Product;
import org.mapstruct.Mapper;

import java.util.List;

public interface ProductMapper extends BaseMapper<Product> {
    List<Product> selectByIds(List<Long> productIds);
}
