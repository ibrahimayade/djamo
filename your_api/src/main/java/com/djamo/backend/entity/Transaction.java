package com.djamo.backend.entity;
import com.djamo.backend.entity.enumeration.TransactionStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Transaction {
    private String id;
    private TransactionStatus status;
    private LocalDateTime startTime;
}
