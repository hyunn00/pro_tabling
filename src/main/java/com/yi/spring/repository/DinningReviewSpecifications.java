package com.yi.spring.repository;


import com.yi.spring.entity.Dinning;
import com.yi.spring.entity.Review;
import com.yi.spring.entity.meta.DinningReviewView;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class DinningReviewSpecifications {

    public static Specification<Dinning> hasId(Long id) {
        return (root, query, builder) -> id == null ? null : builder.equal(root.get("id"), id);
    }

    public static Specification<Dinning> hasName(String name) {
        return (root, query, builder) -> name == null ? null : builder.equal(root.get("name"), name);
    }

    public static Specification<Dinning> hasAddresses(List<String> addresses) {
        return (root, query, builder) -> addresses == null ? null : root.get("address").in(addresses);
    }

    public static Specification<Dinning> hasPhone(String phone) {
        return (root, query, builder) -> phone == null ? null : builder.equal(root.get("phone"), phone);
    }

    public static Specification<DinningReviewView> joinReviewById() {
        return (root, query, criteriaBuilder) -> {
            Object user = root.get("userNo");
            Join<Review, Dinning> reviewJoin = root.join("restNo", JoinType.INNER);
            return criteriaBuilder.ge(reviewJoin.get("restNo"), 0);
        };
    }
    public static Specification<DinningReviewView> likeRestName(String restName) {
        return (root, query, builder) -> restName == null ? null : builder.like(root.get("restName"), "%"+restName+"%");
    }
    public static Specification<DinningReviewView> eqCategory(String category) {
        return (root, query, builder) -> category == null ? null : builder.equal(root.get("restCategory"), category);
    }

    public static Specification<DinningReviewView> likeAddr(String addr) {
        return (root, query, builder) -> addr == null ? null : builder.like(root.get("restAddr"), "%"+addr+" %");
    }



}


/*
class YourService {

    private final DinningRepository dinningRepository;

    public YourService(DinningRepository dinningRepository) {
        this.dinningRepository = dinningRepository;
    }

    public List<Dinning> findByConditions(Long id, String name, List<String> addresses, String phone) {
        Specification<Dinning> spec = Specification
                .where(DinningReviewSpecifications.hasId(id))
                .and(DinningReviewSpecifications.hasName(name))
                .and(DinningReviewSpecifications.hasAddresses(addresses))
                .and(DinningReviewSpecifications.hasPhone(phone));

        return dinningRepository.findAll(spec);
    }
}
*/