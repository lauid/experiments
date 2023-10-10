package com.example.sp.convert;

import com.example.sp.entity.UserEntity;
import com.example.sp.po.UserPo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = AttributeConvertUtil.class)
public interface IPersonMapper {
    IPersonMapper INSTANCE = Mappers.getMapper(IPersonMapper.class);

    @Mapping(target = "userNick1", source = "userNick")
    @Mapping(target = "createTime", source = "createTime", dateFormat = "yyyy-MM-dd")
    @Mapping(target = "age", source = "age", numberFormat = "#0.00")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userVerified", defaultValue = "default-userVerified")
    @Mapping(target = "attribute", source = "attribute", qualifiedByName = "jsonToObject")
    UserEntity po2entity(UserPo userPo);
}
