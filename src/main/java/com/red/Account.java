package com.red;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.math.BigDecimal;
import java.util.List;

@ApplicationScoped
public class Account {

    @Inject
    @RestClient
    TranxClient tranxClient;

    private String id;
    private String name;
    private String currency;
    private BigDecimal balance;

    public Account() {
    }

    public Account(String id, String name, String currency, BigDecimal balance) {
        this.id = id;
        this.name = name;
        this.currency = currency;
        this.balance = balance;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public List<Tranx> getTransactions(String id) {
        return tranxClient.getTransactions(id);
    }
}
