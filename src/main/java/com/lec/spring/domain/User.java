package com.lec.spring.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    private long id; //    회원 번호.

    private String username; //    회원 아이디

    @JsonIgnore // json에 표시하지 않음.
    private String password; //    회원 비밀번호


    @JsonIgnore
    @ToString.Exclude
    private String re_password;
//    회원 비밀번호 확인 입력, db저장 X
    private String name;
//    회원 이름
    private String email;
//    회원 이메일

    @JsonIgnore
    private LocalDateTime regDate; //    회원 가입일.

    // OAuth2 Client
    private String provider;
    private String providerId;


}
