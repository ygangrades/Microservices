package com.accountservice.accounts.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Yash Bank
 *
 */
@Getter
@Setter
@ToString
public class CustomerDetails {

    private Accounts accounts;
    private List<Loans> loans;
    private List<Cards> cards;



}