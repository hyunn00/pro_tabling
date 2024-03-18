package com.yi.spring.entity;

import com.yi.spring.entity.meta.ImageMan;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.sql.Blob;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;

@Getter
@Setter
@Entity
@ToString
@Table(name = "review")
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rev_no", nullable = false)
    private Integer id;

    @Column(name = "rev_score")
    private Integer revScore;

    @Column(name = "rev_content", columnDefinition = "text")
    private String revContent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_no")
    private User userNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rest_no")
    private Dinning restNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "res_no")
    private Reservation resNo;

    @Column(name = "rev_write_time", length = 100)
    private LocalDateTime revWriteTime;

    @ToString.Exclude
    @Lob
    @Column(name = "rev_img" ,length = 105000)
    private byte[] revImg;

    @Column(name = "rev_str_img")
    private String revStrImg;
    public void setRevStrImg( String param ) {
        this.revStrImg = param;
        this.revImgMan = new ImageMan( param );
    }
    public String getRevStrImg(){
        return null != revStrImg ? revStrImg : "";
    }

    @Transient
    private ImageMan revImgMan = new ImageMan( revStrImg );

    @Column(name = "rev_like", length = 100)
    private String revLike;

    @Column(name="rev_status")
    private String revStatus;

    public double getRevScore(){

        return revScore / 10.0;
    }

    public String getBase64Image() {
        // byte[]를 Blob 으로 변경하면 문제가 해결될까
//        private Blob revImg;
//        try {
//            byte[] bytes = revImg.getBinaryStream().readAllBytes();
//            if (bytes != null && bytes.length > 0) {
//                return Base64.getEncoder().encodeToString(bytes);
//            }
        if (revImg != null && revImg.length > 0) {
            return Base64.getEncoder().encodeToString(revImg);
        }
        return "";
    }

}