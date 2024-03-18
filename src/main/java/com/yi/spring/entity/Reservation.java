package com.yi.spring.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "res_no", nullable = false)
    @JdbcTypeCode(SqlTypes.INTEGER)
    private Long res_no;
//    public Long getRes_no(){ return this.res_no; }
//    public void setRes_no( Long resNo ){ this.res_no = resNo; }


    private LocalDateTime res_time_new;

    @Column(name = "res_time")
    private LocalDateTime resTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_no")
    private User userNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rest_no")
    private Dinning restNo;

    private String res_guest_count;
    @Column(name = "res_status")
    private String resStatus;
    private String res_comment;
    private String res_table_type;
    private String res_rejection_reason;

    @Column(name = "res_like")
    private boolean resLike = false;




////    @Access(AccessType.PROPERTY)
//    @Transient
    // transient (직렬화 방지) 디비에 저장될 필요 없음
    private transient Long dateType; // 0:오늘 -1:지남 1:예약대기
//    @Access(AccessType.PROPERTY)
//    @Transient
//    public Long getDatetype() {
//        return datetype;
//    }
//    @Column(name = "datetype")
//    public void setDatetype( Long datetype) {
//        this.ddatetype = datetype;
//    }
//    @Column(name = "datetype")
//    public Long getDatetype() {
//        return ddatetype;
//    }
    public void updateDateType() {
//        long date_diff = Period.between(LocalDateTime.now().toLocalDate(), resTime.toLocalDate()).getDays();
//        dateType = Math.abs(date_diff) / (date_diff==0?1:date_diff);

        long date_diff = ChronoUnit.DAYS.between(LocalDate.now(), resTime);
        long month_diff = ChronoUnit.MONTHS.between(LocalDate.now(), resTime);
        long year_diff = ChronoUnit.YEARS.between(LocalDate.now(), resTime);

        dateType = year_diff != 0 ? Math.abs(year_diff) / year_diff :
                month_diff != 0 ? Math.abs(month_diff) / month_diff :
                        date_diff != 0 ? Math.abs(date_diff) / date_diff : date_diff;
    }




        public ReservationStatus getReservationStatusEnum() {
        try {
            return ReservationStatus.valueOf(resStatus);
        } catch (Exception e) {
            // 예외 처리: 디비에 저장된 값이 Enum에 존재하지 않을 경우
            return ReservationStatus.NONE; // 또는 다른 기본값 설정
        }
    }
}
