package com.accountservice.cards.controller;

import java.util.List;

import com.accountservice.cards.config.CardsServiceConfig;
import com.accountservice.cards.model.Properties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.accountservice.cards.model.Cards;
import com.accountservice.cards.model.Customer;
import com.accountservice.cards.repository.CardsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@RestController
public class CardsController {
    private static final Logger logger = LoggerFactory.getLogger(CardsController.class);

    @Autowired
    private CardsRepository cardsRepository;

    @Autowired
    private CardsServiceConfig cardsServiceConfig;

    @PostMapping("/myCards")
    public List<Cards> getCardDetails(@RequestHeader("yashbank-correlation-id") String correlationid, @RequestBody Customer customer) {
        logger.info("My card details method started");
        List<Cards> cards = cardsRepository.findByCustomerId(customer.getCustomerId());
        logger.info("My card details method ended");
        if (cards != null) {
            return cards;
        } else {
            return null;
        }

    }

    @GetMapping("/cards/properties")
    public String getPropertyDetails() throws JsonProcessingException {
        ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
        Properties properties = new Properties(cardsServiceConfig.getMsg(),cardsServiceConfig.getBuildVersion(),
                cardsServiceConfig.getMailDetails(),cardsServiceConfig.getActiveBranches());
        String jsonStr = objectWriter.writeValueAsString(properties);
        return jsonStr;
    }

}
