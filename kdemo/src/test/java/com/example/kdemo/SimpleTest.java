package com.example.kdemo;

import org.junit.jupiter.api.Test;
import io.kubernetes.client.custom.Quantity;
import com.example.kdemo.util.QuantityUtils;
import static org.junit.jupiter.api.Assertions.*;

class SimpleTest {

    @Test
    void testBasic() {
        assertTrue(true);
    }

    @Test
    void testQuantityAddition(){
        Quantity q1 = new Quantity("500Mi");
        Quantity q2 = new Quantity("1Gi");
        
        // 使用 QuantityUtils 进行相加
        String result = QuantityUtils.addQuantities(q1, q2);
        System.out.println("500Mi + 1Gi = " + result);
        
        // 验证结果
        assertNotNull(result);
        assertTrue(result.contains("Mi") || result.contains("Gi"));
    }
} 