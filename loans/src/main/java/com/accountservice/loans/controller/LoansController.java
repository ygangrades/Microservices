
package com.accountservice.loans.controller;

import com.accountservice.loans.config.LoansServiceConfig;
import com.accountservice.loans.model.Properties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.accountservice.loans.model.Customer;
import com.accountservice.loans.model.Loans;
import com.accountservice.loans.repository.LoansRepository;

import java.util.List;

/**
 * @author Eazy Bytes
 *
 */

@RestController
public class LoansController {
    private static final Logger logger = LoggerFactory.getLogger(LoansController.class);

    @Autowired
    private LoansRepository loansRepository;

    @Autowired
    private LoansServiceConfig loansServiceConfig;

    @PostMapping("/myLoans")
    public List<Loans> getLoansDetails(@RequestHeader("yashbank-correlation-id") String correlationid, @RequestBody Customer customer) {
        logger.info("My loan details method started");
        List<Loans> loans = loansRepository.findByCustomerIdOrderByStartDtDesc(customer.getCustomerId());
        logger.info("My loan details method ended");
        if (loans != null) {
            return loans;
        } else {
            return null;
        }

    }

    @GetMapping("/loans/properties")
    public String getPropertyDetails() throws JsonProcessingException {
        ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
        Properties properties = new Properties(loansServiceConfig.getMsg(),loansServiceConfig.getBuildVersion(),
                loansServiceConfig.getMailDetails(),loansServiceConfig.getActiveBranches());
        String jsonStr = objectWriter.writeValueAsString(properties);
        return jsonStr;
    }

}