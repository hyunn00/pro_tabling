package com.yi.spring.controller;

import com.yi.spring.OAuth2.OAuth2MemberService;
import com.yi.spring.entity.*;
import com.yi.spring.repository.*;
import com.yi.spring.service.QAService;
import com.yi.spring.service.ReservationService;
import com.yi.spring.service.ReviewService;
import com.yi.spring.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Controller
@RequestMapping("/")
public class LoginController {

    @Autowired
    UserService userService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    ReservationRepository reservationRepository;
    @Autowired
    ReviewRepository reviewRepository;
    @Autowired
    QARepository qaRepository;
    @Autowired
    QAService qaService;
    @Autowired
    DinningRepository dinningRepository;
    @Autowired
    ReservationService reservationService;
    @Autowired
    ReviewService reviewService;
    @Autowired
    private OAuth2MemberService o2MemberService;


    @GetMapping("/findId")
    public String findId(Model model){




        return "findId";
    }

    @GetMapping("/findPw")
    public String findPw(Model model){




        return "findPw";
    }

    @GetMapping("/Pwcheck")
    public String Pwcheck(@RequestParam String id,
            Model model){




        return "Pwcheck";
    }

    @PostMapping("/UpdatePw")
    public String UpdatePw(@RequestParam String id,
                           @RequestParam String newPassword,
                           Model model){


        Optional<User> userOptional = userRepository.findByUserId(id);

        userOptional.ifPresent(user -> {

            user.setUserPassword(newPassword);
            userRepository.save(user);
        });


        return "login";
    }

    @PostMapping("/check_duplicate")
    @ResponseBody
    public Map<String, Boolean> checkDuplicate(@RequestBody Map<String, String> requestBody) {
        String userId = requestBody.get("userId");
        Map<String, Boolean> response = new HashMap<>();

        User user = userRepository.findByUserId(userId).orElse(null);

        response.put("available", user == null);
        return response;
    }
}