package com.zsecurity.demo.entity;

import com.zsecurity.demo.enums.Roles;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Users implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    @Email(message = "Please enter valid email")
    private String email;
    private String passwords;
    private String otp;
    private String firstName;
    private String lastName;
    private LocalDate dob;
    private String gender;
    private LocalDateTime otpVerificationTime;
    private String imageLink;
    private Boolean isActive = true;
    private Boolean isActiveNow = false;
    private LocalDateTime LastActiveAt;

    @Enumerated(EnumType.STRING)
    private Roles roles;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority("ROLE_" + roles.name()));
    }


    @Override
    public String getPassword() {
        return this.passwords;
    }

    @Override
    public String getUsername() {
        return this.email;
    }


}
