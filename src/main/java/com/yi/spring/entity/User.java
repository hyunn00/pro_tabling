package com.yi.spring.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@ToString
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_no", nullable = false)
    private Integer userNo;

    @Column(name = "user_id", length = 30)
    private String userId;

    @Column(name = "user_name", length = 50)
    private String userName;

    @Column(name = "user_password", length = 255)
    private String userPassword;

    @Column(name = "user_email", length = 50)
    private String userEmail;

    @Column(name = "user_tel", length = 100)
    private String userTel;

    @Column(name = "user_auth", length = 100)
    private String userAuth;

    @Column(name = "user_start_date", length = 100)
    private String userStartDate= String.valueOf(LocalDateTime.now());

    @ToString.Exclude
    @Lob
    @Column(name = "user_img", columnDefinition = "longblob")
    private byte[] userImg;

    @Column(name = "user_block")
    private boolean userBlock = false;

    @Column(name = "o_auth_provider")
    private String provider;


    @OneToMany(mappedBy = "userNo")
    @ToString.Exclude
    private Set<Dinning> diningRests = new LinkedHashSet<>();

    @OneToMany(mappedBy = "userNo")
    @ToString.Exclude
    private Set<Review> reviews = new LinkedHashSet<>();


    @Column(name = "memo")
    private String memo;

    @Column(name = "point", columnDefinition = "int default 0")
    private int point;



    public String getBase64Image() {
        if (userImg != null && userImg.length > 0) {
            return Base64.getEncoder().encodeToString(userImg);
        }
        return "";
    }


    public UserRole getUserRoleEnum() {
        try {
            return UserRole.valueOf(userAuth);
        } catch (Exception e) {
            // 예외 처리: 디비에 저장된 값이 Enum에 존재하지 않을 경우
            return UserRole.USER; // 또는 다른 기본값 설정
        }
    }
}