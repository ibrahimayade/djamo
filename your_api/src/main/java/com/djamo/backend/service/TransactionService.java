package com.djamo.backend.service;

import com.djamo.backend.dto.UpdateStatusRequest;
import com.djamo.backend.entity.Transaction;
import com.djamo.backend.entity.enumeration.TransactionStatus;
import com.djamo.backend.service.activemq.TransactionConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

import static com.djamo.backend.constants.DjamoConstant.CLIENT_BASE_URL;
import static com.djamo.backend.constants.DjamoConstant.THIRDPARTY_BASE_URL;

@Service
@Slf4j
public class TransactionService {
    @Autowired
    private JmsTemplate jmsTemplate;
    //private static final String API_URL = CLIENT_BASE_URL + "/transaction";
    private final RestTemplate restTemplate = new RestTemplate();

    private final TransactionConsumer transactionConsumer;
    private final Environment env;

    public TransactionService(TransactionConsumer transactionConsumer, Environment env) {
        this.transactionConsumer = transactionConsumer;
        this.env = env;
    }

    public ResponseEntity<Void> updateTransactionStatus(Transaction transaction, TransactionStatus status) {

        UpdateStatusRequest request = new UpdateStatusRequest();
        request.setId(transaction.getId());
        request.setStatus(status.name());
        if (transaction != null) {
            transaction.setStatus(status);
            // Appeler l'API du client Node.js pour mettre à jour le statut de la transaction
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<UpdateStatusRequest> requestBody = new HttpEntity<>(request, headers);
                restTemplate.put(env.getProperty("CLIENT")+"/transaction", requestBody);
                transactionConsumer.receiveTransaction(transaction);
                return ResponseEntity.ok().build();
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    private void processStaleTransaction(Transaction transaction) {
        String id = transaction.getId();
        String status = getTransactionStatus(id);
        if (status.equalsIgnoreCase("Unknown") || status.equalsIgnoreCase("Error")) {
            log.info("####### Unknown or error ");
        } else {
            updateTransactionStatus(transaction, TransactionStatus.valueOf(status));
        }


    }

    public void readAllMessages() {
        while (true) {
            Transaction transaction = (Transaction) jmsTemplate.receiveAndConvert("transactionsQueue");
            if (transaction != null) {
                log.info("Transaction récupérée de la queue {} : ", transaction);
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime transactionCreatedDate = transaction.getStartTime();
                if (transactionCreatedDate.plusMinutes(5).isBefore(now)) {
                    processStaleTransaction(transaction);

                }
            } else {
                break; // Sortie de la boucle si aucun message n'est récupéré
            }
        }
    }


    private String getTransactionStatus(String transactionId) {
        String apiUrl = env.getProperty("THIRD_PARTY") + "/transactions/" + transactionId;

        ResponseEntity<Transaction> responseEntity = restTemplate.getForEntity(apiUrl, Transaction.class);
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            Transaction responseBody = responseEntity.getBody();
            if (responseBody != null) {
                return responseBody.getStatus().name();
            } else {
                return "Unknown";
            }
        } else {
            return "Error";
        }
    }

}
