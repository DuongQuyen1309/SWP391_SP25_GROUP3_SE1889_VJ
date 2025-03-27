package com.demoproject.service;

import com.demoproject.entity.Customer;
import com.demoproject.entity.Note;
import com.demoproject.notemoneyqueue.TransactionRequest;
import com.demoproject.repository.CustomerRepository;
import com.demoproject.repository.NoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class CustomerBalanceService {
    private final CustomerRepository customerRepository;
    private final NoteRepository noteRepository;

    public CustomerBalanceService(CustomerRepository customerRepository, NoteRepository noteRepository) {
        this.customerRepository = customerRepository;
        this.noteRepository = noteRepository;
    }

    @Transactional
    public void updateBalance(TransactionRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        // Nếu là nợ, trừ tiền từ tài khoản khách hàng
        if (request.isDebt().equalsIgnoreCase("customerOwe") || request.isDebt().equalsIgnoreCase("storePay")) {
            customer.setMoneyState(customer.getMoneyState()-(request.getAmount()));
        } else { // Nếu là thanh toán, cộng tiền vào tài khoản khách hàng
            customer.setMoneyState(customer.getMoneyState()+(request.getAmount()));
        }
        customerRepository.save(customer);
        Note note = new Note();
        note.setCustomerId(customer.getId());
        note.setDebt(request.isDebt());
        note.setMoney(request.getAmount());
        note.setCreatedBy(request.getCreatedByID());
        note.setNote(request.getNotename());
        note.setCreatedAt(LocalDateTime.now());
        note.setStoreId(request.getStoreId());
        note.setImagePath(request.getImagePath());
        noteRepository.save(note);
    }
}
