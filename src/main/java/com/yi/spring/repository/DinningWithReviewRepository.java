package com.yi.spring.repository;

import com.yi.spring.entity.meta.DinningReviewView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DinningWithReviewRepository extends JpaRepository<DinningReviewView, Integer>, JpaSpecificationExecutor<DinningReviewView> {

}