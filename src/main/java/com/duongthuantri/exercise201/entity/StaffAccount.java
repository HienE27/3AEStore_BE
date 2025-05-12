package com.duongthuantri.exercise201.entity;

import java.util.List;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Date;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "staff_accounts")
public class StaffAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    @JsonIgnore
    // @JsonProperty("role")
    private Role role;
    @Column(nullable = true)
    private String first_name;
    @Column(nullable = true)
    private String last_name;
    @Column(nullable = true)
    private String phone_number;
    @Column(nullable = true)
    private String email;
    @Column(nullable = true)
    private String password_hash;
    @Column(nullable = true)
    private String user_name;
    @Column(columnDefinition = "tinyint(1) default 1")
    private boolean active;
    @Column(nullable = true)
    private String image;
    @Column(nullable = true)
    private String placeholder;
    @Column(nullable = false)
    private Date created_at;
    @Column(nullable = false)
    private Date updated_at;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", referencedColumnName = "id", insertable = false, updatable = false)
    @JsonIgnore
    private StaffAccount createdBy;
    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL)
    private List<StaffAccount> subCreatedBy;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by", referencedColumnName = "id", insertable = false, updatable = false)
    @JsonIgnore
    private StaffAccount updatedBy;
    @OneToMany(mappedBy = "updatedBy", cascade = CascadeType.ALL)
    private List<StaffAccount> subUpdatedBy;
    
}