package com.example.demo.Repositories;

import com.example.demo.DTOs.AccountDTO;
import com.example.demo.DTOs.TransactionDTO;
import lombok.Getter;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;


@Repository(value = "InMemoryAccountRepository")
public class InMemoryAccountRepository implements AccountRepository {

    @Getter
    private final Map<Long, AccountDTO> accounts = new HashMap<>();
    private final AtomicLong id_counter = new AtomicLong(0);

    @Override
    public synchronized AccountDTO createAccount(double balance) {
        AccountDTO account = new AccountDTO(id_counter.get(), balance);
        id_counter.incrementAndGet();
        accounts.put(account.getId(), account);
        return account;
    }

    @Override
    public synchronized AccountDTO addBalance(long id, double money) {
        AccountDTO account = accounts.get(id);
        account.setBalance(accounts.get(id).getBalance() + money);
        return account;
    }

    @Override
    public synchronized void commitTransaction(TransactionDTO transaction) {
        AccountDTO account1 = accounts.get(transaction.getSender());
        AccountDTO account2 = accounts.get(transaction.getReceiver());
        addBalance(transaction.getSender(), -transaction.getMoney());
        addBalance(transaction.getReceiver(), transaction.getMoney());
        account1.addTransaction(transaction);
        account2.addTransaction(transaction);
    }

    @Override
    public synchronized void cancelTransaction(TransactionDTO transaction) {
        addBalance(transaction.getSender(), transaction.getMoney());
        addBalance(transaction.getReceiver(), -transaction.getMoney());
    }

    @Override
    public AccountDTO getAccount(long id) {
        return accounts.get(id);
    }
}
