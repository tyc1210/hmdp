package com.hmdp.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class LoginFormDTO {
    @NotEmpty(message = "phone is empty")
    private String phone;
    @NotEmpty(message = "code is empty")
    private String code;
    private String password;
}
