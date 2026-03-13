package com.nguyenviethien.exercise201.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class StaffAccountDto {
    private UUID id;
    private String first_name;
    private String last_name;
    private String email;
    private String user_name;
    private String phone_number;
    private boolean active;
    private String image;
    private String placeholder;
    private Date created_at;
    private Date updated_at;
    
    // Constructor from entity
    public StaffAccountDto(com.nguyenviethien.exercise201.entity.StaffAccount staff) {
        this.id = staff.getId();
        this.first_name = staff.getFirst_name();
        this.last_name = staff.getLast_name();
        this.email = staff.getEmail();
        this.user_name = staff.getUser_name();
        this.phone_number = staff.getPhone_number();
        this.active = staff.isActive();
        this.image = staff.getImage();
        this.placeholder = staff.getPlaceholder();
        this.created_at = staff.getCreated_at();
        this.updated_at = staff.getUpdated_at();
    }
}

