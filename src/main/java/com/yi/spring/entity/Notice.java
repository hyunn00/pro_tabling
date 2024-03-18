package com.yi.spring.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;
import java.util.Base64;

@Getter
@Setter
@Entity
@ToString
@Table(name = "notice")
public class Notice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "no", nullable = false)
    private Integer id;

    @Column(name = "subject", length = 30)
    private String subject;

    @Column(name = "writer", length = 20)
    private String writer;

    @Column(name = "write_date")
    private Instant writeDate;

    @Lob
    @Column(name = "content", columnDefinition = "text")
    private String content;

    @Column(name = "important_notice", columnDefinition = "longblob")
    private boolean importantNotice = false;

    @Column(name = "notice_img")
    private byte[] img ;

    public String getBase64Image() {
        if (img != null && img.length > 0) {
            return Base64.getEncoder().encodeToString(img);
        }
        return "";
    }

    // writerDate 필드에 대한 Setter 메서드
    public void setWriteDate(Instant writeDate) {
        this.writeDate = Instant.now(); // 데이터가 저장될 때 현재 시간으로 설정
    }

}