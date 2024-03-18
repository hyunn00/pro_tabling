package com.yi.spring.repository;

import com.yi.spring.entity.Dinning;
import com.yi.spring.entity.QA;
import com.yi.spring.entity.User;
import com.yi.spring.entity.UserLikeRest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Repository
public interface UserLikeRestRepository extends JpaRepository<UserLikeRest, Integer> {


    UserLikeRest findByUserNoAndRestNo(User user, int likeRestNo);

    @Query("SELECT dr FROM Dinning dr JOIN UserLikeRest r ON dr.restNo = r.restNo where r.userNo = :userNo order by r.id desc ")
    List<Dinning> findAllDiningRestsLikedByUsers(@Param("userNo") User userNo);

    @Transactional
    void deleteAllByUserNo(User user);

}
