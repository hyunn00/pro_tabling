package com.yi.spring.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Base64;

@Getter
@Setter
@Entity
@Table(name = "img_tb")
public class ImgTb {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "dtype", length = 100)
    private String dtype;

    @Column(name = "bytes")
    private byte[] bytes;


    public String getBase64Image() {
        if (bytes != null && bytes.length > 0) {
            return Base64.getEncoder().encodeToString(bytes);
        }
        return "";
    }

}