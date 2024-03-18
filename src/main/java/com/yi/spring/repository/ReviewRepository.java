package com.yi.spring.repository;

import com.yi.spring.entity.Dinning;
import com.yi.spring.entity.Review;
import com.yi.spring.entity.User;
import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {


    List<Review> findByUserNoOrderByRevWriteTimeDesc(User userNo);
    Page<Review> findAll(Pageable pageable);

    Page<Review> findByUserNoAndRevContentContainingOrderByRevWriteTimeDesc(User user,String revContent, Pageable pageable);
//    Page<Review> findByUserNoAndRevContentAndRestNoRestNameContaining(User user, String search, Pageable pageable);
    Page<Review> findByUserNoAndRestNoRestNameContainingOrderByRevWriteTimeDesc(User user,String restName, Pageable pageable);


    @Query("select r from Review r where r.revStatus != 'NORMAL'")
    Page<Review> findByRevStatus(Pageable pageable);


//    Page<Review> findByUserNoAndRestNameContaining(User user,String restName, Pageable pageable);


    List<Review> findByRestNo(Dinning dinning);
//    List<Review> findByRestNo(int dinning);

    long countByUserNo(User user);

    @Query("select r from Review r where r.revStatus = 'NORMAL' and r.restNo.restNo= :restNo")
    List<Review> findByRevStatusAndRestNo(long restNo);

    @Query("select r from Review r inner join r.userNo.reviews reviews where reviews.userNo = ?1")
    List<Review> findByUserNo_Reviews_UserNo(int userNo);





    @Query("select im from ImgTb im where im.id = 3")
    List<Object[]> getImgTest();
    @Query(value="select bytes from img_tb im where im.id = 3", nativeQuery = true)
    List<Object[]> getImgTest2();
    @Query(value="select bytes from img_tb im where im.id = 3", nativeQuery = true)
    List<Tuple> getImgTest3();

//    @Query("select r from Review r where r.revImg is not null and not count(r.revImg) > 0  order by RAND() LIMIT :sLimit")
    @Query(value="select * from review r where r.rev_img is not null and r.rev_img != '' order by RAND() LIMIT :sLimit", nativeQuery = true)
    List<Review> getRandomList(@Param("sLimit") int sLimit);
//    @Query("select r from Review r where r.revImg is not null and SIZE(BYTE_LENGTH(r.revImg)) != 0 order by RAND() LIMIT :sLimit")
//    List<Review> getRandomList(@Param("sLimit") String sLimit);
//    @Query("select r from Review r where r.revImg is not null and SIZE(BYTE_LENGTH(r.revImg)) != 0 order by RAND() LIMIT :sLimit")
//    List<Review> getRandomList(@Param("sLimit") String sLimit);
    @Query("select r from Review r where r.revImg is not null order by RAND() LIMIT :sLimit")
    List<Review> getRandomList0(@Param("sLimit") String sLimit);


//    @Query("SELECT m FROM Review m WHERE m.name IN :names OR m.address IN :addresses")
//    List<Review> findByConditions(@Param("names") List<String> names, @Param("addresses") List<String> addresses);

//    @Query("SELECT m FROM Review m WHERE " +
//            "(:id is null OR m.id = :id) AND " +
//            "(:revContent is null OR m.revContent = :revContent) AND " +
//            "(:revScores is null OR m.revScore IN :revScores) AND " +
//            "(:revWriteTime is null OR m.revWriteTime = :revWriteTime)")
//    List<Review> findByRevContent(
//            @Param("id") String id,
//            @Param("revContent") String revContent,
//            @Param("revScores") List<Integer> revScores,
//            @Param("revWriteTime") String revWriteTime
//    );

//    List<Review> findByUserNo(User user);

    @Query(value = "select * from review r where r.rest_no = :restNo and r.rev_write_time >= CURDATE() - INTERVAL 7 DAY", nativeQuery = true)
    List<Review> getCountReviewLately7Days(Long restNo);

    @Transactional
    void deleteAllByUserNo(User user);

}
