package com.red;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

@ApplicationScoped
public class AccountService {

    private final ConcurrentMap<String, Account> accounts = new ConcurrentHashMap<>();
    private final AtomicInteger nextId = new AtomicInteger(4000);

    @PostConstruct
    void init() {
        accounts.put("1001", new Account("1001", "Bootstrap Checking", "USD", new BigDecimal("1532.44")));
        accounts.put("2001", new Account("2001", "Savings", "USD", new BigDecimal("8099.12")));
        accounts.put("3001", new Account("3001", "Bake failure" +
                " II", "USD", new BigDecimal("12500.01")));
    }
    public List<Account> list() {
        return accounts.values().stream()
                .sorted(Comparator.comparing(Account::getId))
                .map(this::copy)
                .toList();
    }

    public Optional<Account> findById(String id) {
        Account account = accounts.get(id);
        return Optional.ofNullable(account).map(this::copy);
    }

    public Account create(Account request) {
        if (request == null) {
            throw new IllegalArgumentException("Account payload is required");
        }

        String id = normalize(request.getId());
        if (id == null) {
            id = String.valueOf(nextId.incrementAndGet());
        }

        String name = normalize(request.getName());
        String currency = normalize(request.getCurrency());
        BigDecimal balance = request.getBalance();

        if (name == null || currency == null || balance == null) {
            throw new IllegalArgumentException("Account name, currency, and balance are required");
        }

        Account account = new Account(id, name, currency.toUpperCase(Locale.ROOT), balance);
        Account existing = accounts.putIfAbsent(id, account);
        if (existing != null) {
            throw new IllegalStateException("Account with id '" + id + "' already exists");
        }

        return copy(account);
    }

    private Account copy(Account account) {
        return new Account(account.getId(), account.getName(), account.getCurrency(), account.getBalance());
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public String getInstanceName() {
        return "AccountService";
    }

}


