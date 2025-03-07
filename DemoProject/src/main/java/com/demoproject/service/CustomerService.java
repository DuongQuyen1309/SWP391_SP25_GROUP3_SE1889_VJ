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

import java.time.LocalDate;
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
        customer.setStoreId(customer.getStoreId());
        customerRepository.save(customer);
    }

    public Customer getCustomer(Long id) {
        Optional<Customer> customer = customerRepository.findById(id);
        if (customer.isPresent()) {
            return customer.get();
        }
        return null;
    }
    public Page<Customer> searchCustomersByNameAndType(List<Long> relatedUserList,String name, Pageable pageable) {
//        List<Long> ids = customerRepository.findAllActiveCustomerIds();
//        if (ids.isEmpty()) {
//            return Page.empty();
//        }
        return customerRepository.findByIdInAndIsDeleteFalseAndNameAndCtype(relatedUserList, name, pageable);
    }


    public Page<Customer> getAllCustomersAndIsDeleteFalse(List<Long> relatedUserList, Pageable pageable) {

//        List<Long> ids = customerRepository.findAllActiveCustomerIds();
//        if (ids.isEmpty()) {
//            return Page.empty();
//        }
        Page<Customer> customers = customerRepository.findByIdInAndIsDeleteFalse(relatedUserList, pageable);
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
        updatedCustomer.get().setUpdatedAt(customer.getUpdatedAt());
        customerRepository.save(updatedCustomer.get());
    }

    public List<String> getAllPhoneNumbers(List<Long> relatedUserList){
        return customerRepository.getAllPhoneNumbers(relatedUserList);
    }


    public boolean isPhoneNumberExist(String phone, List<Long> relatedUserList) {
        List<String> allPhoneNumbers = customerRepository.getAllPhoneNumbers(relatedUserList);
        return allPhoneNumbers.contains(phone);
    }

    public Optional<Customer> getCustomerByPhone(String phone) {
        return customerRepository.findByPhone(phone);
    }

    public Page<Customer> searchCustomerByAttribute(List<Long> relatedUserList, Long req_idFrom, Long req_idTo, Integer req_moneyFrom,
                                                    Integer req_moneyTo, String req_phone, LocalDate dobFrom, LocalDate dobTo,
                                                    String req_address, String req_name, Pageable pageable) {
        return customerRepository.findByAttribute(relatedUserList, req_idFrom, req_idTo, req_moneyFrom,
                req_moneyTo, req_phone, dobFrom, dobTo, req_address, req_name, pageable);
    }

    public Page<Customer> searchCustomerAll(List<Long> relatedUserList, Pageable pageable) {

        Page<Customer> customers = customerRepository.findByIdInAndIsDeleteFalse(relatedUserList, pageable);
        return customers;
    }

    public List<Customer> searchCustomer(Long storeId,String name){
        return customerRepository.findByStoreIdAndIsDeleteFalseAndNameContainingIgnoreCase(storeId, name);
    }
}
