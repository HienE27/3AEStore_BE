package com.nguyenviethien.exercise201.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDto {
    private java.util.UUID id;
    private String firstName;
    private String lastName;
}


