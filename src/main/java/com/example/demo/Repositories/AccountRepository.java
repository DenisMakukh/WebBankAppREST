package com.example.demo.Repositories;

import com.example.demo.DTOs.AccountDTO;
import com.example.demo.DTOs.TransactionDTO;

import java.util.Map;


public interface AccountRepository {
    AccountDTO createAccount(double balance);

    AccountDTO addBalance(long id, double money);

    void commitTransaction(TransactionDTO transaction);

    void cancelTransaction(TransactionDTO transaction);

    AccountDTO getAccount(long id);

    Map<Long, AccountDTO> getAccounts();
}
