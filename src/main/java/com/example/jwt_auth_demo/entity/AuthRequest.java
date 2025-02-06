package com.example.jwt_auth_demo.entity;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AuthRequest {

    private String username;
    private String password;

}
