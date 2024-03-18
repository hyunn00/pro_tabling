package com.yi.spring.service;

import com.yi.spring.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.relational.core.sql.In;

import java.util.List;
import java.util.Optional;

public interface UserService {

    List<User> getAllUsers();

    Optional<User> findByUserNo(int userNo);

    Optional<User> findByUserId(String userId);

    long deleteByUserNo(Integer userNo);

    public User updateMenu(User user);

    User updateUser(User user);

//    Page<User> findAll(int page);

    Page<User> findByUserNoPaged(int page);
    Page<User> findByJumNoPaged(int page);

    public Page<User> findByUserNoBlack(int page);

    Page<User> findByUserAuthAndUserNameContainingPaged(String userAuth, String userName, int page);

    public Page<User> findInput(String userAuth, String userName, int page);

    List<User> findByUserAuthAndUserBlockNot(String number, boolean b, int page);
    Page<User> findByUserAuthAndUserNameContainingPagedNoBlack(String userAuth, String userName, int page, Boolean a);




}
