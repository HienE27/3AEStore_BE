package com.nguyenviethien.exercise201.DTO;

import java.util.Date;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenInfoResponse {
    private UUID id;
    private String username;
    private String role;
    private Date expiration;
}
