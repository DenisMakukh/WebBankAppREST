package com.example.demo.DTOs;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

public class TransactionDTO {
    @Getter
    private final long id;

    @Getter
    private final long sender;

    @Getter
    private final long receiver;

    @Getter
    private volatile boolean cancelled;

    @Getter
    private final double money;

    @JsonCreator
    public TransactionDTO(@JsonProperty("id") long id,
                          @JsonProperty("sender") long sender,
                          @JsonProperty("receiver") long receiver,
                          @JsonProperty("cancelled") boolean cancelled,
                          @JsonProperty("money") double money) {
        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.cancelled = cancelled;
        this.money = money;
    }

    public TransactionDTO(long id, long sender, long receiver, double money) {
        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.cancelled = false;
        this.money = money;
    }

    public void cancel() {
        cancelled = true;
    }
}
