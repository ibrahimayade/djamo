package com.djamo.backend.dto;

import lombok.Data;

@Data
public class UpdateStatusRequest {
    private String id;
    private String status;
}
