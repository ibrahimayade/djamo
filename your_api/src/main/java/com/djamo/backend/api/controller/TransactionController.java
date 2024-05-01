package com.djamo.backend.api.controller;

import com.djamo.backend.dto.ThirdPartyTransactionRequest;
import com.djamo.backend.dto.TransactionDTO;
import com.djamo.backend.entity.Transaction;
import com.djamo.backend.entity.enumeration.TransactionStatus;
import com.djamo.backend.service.TransactionService;
import com.djamo.backend.service.activemq.TransactionConsumer;
import com.djamo.backend.service.activemq.TransactionProducer;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Controller
@RequestMapping("transactions")
public class TransactionController {
    private final RestTemplate restTemplate = new RestTemplate();
    private final TransactionProducer transactionProducer;
    private final TransactionConsumer transactionConsumer;
    private final TransactionService transactionService;
    private final Environment env;


    public TransactionController(TransactionProducer transactionProducer, TransactionConsumer transactionConsumer, TransactionService transactionService, Environment env) {
        this.transactionProducer = transactionProducer;
        this.transactionConsumer = transactionConsumer;
        this.transactionService = transactionService;

        this.env = env;
    }

    @PostMapping()
    public ResponseEntity<Transaction> execute(@RequestBody TransactionDTO dto) {
        Transaction transaction = new Transaction();
        transaction.setStatus(TransactionStatus.pending);
        transaction.setId(dto.getId());
        transaction.setStartTime(LocalDateTime.now());
        transactionProducer.logTransaction(transaction);
        // Call the third-party service
        ResponseEntity<Transaction> responseEntity = restTemplate.postForEntity(env.getProperty("THIRD_PARTY")+"/transaction", new ThirdPartyTransactionRequest(dto.getId()), Transaction.class);
        if (responseEntity.getStatusCode() == HttpStatus.GATEWAY_TIMEOUT) {
            // Handle timeout
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        } else if (responseEntity.getStatusCode() == HttpStatus.OK) {
            // Handle successful
            Transaction response = responseEntity.getBody();
            transactionService.updateTransactionStatus(transaction, TransactionStatus.accepted);

            return ResponseEntity.ok(response);
        } else {
            // Handle other errors
            transactionService.updateTransactionStatus(transaction, TransactionStatus.declined);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
