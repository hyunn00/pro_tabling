package com.yi.spring.repository;

import com.yi.spring.entity.Dinning;
import com.yi.spring.entity.QA;
import com.yi.spring.entity.Review;
import com.yi.spring.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Repository
public interface QARepository extends JpaRepository<QA, Integer> {


    @Query("select q from QA q join QaAnswer qa on q.id = qa.qaNo where q.userNo.userNo = :userNo")
    List<QA> findQaAnswer(@Param("userNo") Long user);


    @Query("select r from QA r order by r.qaStatus asc ")
    List<QA> findAllByQaStatusOrderByAsc();



    int countByUserNo(User userNo);

    List<QA> findAll();
    
    Page<QA> findAll(Pageable pageable);

    @Transactional
    void deleteAllByUserNo(User user);

    long countByQaStatusFalse();


    List<QA> findByUserNo(User userNo, Sort qaWriteTime);
}
