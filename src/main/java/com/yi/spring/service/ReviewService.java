package com.yi.spring.service;

import com.yi.spring.entity.Review;
import com.yi.spring.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewService {

    Page<Review> findAll(int page);
    Page<Review> findByUserNoOrderByRevWriteTimeDesc(User user, int page);

    Page<Review> findByRevContentContainingPaged(User user,String revContent, int page);
    Page<Review> findByRestNameContainingPaged(User user,String restName, int page);

//    Page<Review> findByRevContentOrRestNameContainingPaged(User user, String search, int page);



    Page<Review> findByStatus(int page);
}
