package com.demoproject.mapper;

import com.demoproject.dto.reponse.CustomerResponse;
import com.demoproject.dto.request.CustomerRequest;
import com.demoproject.entity.Customer;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
    Customer toCustomer(CustomerRequest customerRequest);
    CustomerRequest toCustomerRequest(Customer customer);
    CustomerResponse toCustomerResponse(Customer customer);

}
