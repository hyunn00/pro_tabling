package com.yi.spring.repository;

import com.yi.spring.entity.*;
import jakarta.persistence.Tuple;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public interface DinningRepository extends JpaRepository<Dinning, Long>, JpaSpecificationExecutor<Dinning> {

    List<Dinning> findAll();
    Optional<Dinning> deleteDinningByRestNo(int restNo);
    Optional<Dinning> findByRestNo(int restNo);
    Optional<Dinning> findByUserNo(User userNo);
    List<Dinning> findByRestNo(Long restNo);
    Page<Dinning> findAll(Pageable pageable);
    Page<Dinning> findByRestNameContainingIgnoreCase(String name, Pageable pageable);
    Optional<Dinning> findByUserNoAndRestStatusNot(User userNo, String restStatus);


    @Query("SELECT d FROM Dinning d WHERE d.restName LIKE %:keyword%")
    List<Dinning> findByRestNameContaining(@Param("keyword") String keyword);
    @Query(value = "SELECT * FROM rest_except_img_view d WHERE d.rest_name LIKE %:keyword%", nativeQuery = true)
    List<Object[]> findByRestNameContainingFromView(@Param("keyword") String keyword);
    @Query("select d from Dinning d where d.restImg is not null order by RAND() LIMIT :sLimit")
    List<Dinning> getRandomList(@Param("sLimit") String sLimit);
    /*
    @Query("SELECT d, COALESCE((SELECT COUNT(r) FROM Review r WHERE r.restNo.restNo = d.restNo), 0) AS totalReviews FROM Dinning d ORDER BY totalReviews DESC")
    List<Dinning> findAllWithTotalReviewsOrderByTotalReviewsDesc();

    @Query("SELECT d, COALESCE((SELECT COUNT(r) FROM Review r WHERE r.restNo.restNo = d.restNo), 0) AS totalReviews FROM Dinning d WHERE d.restName LIKE %:keyword% ORDER BY totalReviews DESC")
    List<Dinning> findAllWithTotalReviewsOrderByTotalReviewsDesc(@Param("keyword") String keyword);
    */
//    @Query(value = "select d.rest_no, avg(r.rev_score) from dining_rest d left join review r on d.rest_no = r.rest_no group by d.rest_no order by avg(r.rev_score) desc", nativeQuery = true)
//    Map<Integer, Float> getRestScore();

    /*
    @Query("select d, COALESCE((SELECT avg(r.revScore) FROM Review r WHERE r.restNo.restNo = d.restNo), 0) AS restScore from Dinning d group by d.restNo order by restScore desc")
    List<Dinning> getRestScore();
    @Query("select d, COALESCE((SELECT avg(r.revScore) FROM Review r WHERE r.restNo.restNo = d.restNo), 0) AS restScore from Dinning d group by d.restNo order by restScore desc")
    List<Object[]> getRestScore2();
    */
    @Query("SELECT d FROM Dinning d JOIN FETCH d.userNo u WHERE u.userAuth = '2'")
//    @Query("SELECT d FROM Dinning d inner join d.userNo u WHERE u.userAuth = '2'")
    List<Dinning> getAllByUserAuthIsOwner();



    // 폐점 관련

    Page<Dinning> findByRestNameContainingAndRestStatus(String name, String status, Pageable pageable);



    Page<Dinning> findByRestStatus(String status, Pageable pageable);

//    @Query( "SELECT subquery.*\n" +
//            "FROM (\n" +
//            "    SELECT *,\n" +
//            "           ROW_NUMBER() OVER (PARTITION BY rest_category ORDER BY RAND()) AS row_num\n" +
//            "    FROM dining_rest\n" +
//            "    where rest_status = 'NORMAL'\n" +
//            ") AS subquery")
//    Slice<Tuple> getRandomCategoryList();
//    @Query( value= """
//            SELECT *
//            FROM (
//                SELECT *,
//                       ROW_NUMBER() OVER (PARTITION BY rest_category ORDER BY RAND()) AS row_num
//                FROM dining_rest
//                where rest_status = 'NORMAL'
//            ) AS subquery""", nativeQuery = true)
//    Slice<Tuple> getRandomCategoryList();


    /*
<div class="reviewT" th:each="review : ${list}">
    <p class="username" th:text="${review.userNo.userName}"></p>
    <p class="star" th:text="${review.getRevScore()}"></p>
    <p th:text="${review.restNo.restName}"></p>
    <p th:text="${review.restNo.restCategory}"></p>
    <p class="comment" th:text="${review.revContent}"></p>
    <img th:if="${review.base64Image!=''}" th:src="'data:image/png;base64,' + ${review.base64Image}" alt="리뷰 이미지"/>
    <div class="overtip" th:if="${bSlide}==1">
        <span th:text="${review.restNo.restName}"></span>
        <span th:text="${review.restNo.restCategory}"></span>
        <hr>
        <span><a th:href="'/detail?restNo='+${review.restNo.restNo}">식당바로가기</a></span>
        <span th:onclick="|addWishList(this,${review.restNo.restNo})|">관심등록>></span>
    </div>
</div>
    */
    @Query( value= """
            WITH random_category AS (
                 SELECT rest_category
                 FROM dining_rest
                 GROUP BY rest_category
                 ORDER BY RAND()
                 LIMIT 1
            )
            SELECT
                rest_category as category,
                rest_img as img,
                rest_name as name,
                rest_no as no,
                (SELECT avg(r.rev_score) FROM review r WHERE r.rest_no = dr.rest_no) AS score,
                (select r2.rev_img from review r2 where r2.rest_no = dr.rest_no and r2.rev_img != '' order by RAND() limit 1 ) AS revImg
            FROM dining_rest dr
            WHERE rest_category = (SELECT rest_category FROM random_category)
                and rest_img != ''
                and rest_status = 'NORMAL'
            ORDER BY RAND()
            LIMIT :sLimit""", nativeQuery = true)
    List<Tuple> getRandomCategoryList( @Param("sLimit") int sLimit );


}
