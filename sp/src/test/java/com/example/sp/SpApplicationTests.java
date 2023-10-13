package com.example.sp;

import com.example.sp.convert.IPersonMapper;
import com.example.sp.entity.UserEntity;
import com.example.sp.mapper.ProductMapper;
import com.example.sp.po.UserPo;
import com.example.sp.service.ProductService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.imageio.IIOParam;
import java.util.Date;

@SpringBootTest
class SpApplicationTests {
    @Resource
    private ProductService productService;

    @Test
    void contextLoads() {
    }

    @Test
    void mapStructTest() {
        String attributes = "{\"id\":2,\"name\":\"测试123\"}";
        UserPo userPo = UserPo.builder()
                .id(1L)
                .age("18")
                .gmtCreate(new Date())
                .createTime(new Date())
                .buyerId(12L)
                .userNick("userNick")
                .attribute(attributes)
                .build();
        System.out.println(userPo);
        UserEntity userEntity = IPersonMapper.INSTANCE.po2entity(userPo);
        System.out.println("-----------------");
        System.out.println(userEntity);
    }

    @Test
    public void testProduct() {
        //todo
        Integer productId = productService.createProduct();
        System.out.println(productId);
    }
}
