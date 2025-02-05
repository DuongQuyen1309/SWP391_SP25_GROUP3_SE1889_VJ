package com.demoproject.dao;

import com.demoproject.entity.Account;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.List;

public class AccountDAO {
    private static EntityManagerFactory emf ;
    private static EntityManager em ;

    public AccountDAO(String persistenceName) { emf = Persistence.createEntityManagerFactory(persistenceName); }

    public List<Account> getAccounts() {
        em = emf.createEntityManager();
        em.getTransaction().begin();
        List<Account> accounts = em.createQuery("from Account", Account.class).getResultList();
        em.getTransaction().commit();
        return accounts;
    }
}
