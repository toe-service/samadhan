package com.samadhan.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
@Entity(name = "user_login_data")
public class UserLoginEntity {
    @Id
    @Column(name = "mobile_number")
    private Long mobileNumber;
    @Column(name = "otp")
    private Integer otp;
}
