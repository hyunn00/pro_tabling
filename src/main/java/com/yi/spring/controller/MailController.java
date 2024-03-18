package com.yi.spring.controller;

import com.yi.spring.entity.MailVo;
import com.yi.spring.entity.User;
import com.yi.spring.repository.UserRepository;
import com.yi.spring.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class MailController {

    @Autowired
    UserRepository userRepository;

    private final MailService mailService;

    @GetMapping("/CheckMail")
    public String MailPage(){
        return "View";
    }


    @ResponseBody
    @PostMapping("/emailToPw")
    public String checkEmailAndId(String mail, String id, Model model) {

        Optional<User> check = userRepository.findByEmailAndUserId(mail,id);

        if (check.isPresent()) {

            int number = mailService.sendMail(mail);

            String num = "" + number;


            return num;
        } else {

            return "findPw";
        }


    }


    @ResponseBody
    @PostMapping("/emailToId")
    public String checkEmail(String mail, Model model) {

        Optional<User> check = userRepository.findByEmail(mail);

        if (check.isPresent()) {

            int number = mailService.sendMail(mail);

            String num = "" + number;


            return num;
        } else {

            return "findId";
        }

    }

    @ResponseBody
    @PostMapping("/sendEmail")
    public String sendEmail(String mail, Model model) {

            int number = mailService.sendMail2(mail);

            String num = "" + number;

            return num;


    }

    @ResponseBody
    @PostMapping("/mail")
    public String MailSend(String mail){

        int number = mailService.sendMail(mail);

        String num = "" + number;

        return num;
    }


}
