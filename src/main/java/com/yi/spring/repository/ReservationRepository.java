package com.yi.spring.repository;

import com.yi.spring.entity.Dinning;
import com.yi.spring.entity.Reservation;
import com.yi.spring.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Integer> {
    List<Reservation> findByRestNo_RestNo(Long rest_no);

    List<Reservation> findByRestNo_RestNoAndUserNo_UserNoAndResStatus(Long restNo, Long userNo, String resStatus);

    @Query("SELECT r FROM Reservation r WHERE r.userNo.userNo = :userNo ORDER BY r.resTime DESC")
    List<Reservation> findLatestReservationByUserNo(@Param("userNo") Long userNo, Pageable pageable);

    @Query("SELECT r FROM Reservation r WHERE r.userNo.userNo = :userNo and r.resStatus = 'EXPIRED' ORDER BY r.resTime DESC")
    List<Reservation> ReservationStatusEXPIRED(@Param("userNo") Long userNo);
    @Query("SELECT r FROM Reservation r WHERE r.userNo.userNo = :userNo and r.resStatus = 'REVIEW' ORDER BY r.resTime DESC")
    List<Reservation> ReservationStatusREVIEW(@Param("userNo") Long userNo);
    @Query("SELECT r FROM Reservation r WHERE r.userNo.userNo = :userNo and r.resStatus = 'WAIT' ORDER BY r.resTime DESC")
    List<Reservation> ReservationStatusWAIT(@Param("userNo") Long userNo);
    @Query("SELECT r FROM Reservation r WHERE r.userNo.userNo = :userNo and r.resStatus = 'RESERVE_COMPLETED' ORDER BY r.resTime DESC")
    List<Reservation> ReservationStatusRESERVE_COMPLETED(@Param("userNo") Long userNo);


    @Query("SELECT r "+
            "FROM Reservation r " +
            "WHERE r.userNo.userNo = :userNo")
    List<Reservation> findReservationDetailsByUserNo(Long userNo);

    @Query("SELECT r " +
            "FROM Reservation r " +
            "WHERE r.userNo.userNo = :userNo and r.restNo.restName like %:restName% " +
            "ORDER BY r.resTime DESC")
    List<Reservation> findReservationDetailsByUserNoAndRestName(Long userNo, String restName);

//    @Query("SELECT r " +
//            "FROM Reservation r " +
//            "WHERE r.userNo.userNo = :userNo and r.restNo.restName like %:restName% " +
//            "ORDER BY case " +
//            "when r.restNo.restStatus = 'RESERVE_COMPLETED' then 1 " +
//            "when r.restNo.restStatus = 'WAIT' then 2 " +
//            "else 3" +
//            "end, " +
//            "r.resTime DESC")
//    List<Reservation> findReservationDetailsByUserNoAndRestName2(Long userNo, String restName);


    @Query(value = "select * from reservation r where DATE(r.res_time) = CURDATE() and r.rest_no = :restNo order by r.res_time asc", nativeQuery = true)
    List<Reservation> getTodayReservation(Long restNo);

    @Query(value = "select * from reservation r where DATE(r.res_time) > CURDATE() and r.rest_no = :restNo order by r.res_time asc", nativeQuery = true)
    List<Reservation> getWaitReservation(Long restNo);

    @Query(value="select * from reservation r where DATE(r.res_time) < CURDATE() and r.rest_no = :restNo order by r.res_time desc", nativeQuery = true)
    List<Reservation> getPastReservation(Long restNo);

    @Query(value = "SELECT r.res_no, res_time, user_no, res_guest_count, res_table_type, res_comment, res_status, " +
            " res_rejection_reason, res_time_new, rest_no , " +
            "CASE " +
            "    WHEN DATE(r.res_time) < CURDATE() THEN -1 " +
            "    WHEN DATE(r.res_time) = CURDATE() THEN 0 " +
            "    ELSE 1 " +
            "END AS datetype " +
            "FROM reservation r " +
            "WHERE r.rest_no = :restNo", nativeQuery = true)
    List<Reservation> getReservationWithDateType333(Long restNo);
    @Query(value = "SELECT r.* " +
//            ", CASE " +
//            "    WHEN DATE(r.res_time) < CURDATE() THEN -1 " +
//            "    WHEN DATE(r.res_time) = CURDATE() THEN 0 " +
//            "    ELSE 1 " +
//            "END AS datetype " +
            "FROM reservation r " +
            "WHERE r.rest_no = :restNo  order by r.res_time asc ", nativeQuery = true)
    List<Reservation> getReservationWithDateType(Long restNo);
    @Query(value = "SELECT r.*, " +
            "CASE " +
            "    WHEN DATE(r.res_time) < CURDATE() THEN -1 " +
            "    WHEN DATE(r.res_time) = CURDATE() THEN 0 " +
            "    ELSE 1 " +
            "END AS dateType " +
            "FROM reservation r " +
            "WHERE r.rest_no = :restNo", nativeQuery = true)
    List<Object[]>  getReservationWithDateType222(Long restNo);

    @Modifying
    @Transactional
    @Query(value = "UPDATE reservation r INNER JOIN review rv ON r.res_no = rv.res_no SET r.res_status = 'REVIEW' WHERE r.res_no = rv.res_no", nativeQuery = true)
    int updateReservationStatusToReviewWithJoin();

    @Modifying
    @Transactional
    @Query(value = "UPDATE reservation r SET r.res_status = 'EXPIRED' WHERE r.res_no NOT IN (SELECT rv.res_no FROM review rv WHERE rv.res_no IS NOT NULL) AND r.res_status = 'REVIEW'", nativeQuery = true)
    int updateReservationStatusToReviewWithJoin2();


    @Query(value= """
            SELECT DATE(r.res_time_new) as day, count(*)
            FROM reservation r
            WHERE
                CASE
                    WHEN :interval='1 week' THEN DATE_SUB(CURDATE(), INTERVAL 1 WEEK) <= r.res_time_new
                    WHEN :interval='1 month' THEN DATE_SUB(CURDATE(), INTERVAL 1 MONTH) <= r.res_time_new
                    WHEN :interval='3 month' THEN DATE_SUB(CURDATE(), INTERVAL 3 MONTH) <= r.res_time_new
                END
                and r.rest_no=:restNo
            group by DATE(r.res_time_new)
            ORDER BY day DESC;""",
            nativeQuery = true)
    List<Object[]> getReservationCountsByInterval(String interval, Long restNo);

    @Transactional
    void deleteAllByUserNo(User user);


    List<Reservation> findByUserNo_UserNoAndRestNo_RestNo(Integer userNo, int restNo);


}
