package com.example.demo;

import com.example.demo.DTOs.AccountDTO;
import com.example.demo.DTOs.TransactionDTO;
import com.example.demo.Repositories.AccountRepository;
import com.example.demo.Repositories.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Component
public class OperationManager {
    
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    @Autowired
    public OperationManager(@Qualifier("InMemoryAccountRepository") AccountRepository accountRepository,
                            @Qualifier("InMemoryTransactionRepository") TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    public AccountDTO createAccount(double balance) {
        if (balance <= 0) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Wrong balance");
        }
        return accountRepository.createAccount(balance);
    }

    public AccountDTO addBalance(long id, double balance) {
        if ((balance < 0) && (accountRepository.getAccount(id).getBalance() < balance)) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Not enough money");
        }
        return accountRepository.addBalance(id, balance);
    }

    public synchronized TransactionDTO createTransaction(long sender, long receiver, double money) {
        if (accountRepository.getAccount(sender).getBalance() < money) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Not enough money");
        }
        if (money < 0) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Not correct sum");
        }
        TransactionDTO transaction = transactionRepository.createTransaction(sender, receiver, money);
        accountRepository.commitTransaction(transaction);
        return transaction;
    }

    public synchronized TransactionDTO cancelTransaction(long id) {
        TransactionDTO transaction = transactionRepository.getTransaction(id);
        if (transaction.isCancelled()) {
            return transaction;
        }
        if (accountRepository.getAccount(transaction.getReceiver()).getBalance() < transaction.getMoney()) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Not enough money");
        }
        accountRepository.cancelTransaction(transaction);
        return transactionRepository.cancelTransaction(id);
    }

    public AccountDTO getAccount(long id) {
        return accountRepository.getAccount(id);
    }

    public TransactionDTO getTransaction(long id) {
        return transactionRepository.getTransaction(id);
    }

    public Map<Long, AccountDTO> getListAccounts() {
        return accountRepository.getAccounts();
    }

    public Map<Long, TransactionDTO> getListTransactions() {
        return transactionRepository.getTransactions();
    }
}
