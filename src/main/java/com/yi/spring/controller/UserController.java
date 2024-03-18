package com.yi.spring.controller;

import com.yi.spring.OAuth2.OAuth2MemberService;
import com.yi.spring.entity.*;
import com.yi.spring.entity.meta.DinningReviewView;
import com.yi.spring.repository.*;
import com.yi.spring.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Controller
@RequestMapping("user/")
public class UserController {

    @Autowired
    UserService userService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    ReservationRepository reservationRepository;
    @Autowired
    ReviewRepository reviewRepository;
    @Autowired
    private ImgTableRepository imageTableRepository;
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
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    DeleteUserRepository deleteUserRepository;
    @Autowired
    UserLikeRestRepository userLikeRestRepository;
    @Autowired
    DiningRestService diningRestService;


    private User user = null;

    public List<Dinning> getRestaurantsForLatestReservation(Long userNo) {
        PageRequest pageRequest = PageRequest.of(0, 1, Sort.by(Sort.Order.desc("resTime")));
        List<Reservation> latestReservationList = reservationRepository.findLatestReservationByUserNo(userNo, pageRequest);

        if (!latestReservationList.isEmpty()) {
            List<Dinning> dinningList = new ArrayList<>();
            for (Reservation r : latestReservationList)
                dinningList.add(r.getRestNo());
            return dinningList;
//            Long latestRestNo = latestReservationList.get(0).getRestNo();
//            return dinningRepository.findByRestNo(latestRestNo);
        } else {
            // 예약 기록이 없는 경우 처리
            return Collections.emptyList();
        }
    }

    // 유저 컨텐츠 페이지로 이동
    @GetMapping("userPage")
    public String userPageForm1(Principal principal, Model model) {
//        user = userRepository.findByUserId(principal.getName()).get();
        user = o2MemberService.findUser( principal );

        List<Dinning> restaurantsForLatestReservation = getRestaurantsForLatestReservation(Long.valueOf(user.getUserNo()));

        model.addAttribute("user", user);
        model.addAttribute("restaurants", restaurantsForLatestReservation);

        System.out.println(user);
        return "userPage/user_main";
    }

    // 유저가 작성한 포스트 목록 페이지로 이동
    @GetMapping("user_posts")
    public String userPosts(Principal principal, Model model
                            ,@RequestParam(value = "searchInput", required = false) String searchInput
                            ,@RequestParam(value = "filterExpired", defaultValue = "") String filterExpired
                            ,@RequestParam(value = "filterReview", defaultValue = "") String filterReview
                            ,@RequestParam(value = "filterWait", defaultValue = "") String filterWait
                            ,@RequestParam(value = "filterResCompleted", defaultValue = "") String filterResCompleted
                            ,@RequestParam(value = "filterAll", defaultValue = "") String filterAll){
//        User user = userRepository.findByUserId(principal.getName()).orElse(null);
        user = o2MemberService.findUser( principal );
        reservationRepository.updateReservationStatusToReviewWithJoin2();
        Long userNo = Long.valueOf(user.getUserNo());
        List<Reservation> reservations = reservationRepository.findReservationDetailsByUserNo(userNo);

        System.out.println("searchInput => " + searchInput);

        if (user != null || filterAll.equals("All")) {

            if (searchInput != null && !searchInput.isEmpty()) {
                List<Reservation> reservationsList = reservationRepository.findReservationDetailsByUserNoAndRestName(userNo, searchInput);
                model.addAttribute("list", reservationsList);
                reservationService.processReservations(reservations);
                reservationService.checkReservationStatus(reservations, model);
            } else {
//                List<Reservation> reservations = reservationRepository.findReservationDetailsByUserNo(userNo);
                model.addAttribute("list", reservations);
                reservationService.processReservations(reservations);
                reservationService.checkReservationStatus(reservations, model);
            }

            if (filterExpired.equals("expired")){
                List<Reservation> reservationsExpired = reservationRepository.ReservationStatusEXPIRED(userNo);
                reservationService.checkReservationStatus(reservations, model);

                model.addAttribute("list", reservationsExpired);
            }

            if (filterReview.equals("review")){
                List<Reservation> reservationsReview = reservationRepository.ReservationStatusREVIEW(userNo);
                reservationService.checkReservationStatus(reservations, model);

                model.addAttribute("list", reservationsReview);
            }

            if (filterWait.equals("wait")){
                List<Reservation> reservationsWait = reservationRepository.ReservationStatusWAIT(userNo);
                reservationService.checkReservationStatus(reservations, model);

                model.addAttribute("list", reservationsWait);
            }

            if (filterResCompleted.equals("res_completed")){
                List<Reservation> reservationsResCompleted = reservationRepository.ReservationStatusRESERVE_COMPLETED(userNo);
                reservationService.checkReservationStatus(reservations, model);

                model.addAttribute("list", reservationsResCompleted);
            }
        }

        model.addAttribute("main_user", user);
        return "userPage/user_posts";
    }

    @GetMapping("user_posts_detail/{resNo}")
    public String postsDetail(@PathVariable("resNo") int resNo,Principal principal, Model model){
        user = o2MemberService.findUser( principal );
        Long userNo = Long.valueOf(user.getUserNo());

        List<Dinning> restaurantsForLatestReservation = getRestaurantsForLatestReservation(Long.valueOf(user.getUserNo()));
        List<Reservation> reservations = reservationRepository.findReservationDetailsByUserNo(userNo);
        reservationService.checkReservationStatus(reservations, model);

        Optional<Reservation> reservation = reservationRepository.findById(resNo);
        System.out.println("aaaaaaa" + reservation.get().getRestNo().getRestNo());
        int RestNo = reservation.get().getRestNo().getRestNo();

        model.addAttribute("main_user", user);
        model.addAttribute("restaurants", restaurantsForLatestReservation);
        model.addAttribute("res", reservationRepository.findById(resNo));
        model.addAttribute("dinning", diningRestService.getRestByRestNo(RestNo));
        return "userPage/user_posts_detail";
    }

    @PostMapping("submitReview")
    public String reviewAdd(Principal principal,Review review,@RequestParam MultipartFile[] file){
//        user = userRepository.findByUserId(principal.getName()).get();
        user = o2MemberService.findUser( principal );

        if (file[0] != null && !file[0].isEmpty()) {
            try {
                byte[] revImg = file[0].getBytes();
                review.setRevImg(revImg);

                int endIndex = Math.min( 4, file.length );
                MultipartFile[] selectedFiles = Arrays.copyOfRange(file, 0, endIndex);

                review.setRevStrImg( review.getRevImgMan().setReviewImg( imageTableRepository, selectedFiles ) );
            } catch (IOException e) {
                throw new RuntimeException("이미지 업로드 중 오류 발생: " + e.getMessage());
            }
        }


        review.setRevScore((int) (review.getRevScore() * 10));
        review.setUserNo(user);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String formattedDateTime = LocalDateTime.now().format(formatter);
        review.setRevWriteTime(LocalDateTime.parse(formattedDateTime, formatter));
        review.setRevStatus(String.valueOf(ReviewStatus.NORMAL));
        int currenPoint = user.getPoint();
        user.setPoint(currenPoint + 100);

        userRepository.save(user);
        reviewRepository.save(review);
        reservationRepository.updateReservationStatusToReviewWithJoin();
        return "redirect:/user/user_posts";
    }

    // 유저가 작성한 리뷰 목록 페이지로 이동
    @GetMapping("user_review")
    public String userReviews(Principal principal,
                              @RequestParam(value = "page", defaultValue = "0") int page,
                              @RequestParam(value = "selectedOption", required = false) String selectedOption,
                              @RequestParam(value = "searchInput", required = false) String searchInput ,Model model) {
        user = o2MemberService.findUser( principal );
//        System.out.println("search => " + searchInput);

        if (user != null) {
            List<Dinning> restaurantsForLatestReservation = getRestaurantsForLatestReservation(Long.valueOf(user.getUserNo()));
            List<Reservation> reservations = reservationRepository.findReservationDetailsByUserNo(Long.valueOf(user.getUserNo()));
            Page<Review> reviewsPage;
            List<Review> reviewsList;

            if (searchInput != null && !searchInput.isEmpty()) {
                if (Objects.equals(selectedOption, "content")){
                    reviewsPage = reviewService.findByRevContentContainingPaged(user, searchInput, page);
                } else {
                    reviewsPage = reviewService.findByRestNameContainingPaged(user, searchInput, page);
                }
                reviewsList = reviewsPage.getContent();
                model.addAttribute("userNoCount", reviewsList.size());
                if (reviewsList.isEmpty()) {
                    System.out.println("검색 결과가 없습니다.");
                    model.addAttribute("noResultsMessage", "검색 결과가 없습니다.");
                }
            } else {
                reviewsPage = reviewService.findByUserNoOrderByRevWriteTimeDesc(user, page);
                reviewsList = reviewsPage.getContent();
                long userNoCount = reviewRepository.countByUserNo(user);
                model.addAttribute("userNoCount", userNoCount);
            }

            reservationService.checkReservationStatus(reservations, model);

            model.addAttribute("main_user", user);
            model.addAttribute("restaurants", restaurantsForLatestReservation);
            model.addAttribute("list", reservations);


            List<String> timeAgoList = new ArrayList<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");
            LocalDateTime currentTime = LocalDateTime.now();

            for (Review review : reviewsList) {
                if (review.getRevWriteTime() != null) {
                    LocalDateTime reviewTime = review.getRevWriteTime();
                    long minutesAgo = ChronoUnit.MINUTES.between(reviewTime, currentTime);

                    if (minutesAgo < 60) {
                        timeAgoList.add((minutesAgo == 0) ? "방금전" : minutesAgo + "분전");
                    } else {
                        long hoursAgo = ChronoUnit.HOURS.between(reviewTime, currentTime);

                        if (hoursAgo < 24) {
                            timeAgoList.add(hoursAgo + "시간전");
                        } else {
                            long daysAgo = ChronoUnit.DAYS.between(reviewTime, currentTime);

                            if (daysAgo < 7) {
                                timeAgoList.add(daysAgo + "일전");
                            } else {
                                long weeksAgo = daysAgo / 7;

                                if (weeksAgo < 5) {
                                    timeAgoList.add(weeksAgo + "주전");
                                } else {
                                    long monthsAgo = daysAgo / 30;

                                    if (monthsAgo < 12) {
                                        timeAgoList.add(monthsAgo + "개월전");
                                    } else {
                                        long yearsAgo = monthsAgo / 12;
                                        timeAgoList.add(yearsAgo + "년전");
                                    }
                                }
                            }
                        }
                    }
                } else {
                    timeAgoList.add("작성 시간 없음");
                }
            }

            List<Map<String, Object>> combinedList = new ArrayList<>();
            for (int i = 0; i < reviewsList.size(); i++) {
                Map<String, Object> combinedItem = new HashMap<>();
                combinedItem.put("rev", reviewsList.get(i));
                combinedItem.put("timeAgo", timeAgoList.get(i));
                combinedList.add(combinedItem);
            }

            // 리뷰 + 작성시간 데이터
            model.addAttribute("combinedList", combinedList);
            // 페이징 용
            model.addAttribute("reviewsPage", reviewsPage);
            model.addAttribute("encodedSearchInput", searchInput);
            model.addAttribute("selectedOption", selectedOption);

            System.out.println("searchInput => " + searchInput);
        }

        return "userPage/user_review";
    }
    @GetMapping("deleteReview/{revNo}")
    public String reviewDelete(Principal principal,@PathVariable("revNo") int revNo) {
        user = o2MemberService.findUser( principal );

        int currenPoint = user.getPoint();
        user.setPoint(currenPoint - 100);
        reviewRepository.deleteById(revNo);
        reservationRepository.updateReservationStatusToReviewWithJoin2();
        return "redirect:/user/user_review";
    }
    @GetMapping("updateReviewForm/{revNo}")
    public String reviewUpdateForm(Principal principal,@PathVariable("revNo") int revNo, Model model){
        user = o2MemberService.findUser( principal );
        Long userNo = Long.valueOf(user.getUserNo());

        List<Dinning> restaurantsForLatestReservation = getRestaurantsForLatestReservation(Long.valueOf(user.getUserNo()));
        List<Reservation> reservations = reservationRepository.findReservationDetailsByUserNo(userNo);
        reservationService.checkReservationStatus(reservations, model);

        model.addAttribute("review", reviewRepository.findById(revNo));
        model.addAttribute("main_user", user);
        model.addAttribute("restaurants", restaurantsForLatestReservation);

        return "userPage/user_reviewUpdate";
    }
    @PostMapping("updateReview/{revNo}")
    public String reviewUpdate(Principal principal,Review review,@RequestParam MultipartFile[] file,@PathVariable("revNo") int revNo){
        user = o2MemberService.findUser( principal );

        System.out.println("file ->" + file[0]);
        Review existReview = reviewRepository.findById( revNo ).orElse( null );

        if (file[0] != null && !file[0].isEmpty()) {
            try {
                byte[] revImg = file[0].getBytes();
                review.setRevImg(revImg);

                int endIndex = Math.min( 4, file.length );
                MultipartFile[] selectedFiles = Arrays.copyOfRange(file, 0, endIndex);

                review.setRevStrImg( review.getRevImgMan().setReviewImg( imageTableRepository, selectedFiles ) );
            } catch (IOException e) {
                throw new RuntimeException("이미지 업로드 중 오류 발생: " + e.getMessage());
            }
        } else
        {
            review.setRevImg( existReview.getRevImg());
            review.setRevStrImg( existReview.getRevStrImg());
        }
        review.setRevScore((int) (review.getRevScore() * 10));
        review.setRevStatus(String.valueOf(ReviewStatus.NORMAL));


        review.setId(revNo);
        reviewRepository.save(review);
//        reservationRepository.updateReservationStatusToReviewWithJoin();
        return "redirect:/user/user_review";
    }


    // 유저 정보 페이지로 이동
    @GetMapping("user_info")
    public String userUpdateForm(Principal principal, Model model) {
//        user = userRepository.findByUserId(principal.getName()).get();
        user = o2MemberService.findUser( principal );

        List<Dinning> restaurantsForLatestReservation = getRestaurantsForLatestReservation(Long.valueOf(user.getUserNo()));

        Long userNo = Long.valueOf(user.getUserNo());
        List<Reservation> reservations = reservationRepository.findReservationDetailsByUserNo(userNo);
        reservationService.checkReservationStatus(reservations, model);

        String hiddenPassword = "********";
        model.addAttribute("hiddenPassword", hiddenPassword);
        model.addAttribute("main_user", user);
        model.addAttribute("restaurants", restaurantsForLatestReservation);
        model.addAttribute("user", userService.findByUserNo(user.getUserNo()));
        return "userPage/user_info";
    }
    // 유저 정보 업데이트를 처리하는 POST 요청 처리
    @PostMapping("user_update")
    public String userUpdate(Principal principal, @RequestParam MultipartFile file,
                             @RequestParam String userName,
                             @RequestParam String userId,
                             @RequestParam String userEmail,
                             @RequestParam(required = false) String userPassword,
                             @RequestParam String userTel
    ) throws IOException {
        user = o2MemberService.findUser(principal);
        User existUser = userService.findByUserNo(user.getUserNo()).orElse(null);

        if (file != null && !file.isEmpty()) {
            try {
                byte[] revImg = file.getBytes();
                user.setUserImg(revImg);
            } catch (IOException e) {
                throw new RuntimeException("이미지 업로드 중 오류 발생: " + e.getMessage());
            }
        } else
        {
            user.setUserImg( existUser.getUserImg());
        }

        if ( null!= userPassword && !userPassword.isEmpty())
            user.setUserPassword(userPassword);
        else
            user.setUserPassword(existUser != null ? existUser.getUserPassword() : null);

        user.setUserName(userName);
        user.setUserId(userId);
        user.setUserEmail(userEmail);
        user.setUserTel(userTel);

        userRepository.save(user);
        return "redirect:/user/user_info";
    }

    @PostMapping("/uploadImg")
    public String uploadImg(Principal principal,@RequestParam MultipartFile file){
        user = o2MemberService.findUser( principal );

        if (file != null && !file.isEmpty()) {
            try {
                byte[] userImg = file.getBytes();
                user.setUserImg(userImg);
            } catch (IOException e) {
                throw new RuntimeException("이미지 업로드 중 오류 발생: " + e.getMessage());
            }
        }
        userRepository.save(user);
        return "redirect:/user/user_info";
    }
    @PostMapping("/updatePw")
    public String updatePw(Principal principal, @RequestParam String newPassword, @RequestParam String oldPw){
        user = o2MemberService.findUser( principal );

        if (passwordEncoder.matches(oldPw,user.getUserPassword())){
            user.setUserPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
        } else {
            throw new RuntimeException("password different");
        }

        return "redirect:/user/user_info";
    }

    @PostMapping("/deleteUser")
    public String deleteUser(Principal principal, DeleteUser deleteUser, @RequestParam String pw){
        user = o2MemberService.findUser( principal );

        if (passwordEncoder.matches(pw,user.getUserPassword())){
            deleteUser.setUserNo(user.getUserNo());
            deleteUser.setUserId(user.getUserId());
            deleteUser.setUserAuth(user.getUserAuth());
            deleteUser.setUserStartDate(user.getUserStartDate());
            deleteUser.setUserBlock(user.isUserBlock());
            reviewRepository.deleteAllByUserNo(user);
            reservationRepository.deleteAllByUserNo(user);
            qaRepository.deleteAllByUserNo(user);
            userLikeRestRepository.deleteAllByUserNo(user);

            deleteUserRepository.save(deleteUser);
            userRepository.delete(user);
        } else {
            throw new RuntimeException("password different");
        }

        return "redirect:/logout";
    }


    @GetMapping("/userLikeAdd")
    public ResponseEntity<String> userLikeAdd(Principal principal,Reservation reservation ,UserLikeRest userLikeRest, @RequestParam int likeRestNo) {
        user = o2MemberService.findUser(principal);

        UserLikeRest existingLike = userLikeRestRepository.findByUserNoAndRestNo(user, likeRestNo);
        List<Reservation> likes =  reservationRepository.findByUserNo_UserNoAndRestNo_RestNo(user.getUserNo(), likeRestNo);

        for (Reservation like : likes) {
            if (existingLike != null) {
                userLikeRestRepository.delete(existingLike);
                reservationRepository.save(reservation);
                like.setResLike(false);
                reservationRepository.save(like);
            } else {
                userLikeRest.setUserNo(user);
                userLikeRest.setRestNo(likeRestNo);
                userLikeRestRepository.save(userLikeRest);
                like.setResLike(true);
                reservationRepository.save(like);
            }
        }
        return new ResponseEntity<>("Like 작동", HttpStatus.OK);
    }

    @GetMapping("/userlike")
    public String userLike(Principal principal, Model model){
        user = o2MemberService.findUser( principal );
        Long userNo = Long.valueOf(user.getUserNo());

        List<Dinning> restaurantsForLatestReservation = getRestaurantsForLatestReservation(Long.valueOf(user.getUserNo()));
        List<Reservation> reservations = reservationRepository.findReservationDetailsByUserNo(userNo);
        reservationService.checkReservationStatus(reservations, model);


        List<Dinning> userLikeList = userLikeRestRepository.findAllDiningRestsLikedByUsers(user);

        for (Dinning userLikeLists : userLikeList) {
            Optional<Dinning> dinningOptional = dinningRepository.findById((long) userLikeLists.getRestNo());
            List<Review> list = reviewRepository.findByRestNo(dinningOptional.get());
            System.out.println("listsize => " + list.size());

            double sum = list.stream().mapToDouble(Review::getRevScore).sum();
            double result = sum / list.size();

            DecimalFormat decimalFormat = new DecimalFormat("#.#");
            userLikeLists.setRestScore(Double.parseDouble(decimalFormat.format(result)));
            userLikeLists.setTotalReviews(list.size());
        }

        model.addAttribute("main_user", user);
        model.addAttribute("restaurants", restaurantsForLatestReservation);
        model.addAttribute("userLike", userLikeList);
//        System.out.println(userLikeRestRepository.findAllDiningRestsLikedByUsers(user));

        return "userPage/user_like";
    }




}