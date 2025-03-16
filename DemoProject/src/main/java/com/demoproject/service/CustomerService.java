package com.demoproject.service;

import com.demoproject.dto.request.CustomerRequest;
import com.demoproject.entity.Customer;
import com.demoproject.entity.CustomerUpdateLog;
import com.demoproject.mapper.CustomerMapper;
import com.demoproject.repository.CustomerRepository;
import com.demoproject.repository.CustomerUpdateLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service

public class CustomerService {
    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final CustomerUpdateLogRepository customerUpdateLogRepository;
    public CustomerService(CustomerRepository customerRepository, CustomerMapper customerMapper, CustomerUpdateLogRepository customerUpdateLogRepository) {
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
        this.customerUpdateLogRepository = customerUpdateLogRepository;
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
    public Page<Customer> searchCustomerByAttribute(List<Long> relatedUserList, Long req_idFrom, Long req_idTo, Integer req_moneyFrom,
                                                    Integer req_moneyTo, String req_phone, LocalDate dobFrom, LocalDate dobTo, Long req_createBy,
                                                    String req_address, String req_name, Pageable pageable) {
        return customerRepository.findByAttribute(relatedUserList, req_idFrom, req_idTo, req_moneyFrom,
                req_moneyTo, req_phone, dobFrom, dobTo, req_createBy, req_address, req_name, pageable);

    }

    public Page<Customer> searchCustomerByAttribute(Long storedID, Long req_idFrom, Long req_idTo, Integer req_moneyFrom,
                                                    Integer req_moneyTo, String req_phone, LocalDate dobFrom, LocalDate dobTo, Long req_createBy,
                                                    String req_address, String req_name, Pageable pageable) {
        return customerRepository.findByAttribute(storedID, req_idFrom, req_idTo, req_moneyFrom,
                req_moneyTo, req_phone, dobFrom, dobTo, req_createBy, req_address, req_name, pageable);

    }

    public Page<Customer> searchCustomerAll(List<Long> relatedUserList, Pageable pageable) {
        Page<Customer> customers = customerRepository.findByIdInAndIsDeleteFalse(relatedUserList, pageable);
        return customers;
    }

    public Page<Customer> searchCustomerAll(Long storeID, Pageable pageable) {
        Page<Customer> customers = customerRepository.findByIdInAndIsDeleteFalse(storeID, pageable);
        return customers;
    }

    public void updateCustomer(Long id, Customer newCustomer, Long updatedBy, String status, Long storeID) {
        Optional<Customer> optionalCustomer = customerRepository.findById(id);
        if (optionalCustomer.isEmpty()) {
            return;
        }

        Customer existingCustomer = optionalCustomer.get();
        LocalDateTime now = LocalDateTime.now();

        // Kiểm tra từng trường dữ liệu xem có thay đổi không, nếu có thì log lại
        logChange(existingCustomer, newCustomer, "name", existingCustomer.getName(), newCustomer.getName(), updatedBy, now, status, storeID);
        logChange(existingCustomer, newCustomer, "address", existingCustomer.getAddress(), newCustomer.getAddress(), updatedBy, now, status, storeID);
        logChange(existingCustomer, newCustomer, "phone", existingCustomer.getPhone(), newCustomer.getPhone(), updatedBy, now, status, storeID);
        logChange(existingCustomer, newCustomer, "dob", existingCustomer.getDob().toString(), newCustomer.getDob().toString(), updatedBy, now, status, storeID);
        logChange(existingCustomer, newCustomer, "gender", existingCustomer.getGender().toString(), newCustomer.getGender().toString(), updatedBy, now, status, storeID);

        // Cập nhật thông tin mới
        existingCustomer.setName(newCustomer.getName());
        existingCustomer.setAddress(newCustomer.getAddress());
        existingCustomer.setPhone(newCustomer.getPhone());
        existingCustomer.setDob(newCustomer.getDob());
        existingCustomer.setGender(newCustomer.getGender());
        existingCustomer.setUpdatedAt(now.toLocalDate());

        customerRepository.save(existingCustomer);
    }
    private void logChange(Customer existingCustomer, Customer newCustomer, String fieldName,
                           String oldValue, String newValue, Long updatedBy, LocalDateTime updatedAt,String status, Long storeID) {
        if (oldValue != null && !oldValue.equals(newValue)) {
            CustomerUpdateLog log = new CustomerUpdateLog(existingCustomer.getId(), updatedBy, updatedAt, fieldName, oldValue, newValue,status, storeID);
            customerUpdateLogRepository.save(log);
        }
    }

    public List<String> getAllPhoneNumbers(List<Long> relatedUserList){
        return customerRepository.getAllPhoneNumbers(relatedUserList);
    }

    public List<String> getAllPhones(Long storeID){
        return customerRepository.getAllPhones(storeID);
    }


    public boolean isPhoneNumberExist(String phone, List<Long> relatedUserList) {
        List<String> allPhoneNumbers = customerRepository.getAllPhoneNumbers(relatedUserList);
        return allPhoneNumbers.contains(phone);
    }

    public Optional<Customer> getCustomerByPhone(String phone) {
        return customerRepository.findByPhone(phone);
    }

    public List<Customer> searchCustomer(Long storeId,String name){
        return customerRepository.findByStoreIdAndIsDeleteFalseAndNameContainingIgnoreCase(storeId, name);
    }
    public Customer searchCustomer(String phone){
        return customerRepository.findByPhoneAttribute(phone);
    }

    public List<Customer> getAllCustomerInStore(Long storeID){
        return customerRepository.getAllCustomerInStore(storeID);
    }

}
