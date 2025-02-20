package com.demoproject.service;

import com.demoproject.dto.request.CustomerRequest;
import com.demoproject.entity.Customer;
import com.demoproject.mapper.CustomerMapper;
import com.demoproject.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service

public class CustomerService {
    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    public CustomerService(CustomerRepository customerRepository, CustomerMapper customerMapper) {
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
    }
    public void createCustomer(CustomerRequest customerRequest) {
        Customer customer = new Customer();
        customer = customerMapper.toCustomer(customerRequest);
        customer.setPhone(customer.getPhone().trim());
        customerRepository.save(customer);
    }

    public Customer getCustomer(Long id) {
        Optional<Customer> customer = customerRepository.findById(id);
        if (customer.isPresent()) {
            return customer.get();
        }
        return null;
    }
    public Page<Customer> searchCustomersByNameAndType(String name, String ctype, Pageable pageable) {
        List<Long> ids = customerRepository.findAllActiveCustomerIds();
        if (ids.isEmpty()) {
            return Page.empty();
        }
        return customerRepository.findByIdInAndIsDeleteFalseAndNameAndCtype(ids, name, ctype, pageable);
    }

    public List<String> getAllCustomerTypes() {
        return customerRepository.findDistinctCtypes();
    }

    public Page<Customer> getAllCustomersAndIsDeleteFalse(Pageable pageable) {

        List<Long> ids = customerRepository.findAllActiveCustomerIds();
        if (ids.isEmpty()) {
            return Page.empty();
        }
        Page<Customer> customers = customerRepository.findByIdInAndIsDeleteFalse(ids, pageable);
        return customers;
    }

    public void updateCustomer(Long id, Customer customer) {
        Optional<Customer> updatedCustomer = customerRepository.findById(id);
        if (updatedCustomer.isEmpty()) {
            return ;
        }
        updatedCustomer.get().setName(customer.getName());
        updatedCustomer.get().setAddress(customer.getAddress());
        updatedCustomer.get().setPhone(customer.getPhone());
        updatedCustomer.get().setGender(customer.getGender());
        updatedCustomer.get().setDob(customer.getDob());
        updatedCustomer.get().setCtype(customer.getCtype());
        updatedCustomer.get().setUpdatedAt(customer.getUpdatedAt());
        customerRepository.save(updatedCustomer.get());

    }
}
