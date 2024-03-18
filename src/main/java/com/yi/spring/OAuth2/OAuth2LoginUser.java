package com.yi.spring.OAuth2;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Entity
@Table(name = "oauth_member")
public class OAuth2LoginUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "no", nullable = false)
    private Long no;
    @Lob
    @Column(columnDefinition = "TEXT")
    private String attributes;
    private String oauthType;
    private String email;
    private Integer hashValue;
}
