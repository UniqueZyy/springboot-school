package com.example.spring_school.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class LoginResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer code;
    private String message;
    private UserVO data;

    @Data
    public static class UserVO implements Serializable {
        private Long id;
        private String username;
        private String name;
        private String email;
        private String phone;
        private Integer roleType;
        private String code;
        private Integer gender;
    }
}
