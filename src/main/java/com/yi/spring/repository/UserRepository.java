package com.yi.spring.repository;

import com.yi.spring.entity.Dinning;
import com.yi.spring.entity.Reservation;
import com.yi.spring.entity.Review;
import com.yi.spring.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {


    List<User> findAll();



    Optional<User> findByUserNo(Integer userNo);
    Optional<User> findByUserIdAndProvider(String userId, String provider);

    List<User> findByUserNo(User user);


    long deleteByUserNo(Integer userNo);

    Optional<User> findByUserId(String userId);

    Page<User> findByUserAuth(String userAuth, Pageable pageable);



    Page<User> findByUserAuthAndUserBlock(String userAuth, Boolean a, Pageable pageable);


    Page<User> findByUserAuthAndUserNameContainingAndUserBlock(String userAuth, String userName, Boolean a, Pageable pageable);



//    Optional<User> findByUsername(String username);
//
//    Optional<User> findByEmail(String email);

    // 사용자가 소유한 가게 정보를 조인해서 가져오는 메서드


    //방금 추가
//    @Query("SELECT u FROM User u JOIN FETCH u.dinningList d WHERE u.userAuth = '2'")
    @Query("SELECT u FROM User u inner join u.diningRests diningRests WHERE u.userAuth = 'OWNER'")
    List<User> getAllWithDinningList();

    @Query("SELECT u FROM User u WHERE u.userEmail = :email")
    Optional<User> findByEmail(@Param("email") String email);

    @Query("SELECT u FROM User u WHERE u.userEmail = :email and u.userId = :id")
    Optional<User> findByEmailAndUserId(@Param("email") String email, @Param("id") String id);

    Page<User> findByUserAuthAndUserNameContaining(String userAuth, String userName, Pageable pageable);

    List<User> findByUserNameContainingIgnoreCaseAndUserAuthAndUserBlock(String searchInput5, String number, boolean b);

    List<User> findByUserAuthAndUserBlock(String number, boolean b);

    List<User> findByUserAuthAndUserBlockNot(String number, boolean b);

    List<User> findByUserAuth(String userAuth);
}