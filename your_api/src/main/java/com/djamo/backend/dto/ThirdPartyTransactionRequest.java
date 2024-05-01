package com.djamo.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class ThirdPartyTransactionRequest {
    private String id;

    public ThirdPartyTransactionRequest(String id) {
        this.id=id;
    }
}
