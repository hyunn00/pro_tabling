package com.yi.spring.controller;

import com.yi.spring.OAuth2.OAuth2MemberService;
import com.yi.spring.entity.*;
import com.yi.spring.repository.QARepository;
import com.yi.spring.repository.QaAnswerRepository;
import com.yi.spring.repository.ReservationRepository;
import com.yi.spring.repository.UserRepository;
import com.yi.spring.service.QAService;
import com.yi.spring.service.ReservationService;
import com.yi.spring.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("QA/")
public class QaController {

    @Autowired
    QaAnswerRepository qaAnswerRepository;
    @Autowired
    QARepository qaRepository;
    @Autowired
    QAService qaService;
    @Autowired
    UserService userService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    ReservationRepository reservationRepository;
    @Autowired
    ReservationService reservationService;
    @Autowired
    private OAuth2MemberService o2MemberService;

    private User user = null;

    public List<Dinning> getRestaurantsForLatestReservation(Long userNo){
        PageRequest pageRequest = PageRequest.of(0, 1, Sort.by(Sort.Order.desc("resTime")));
        List<Reservation> latestReservationList = reservationRepository.findLatestReservationByUserNo(userNo, pageRequest);

        if (!latestReservationList.isEmpty()) {
            List<Dinning> dinningList = new ArrayList<>();
            for ( Reservation r : latestReservationList )
                dinningList.add( r.getRestNo() );
            return dinningList;
//            Long latestRestNo = latestReservationList.get(0).getRestNo();
//            return dinningRepository.findByRestNo(latestRestNo);
        } else {
            // 예약 기록이 없는 경우 처리
            return Collections.emptyList();
        }
    }

    // 유저가 작성한 Q&A 목록 페이지로 이동
    @GetMapping("user_qa")
    public String userQA(Principal principal, @RequestParam(value = "page", defaultValue = "0") int page , Model model) {
//        user = userRepository.findByUserId(principal.getName()).get();
        user = o2MemberService.findUser( principal );
        Long userNo = Long.valueOf(user.getUserNo());

        List<Dinning> restaurantsForLatestReservation = getRestaurantsForLatestReservation(Long.valueOf(user.getUserNo()));
        List<Reservation> reservations = reservationRepository.findReservationDetailsByUserNo(userNo);
        reservationService.checkReservationStatus(reservations, model);

        Page<QA> paging = this.qaService.findByUserNoPaged(user, page);
        List<QaAnswer> answer = qaAnswerRepository.findQaAnswer(userNo);

        model.addAttribute("main_user", user);
        model.addAttribute("restaurants", restaurantsForLatestReservation);
        model.addAttribute("Num", user.getUserNo());
        model.addAttribute("QA", paging);

        return "userPage/user_QA";
    }

    @GetMapping("user_qa_selete")
    public String userQASelete(Principal principal, Model model) {
        user = o2MemberService.findUser( principal );
        Long userNo = Long.valueOf(user.getUserNo());

        List<Dinning> restaurantsForLatestReservation = getRestaurantsForLatestReservation(Long.valueOf(user.getUserNo()));
        List<Reservation> reservations = reservationRepository.findReservationDetailsByUserNo(userNo);
        reservationService.checkReservationStatus(reservations, model);

        model.addAttribute("main_user", user);
        model.addAttribute("restaurants", restaurantsForLatestReservation);
        model.addAttribute("Num", user.getUserNo());

        return "userPage/user_QA_selete";
    }

    // 유저가 Q&A를 추가하는 페이지로 이동
    @GetMapping("user_qa_form")
    public String userQAUpdateForm(Principal principal, Model model){
        user = o2MemberService.findUser( principal );
        Long userNo = Long.valueOf(user.getUserNo());

        List<Dinning> restaurantsForLatestReservation = getRestaurantsForLatestReservation(Long.valueOf(user.getUserNo()));
        List<Reservation> reservations = reservationRepository.findReservationDetailsByUserNo(userNo);
        reservationService.checkReservationStatus(reservations, model);

        model.addAttribute("main_user", user);
        model.addAttribute("restaurants", restaurantsForLatestReservation);
        model.addAttribute("QA_userNo",userService.findByUserNo(user.getUserNo()));
        return "userPage/user_QA_form";
    }

    @GetMapping("Qa_answer/{qaNo}")
    public String QaAnswer(Principal principal, @PathVariable("qaNo") int qaNo, Model model) {
        user = o2MemberService.findUser( principal );
        Long userNo = Long.valueOf(user.getUserNo());

        List<Dinning> restaurantsForLatestReservation = getRestaurantsForLatestReservation(Long.valueOf(user.getUserNo()));
        List<Reservation> reservations = reservationRepository.findReservationDetailsByUserNo(userNo);
        reservationService.checkReservationStatus(reservations, model);

        model.addAttribute("QA", qaRepository.findById(qaNo));
        Optional<QaAnswer> qaAnswerOptional = qaAnswerRepository.findByQaNo(qaNo);
        model.addAttribute("QaAnswer", qaAnswerOptional.orElse(null));
        model.addAttribute("main_user", user);
        model.addAttribute("restaurants", restaurantsForLatestReservation);


        System.out.println("문의 내용 --> " + qaRepository.findById(qaNo));
        System.out.println("답변 내용 --> " + qaAnswerRepository.findByQaNo(qaNo));


        return "userPage/user_QA_answer_form";
    }

    // 유저가 Q&A를 추가하는 POST 요청 처리
    @PostMapping("user_qa_add")
    public String userQAUpdate(@RequestParam("userNo") User userNo,
                               @RequestParam("qa_title") String qaTitle,
                               @RequestParam("qa_content") String qaContent) {
        QA qa = new QA();
        qa.setUserNo(userNo);
        qa.setQaTitle(qaTitle);
        qa.setQaContent(qaContent);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String formattedDateTime = LocalDateTime.now().format(formatter);
        qa.setQaWriteTime(LocalDateTime.parse(formattedDateTime, formatter));
        qaRepository.save(qa);

        int user_no = userNo.getUserNo();
        return "redirect:/QA/user_qa";
    }

    @GetMapping("deleteQA/{qaNo}")
    public String QADelete(@PathVariable("qaNo") int qaNo) {
        qaRepository.deleteById(qaNo);
        return "redirect:/QA/user_qa";
    }

}
