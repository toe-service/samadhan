package com.samadhan.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

import javax.persistence.Column;

@Data
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class UserRegisterRequest {
    @NonNull
    private String userName;

    @NonNull
    private Long userContactNumber;

    @NonNull
    private String userEmail;

    @NonNull
    private String userLatitude;

    @NonNull
    private String userLongitude;

}
