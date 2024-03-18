package com.yi.spring.service;

import com.yi.spring.entity.Review;
import com.yi.spring.entity.User;
import com.yi.spring.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    ReviewRepository reviewRepository;

    @Override
    public Page<Review> findAll(int page) {
        Pageable pageable = PageRequest.of(page, 5);
        return this.reviewRepository.findAll(pageable);
    }

    @Override
    public Page<Review> findByUserNoOrderByRevWriteTimeDesc(User user, int page) {
        List<Review> userReviews = reviewRepository.findByUserNoOrderByRevWriteTimeDesc(user);
        int pageSize = 10;
        int start = page * pageSize;
        int end = Math.min((page + 1) * pageSize, userReviews.size());

        return new PageImpl<>(userReviews.subList(start, end), PageRequest.of(page, pageSize), userReviews.size());
    }

    @Override
    public Page<Review> findByRevContentContainingPaged(User user, String revContent, int page) {
        Pageable pageable = PageRequest.of(page, 10);
        return reviewRepository.findByUserNoAndRevContentContainingOrderByRevWriteTimeDesc(user, revContent, pageable);
    }

    @Override
    public Page<Review> findByRestNameContainingPaged(User user, String restName, int page) {
        Pageable pageable = PageRequest.of(page, 10);
        return reviewRepository.findByUserNoAndRestNoRestNameContainingOrderByRevWriteTimeDesc(user, restName, pageable);
    }


    @Override
    public Page<Review> findByStatus(int page) {
        Pageable pageable = PageRequest.of(page, 10);
        return reviewRepository.findByRevStatus(pageable);
    }

//    @Override
//    public Page<Review> findByRevContentOrRestNameContainingPaged(User user, String search, int page) {
//        Pageable pageable = PageRequest.of(page, 10);
//        return reviewRepository.findByUserNoAndRevContentAndRestNoRestNameContaining(user, search, pageable);
//    }

//    @Override
//    public Page<Review> findByUserNoAndRestNoRestNameContaining(User user, String restName, Pageable pageable) {
//        return null;
//    }

}
