package com.example.demo;

import com.example.demo.DTOs.AccountDTO;
import com.example.demo.DTOs.TransactionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

@RestController
public class Controller {

    private final InMemoryUserDetailsManager inMemoryUserDetailsManager;
    private final OperationManager operationManager;

    @Autowired
    public Controller(InMemoryUserDetailsManager manager, OperationManager operationManager) {
        this.inMemoryUserDetailsManager = manager;
        this.operationManager = operationManager;
    }

    @GetMapping("/")
    public String hello() {
        return "Hello. It's bank lol";
    }

    // @GetMapping("/createAccount")
    @PostMapping("/createAccount")
    public AccountDTO createAccount(double balance, String password) {
        AccountDTO account = operationManager.createAccount(balance);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Collection<? extends GrantedAuthority> currentPrincipalRoles = authentication.getAuthorities();

        if (currentPrincipalRoles.stream().noneMatch(r -> r.getAuthority().equals("ROLE_ADMIN"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        inMemoryUserDetailsManager.createUser(User.withDefaultPasswordEncoder()
                .username(String.valueOf(account.getId()))
                .password(password)
                .roles("USER")
                .build());

        return account;
    }

    // @GetMapping("/createTransaction")
    @PostMapping("/createTransaction")
    public TransactionDTO createTransaction(long receiver, double money) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();
        return operationManager.createTransaction(Long.parseLong(currentPrincipalName), receiver, money);
    }

    // @GetMapping("/cancelTransaction")
    @PostMapping("/cancelTransaction")
    public TransactionDTO cancelTransaction(long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();
        Collection<? extends GrantedAuthority> currentPrincipalRoles = authentication.getAuthorities();

        if ((!Objects.equals(
                currentPrincipalName,
                String.valueOf(operationManager.getTransaction(id).getSender()))
        )
                && (!Objects.equals(
                        currentPrincipalName,
                String.valueOf(operationManager.getTransaction(id).getReceiver()))
        )
                && (currentPrincipalRoles.stream().noneMatch(r -> r.getAuthority().equals("ROLE_ADMIN")))
        ) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return operationManager.cancelTransaction(id);
    }

    @GetMapping("/getAccount")
    public AccountDTO getAccount(long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();
        Collection<? extends GrantedAuthority> currentPrincipalRoles = authentication.getAuthorities();
        if ((!Objects.equals(currentPrincipalName, String.valueOf(id))
                && (currentPrincipalRoles.stream().noneMatch(r -> r.getAuthority().equals("ROLE_ADMIN"))))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return operationManager.getAccount(id);
    }

    @GetMapping("/getListAccounts")
    public Map<Long, AccountDTO> getListAccounts() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Collection<? extends GrantedAuthority> currentPrincipalRoles = authentication.getAuthorities();
        if (currentPrincipalRoles.stream().noneMatch(r -> r.getAuthority().equals("ROLE_ADMIN"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        return operationManager.getListAccounts();
    }

    @GetMapping("/getListTransactions")
    public Map<Long, TransactionDTO> getListTransactions() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Collection<? extends GrantedAuthority> currentPrincipalRoles = authentication.getAuthorities();
        if (currentPrincipalRoles.stream().noneMatch(r -> r.getAuthority().equals("ROLE_ADMIN"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        return operationManager.getListTransactions();
    }
}
