package com.demoproject.mapper;

import com.demoproject.dto.request.UsersRequest;
import com.demoproject.entity.Users;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UsersMapper {
    @Mapping(target = "createdBy", source = "createdBy")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    @Mapping(target = "deletedAt", source = "deletedAt")
    Users toUser(UsersRequest userRequest);

}
