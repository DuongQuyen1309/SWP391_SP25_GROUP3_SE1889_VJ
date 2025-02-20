package com.demoproject.mapper;

import com.demoproject.dto.response.CustomerResponse;
import com.demoproject.dto.request.CustomerRequest;
import com.demoproject.entity.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
    @Mapping(target = "ctype", source = "ctype")
    @Mapping(target = "createdBy", source = "createdBy")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    @Mapping(target = "deletedAt", source = "deletedAt")
    Customer toCustomer(CustomerRequest customerRequest);
    CustomerRequest toCustomerRequest(Customer customer);
    CustomerResponse toCustomerResponse(Customer customer);

}
