package com.djamo.backend.service.activemq;

import com.djamo.backend.entity.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TransactionConsumer {

    @JmsListener(destination = "transactionsQueue", containerFactory = "jmsListenerContainerFactory")
    public void receiveTransaction(Transaction transaction) {
        log.info("Transaction reçue {} : ", transaction);
    }
}
