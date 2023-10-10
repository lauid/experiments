package com.example.sp.convert;

import com.example.sp.po.Attribute;
import org.mapstruct.Named;
import org.springframework.util.StringUtils;
import com.alibaba.fastjson.JSONObject;


public class AttributeConvertUtil {
    @Named("jsonToObject")
    public Attribute jsonToObject(String jsonStr) {
        if (!StringUtils.hasText(jsonStr)) {
            return null;
        }

        return JSONObject.parseObject(jsonStr, Attribute.class);
    }
}
