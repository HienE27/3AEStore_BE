package com.nguyenviethien.exercise201.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomerDto {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String userName;
    private Boolean active;
    private Boolean activated;
    private String avatarUrl;

    /** Constructor for minimal DTO (e.g. review author). */
    public CustomerDto(UUID id, String firstName, String lastName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = null;
        this.userName = null;
        this.active = null;
        this.activated = null;
        this.avatarUrl = null;
    }
}
