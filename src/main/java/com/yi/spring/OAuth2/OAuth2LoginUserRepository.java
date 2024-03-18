package com.yi.spring.OAuth2;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

public interface OAuth2LoginUserRepository extends JpaRepository<OAuth2LoginUser, Long> {
    Optional<OAuth2LoginUser> findByEmailAndOauthType(String email, String oauthType);

}
