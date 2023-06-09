package com.accountservice.accounts.controller;

import com.accountservice.accounts.config.AccountsServiceConfig;
import com.accountservice.accounts.model.*;
import com.accountservice.accounts.repository.AccountsRepository;
import com.accountservice.accounts.service.client.CardsFeignClient;
import com.accountservice.accounts.service.client.LoansFeignClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.core.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class AccountsController {
    private static final Logger logger = LoggerFactory.getLogger(AccountsController.class);
    @Autowired
    private AccountsRepository accountsRepository;

    @Autowired
    private AccountsServiceConfig accountsServiceConfig;

    @Autowired
    private CardsFeignClient cardsFeignClient;

    @Autowired
    private LoansFeignClient loansFeignClient;

    @PostMapping("/myAccount")
    @Timed(value = "getAccountDetails.time", description = "Time taken to return Account Details")
    public Accounts getAccountDetails(@RequestBody Customer customer) {

        Accounts accounts = accountsRepository.findByCustomerId(customer.getCustomerId());
        if (accounts != null) {
            return accounts;
        } else {
            return null;
        }

    }

    @GetMapping("/accounts/properties")
    public String getPropertyDetails() throws JsonProcessingException{
        ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
        Properties properties = new Properties(accountsServiceConfig.getMsg(),accountsServiceConfig.getBuildVersion(),
                accountsServiceConfig.getMailDetails(),accountsServiceConfig.getActiveBranches());
        String jsonStr = objectWriter.writeValueAsString(properties);
        return jsonStr;
    }

    @PostMapping("/myCustomerDetails")
    @CircuitBreaker(name = "detailsForCustomerSupportApp",fallbackMethod ="myCustomerDetailsFallBack")
    @Retry(name = "retryForCustomerDetails", fallbackMethod = "myCustomerDetailsFallBack")
    public CustomerDetails myCustomerDetails(@RequestHeader("yashbank-correlation-id") String correlationid, @RequestBody Customer customer){
        logger.info("My customer details method started");
        Accounts accounts = accountsRepository.findByCustomerId(customer.getCustomerId());
        List<Cards> cards = cardsFeignClient.getCardDetails(correlationid,customer);
        List<Loans> loans = loansFeignClient.getLoansDetails(correlationid,customer);

        CustomerDetails customerDetails = new CustomerDetails();
        customerDetails.setAccounts(accounts);
        customerDetails.setLoans(loans);
        customerDetails.setCards(cards);
        logger.info("My customer details method ended");
        return customerDetails;

    }

    private CustomerDetails myCustomerDetailsFallBack(@RequestHeader("eazybank-correlation-id") String correlationid,Customer customer, Throwable t) {
        Accounts accounts = accountsRepository.findByCustomerId(customer.getCustomerId());
        List<Loans> loans = loansFeignClient.getLoansDetails(correlationid, customer);
        CustomerDetails customerDetails = new CustomerDetails();
        customerDetails.setAccounts(accounts);
        customerDetails.setLoans(loans);
        return customerDetails;

    }

    @GetMapping("/sayHello")
    @RateLimiter(name = "sayHello", fallbackMethod = "sayHelloFallback")
    public String sayHello() {
        return "Hello, Welcome to YashBank";
    }

    private String sayHelloFallback(Throwable t) {
        return "Hi, Welcome to YashBank";
    }
}
