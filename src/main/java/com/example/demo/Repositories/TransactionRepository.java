package com.example.demo.Repositories;

import com.example.demo.DTOs.TransactionDTO;

import java.util.Map;

public interface TransactionRepository {
    TransactionDTO createTransaction(long sender, long receiver, double money);

    TransactionDTO cancelTransaction(long id);

    TransactionDTO getTransaction(long id);

    Map<Long, TransactionDTO> getTransactions();
}
