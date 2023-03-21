package com.example.demo.Repositories;

import com.example.demo.DTOs.TransactionDTO;
import lombok.Getter;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Repository(value = "InMemoryTransactionRepository")
public class InMemoryTransactionRepository implements TransactionRepository {
    @Getter
    private final Map<Long, TransactionDTO> transactions = new HashMap<>();
    private final AtomicLong id_counter = new AtomicLong(0);

    public TransactionDTO createTransaction(long sender, long receiver, double money) {
        TransactionDTO transaction = new TransactionDTO(id_counter.get(), sender, receiver, money);
        id_counter.incrementAndGet();
        transactions.put(transaction.getId(), transaction);
        return transaction;
    }

    public TransactionDTO cancelTransaction(long id) {
        TransactionDTO transaction = transactions.get(id);
        transaction.cancel();
        transactions.put(id, transaction);
        return transaction;
    }

    public TransactionDTO getTransaction(long id) {
        return transactions.get(id);
    }
}
