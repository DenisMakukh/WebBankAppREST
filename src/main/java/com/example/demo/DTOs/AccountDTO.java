package com.example.demo.DTOs;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

public class AccountDTO {
    @Getter
    private final long id;

    @Getter
    @Setter
    private volatile double balance;

    @Getter
    private final Map<Long, TransactionDTO> transactions;

    @JsonCreator
    public AccountDTO(@JsonProperty("id") long id,
                      @JsonProperty("balance") double balance,
                      @JsonProperty("transactions") Map<Long, TransactionDTO> transactions) {
        this.id = id;
        this.balance = balance;
        this.transactions = transactions;
    }

    public AccountDTO(long id, double balance) {
        this.id = id;
        this.balance = balance;
        this.transactions = new HashMap<>();
    }

    public void addTransaction(TransactionDTO transaction) {
        transactions.put(transaction.getId(), transaction);
    }
}
