package com.djamo.backend.service.activemq;

import com.djamo.backend.entity.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
public class TransactionProducer {
    @Autowired
    private JmsTemplate jmsTemplate;


    public void logTransaction(Transaction transaction) {
        jmsTemplate.convertAndSend("transactionsQueue", transaction);
    }

    public void sendTransactionToQueue(Transaction transaction) {
        // Définir l'ID de corrélation sur l'ID de transaction avant d'envoyer le message
        jmsTemplate.convertAndSend("transactionsQueue", transaction, message -> {
            message.setJMSCorrelationID(transaction.getId());
            return message;
        });
    }

    public Transaction retrieveTransactionFromQueue(String transactionId) {
        // Récupérer la transaction spécifique en utilisant un sélecteur JMS pour sélectionner le message avec l'ID de corrélation correspondant
        return (Transaction) jmsTemplate.receiveSelectedAndConvert("transactionsQueue", "JMSCorrelationID='" + transactionId + "'");
    }
}
