package com.djamo.backend.service.scheduled;

import com.djamo.backend.entity.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TransactionProcessor {

    @Autowired
    private JmsTemplate jmsTemplate;

    // Méthode planifiée pour vérifier périodiquement les transactions dans la file d'attente
    @Scheduled(fixedRate = 300000) // Exécuter toutes les 5 minutes
    public void checkQueueForStaleTransactions() {


    }


    private void readAllMessages() {
        while (true) {
            Transaction transaction = (Transaction) jmsTemplate.receiveAndConvert("transactionsQueue");
            if (transaction != null) {
                // Traitement de la transaction récupérée
                log.info("Transaction récupérée de la queue {} : ",  transaction);
            } else {
                break; // Sortie de la boucle si aucun message n'est récupéré
            }
        }
    }
}
