package com.example.kdemo;

import org.junit.jupiter.api.Test;
import io.kubernetes.client.custom.Quantity;
import static org.junit.jupiter.api.Assertions.*;

class SimpleTest {

    @Test
    void testBasic() {
        assertTrue(true);
    }

    @Test
    void test2(){
        Quantity q1 = new Quantity("500Mi");
        Quantity q2 = new Quantity("1Gi");
        System.out.println(q1.add(q2).toSuffixedString()); 
    }
} 