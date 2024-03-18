package com.yi.spring.controller;

import com.yi.spring.entity.*;
import com.yi.spring.entity.meta.ImageFrom;
import com.yi.spring.repository.DeleteUserRepository;
import com.yi.spring.repository.ImgTableRepository;
import com.yi.spring.repository.ReservationRepository;
import com.yi.spring.repository.ReviewRepository;
import com.yi.spring.service.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
@RequestMapping("/owner/*")
@RequiredArgsConstructor
public class OwnerController {
    private final UserService userService;
    private final DiningRestService diningRestService;
    private final MenuService menuService;
    private final ReservationRepository reservationRepository;
    private final EventService eventService;
    private final ImgTableRepository imageTableRepository;
    private final ReviewRepository reviewRepository;
    private final DeleteUserRepository deleteUserRepository;
    private final SendMessage sendMessage;
    private final PasswordEncoder passwordEncoder;

    private User loginUser;
    private Dinning dinning;

    private List<Reservation> status(List<Reservation> list) {
        LocalDateTime currentDateTime = LocalDateTime.now();

        for (Reservation reservation : list) {
            Timestamp resTimestamp = Timestamp.valueOf(reservation.getResTime());

            if (ReservationStatus.RESERVE_COMPLETED.name().equals(reservation.getResStatus())) {
                if (currentDateTime.isAfter(resTimestamp.toLocalDateTime())) {
                    reservation.setResStatus(ReservationStatus.EXPIRED.name());
                    reservationRepository.save(reservation);
                }
            } else if (ReservationStatus.WAIT.name().equals(reservation.getResStatus())) {
                if (currentDateTime.isAfter(resTimestamp.toLocalDateTime())) {
                    reservation.setResStatus(ReservationStatus.REST_CANCEL.name());
                    reservationRepository.save(reservation);
                }
            }
        }
        return list;
    }

    private List<String> getTimeList() {

        List<String> timeSlots = new ArrayList<>();

        int startHour = 0;
        int endHour = 24;

        while (startHour != endHour) {
            timeSlots.add(String.format("%02d:00", startHour));
            startHour++;
        }
        timeSlots.add(String.format("%02d:00", endHour));

        return timeSlots;
    }

    private TreeMap<String, Integer> getStringIntegerTreeMap(List<Reservation> rList) {
        List<String> timeList = getTimeList();
        TreeMap<String, Integer> reserveStat = new TreeMap<>();
        for(String time : timeList) {
            reserveStat.put(time, 0);
        }
        for (Reservation reservation : rList) {
            String key = reservation.getResTime().toLocalTime().toString();
            String formattedKey = String.format("%02d:00", Integer.parseInt(key.split(":")[0]));
            Integer statCount = reserveStat.computeIfAbsent(formattedKey, k -> 0);
            statCount++;
            reserveStat.put(formattedKey, statCount);
        }
        return reserveStat;
    }

    private TreeMap<String, Integer> getStringIntegerTreeMapByPeriod(List<Object[]> mapParam, String period) {


        TreeMap<String, Integer> inputMap = new TreeMap<>();
        for ( Object[] item : mapParam )
        {
            inputMap.put(item[0].toString(), Math.toIntExact((Long) item[1]));
        }


        LocalDate today = LocalDate.now();
        TreeMap<String, Integer> reserveMap = new TreeMap<>();
        switch(period) {
            case "1 week" :
                LocalDate oneWeekAgo = today.minusWeeks(1);
                while (!oneWeekAgo.isAfter(today)) {
                    Integer value = inputMap.get(oneWeekAgo.toString());
                    reserveMap.put(oneWeekAgo.toString(), Objects.requireNonNullElse(value, 0));
                    oneWeekAgo = oneWeekAgo.plusDays(1);
                }
                break;
            case "1 month" :
                LocalDate oneMonthAgo = today.minusMonths(1);
                while (!oneMonthAgo.isAfter(today)) {
                    Integer value = inputMap.get(oneMonthAgo.toString());
                    reserveMap.put(oneMonthAgo.toString(), Objects.requireNonNullElse(value, 0));
                    oneMonthAgo = oneMonthAgo.plusDays(1);
                }
                break;
            case "3 month" :
                LocalDate threeMonthAgo = today.minusMonths(3);
                while (!threeMonthAgo.isAfter(today)) {
                    Integer value = inputMap.get(threeMonthAgo.toString());
                    reserveMap.put(threeMonthAgo.toString(), Objects.requireNonNullElse(value, 0));
                    threeMonthAgo = threeMonthAgo.plusDays(1);
                }
                break;
        }
        return reserveMap;
    }

    @GetMapping("home")
    public String home(Principal principal, Model model) {
        loginUser = userService.findByUserId( principal.getName() ).get();
        model.addAttribute("user", loginUser);

        dinning = diningRestService.getByUserNo(loginUser);
        model.addAttribute("dinning", dinning);

        model.addAttribute("pageName", "DASH BOARD");

        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        model.addAttribute("today", today);

        if(dinning!=null) {
            List<Reservation> reservations = reservationRepository.getTodayReservation((long) dinning.getRestNo());
            int complete = 0;
            int cancel = 0;
            for (Reservation reserv : reservations) {
                if (reserv.getResStatus().equals(String.valueOf(ReservationStatus.RESERVE_COMPLETED)) ||
                        reserv.getResStatus().equals(String.valueOf(ReservationStatus.EXPIRED))) {
                    complete++;
                } else if (reserv.getResStatus().equals(String.valueOf(ReservationStatus.USER_CANCEL)) ||
                        reserv.getResStatus().equals(String.valueOf(ReservationStatus.REST_CANCEL)) ||
                        reserv.getResStatus().equals(String.valueOf(ReservationStatus.NO_SHOW))) {
                    cancel++;
                }
            }
            model.addAttribute("complete", complete);
            model.addAttribute("cancel", cancel);

            int reviewCount = reviewRepository.getCountReviewLately7Days((long) dinning.getRestNo()).size();
            model.addAttribute("reviewCount", reviewCount);

            // List<Reservation> rList = reservationRepository.findAll();
            List<Reservation> timeList = reservationRepository.findByRestNo_RestNo((long) dinning.getRestNo());
            TreeMap<String, Integer> reserveStat = getStringIntegerTreeMap(timeList);

            model.addAttribute("reserveStat", reserveStat);

            TreeMap<String, Integer> week1List = getStringIntegerTreeMapByPeriod(reservationRepository.getReservationCountsByInterval("1 week",(long) dinning.getRestNo()), "1 week");
            TreeMap<String, Integer> month1List = getStringIntegerTreeMapByPeriod(reservationRepository.getReservationCountsByInterval("1 month", (long) dinning.getRestNo()), "1 month");
            TreeMap<String, Integer> month3List = getStringIntegerTreeMapByPeriod(reservationRepository.getReservationCountsByInterval("3 month", (long) dinning.getRestNo()), "3 month");

            model.addAttribute("week1List", week1List);
            model.addAttribute("month1List", month1List);
            model.addAttribute("month3List", month3List);

            model.addAttribute("drawGraph", true);
        }



        return "owner/home";
    }

    @GetMapping("addRest")
    public String addRest(Principal principal, Model model) {
        User loginUser = userService.findByUserId( principal.getName() ).get();
        model.addAttribute("user", loginUser);
        model.addAttribute("pageName", "식당 등록");
        return "owner/addRest";
    }

    @PostMapping("addRest") // 등록 화면으로 전환은 되는데 등록은 안됨
    public String addRest(Principal principal, Dinning dinning, Model model, @RequestParam MultipartFile file) {
        User loginUser = userService.findByUserId( principal.getName() ).get();
        if(!file.isEmpty()) {
            byte[] restImg;
            try {
                restImg = file.getBytes();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            dinning.setRestImg( dinning.getRestImgMan().setRestImg(imageTableRepository, ImageFrom.REST, restImg) );
        }
        dinning.setUserNo(loginUser);
        dinning.setRestStatus(String.valueOf(DinningStatus.HOLD));
        dinning.setRestStartDate(String.valueOf(LocalDateTime.now()));
        diningRestService.createRestaurant(dinning);
        return "redirect:/owner/viewRest";
    }

    // -------------------------- 가게 상세보기 및 메뉴 관리 -----------------------------------------
    @GetMapping("viewRest")
    public String viewRest(Principal principal, Model model) {
        User loginUser = userService.findByUserId( principal.getName() ).get();
        model.addAttribute("user", loginUser);

        Dinning dinning = diningRestService.getByUserNo(loginUser);
        System.out.println(dinning);

        if(dinning == null) {
            model.addAttribute("msg", "등록된 가게가 없습니다. 이 기능은 가게 등록 후 사용 가능한 메뉴입니다.");
            model.addAttribute("location", "/owner/addRest");
            return "redirect:/owner/addRest";
        }
        model.addAttribute("dinning", dinning);


        List<Menu> menuList = menuService.getMenusByRestNo(dinning.getRestNo());
        model.addAttribute("menuList", menuList);

        model.addAttribute("menuPage", "/menu/listMenu");
        model.addAttribute("pageName", "식당 정보 확인");
        return "owner/viewRest";
    }

    @GetMapping("viewRest/addMenu")
    public String createMenuForm(Principal principal, Model model) {
        User loginUser = userService.findByUserId( principal.getName() ).get();
        model.addAttribute("user", loginUser);

        Dinning dinning = diningRestService.getByUserNo(loginUser);
        model.addAttribute("dinning", dinning);

        model.addAttribute("menuPage", "/menu/addMenu");
        model.addAttribute("pageName", "식당 정보 확인");
        return "/owner/viewRest";
    }
    @PostMapping("viewRest/addMenu")
    public String createMenu(Principal principal, Menu menu, Model model) {
        User loginUser = userService.findByUserId( principal.getName() ).get();

        model.addAttribute("user", loginUser);

        Dinning dinning = diningRestService.getByUserNo(loginUser);
        model.addAttribute("dinning", dinning);

        menu.setRestNo(dinning);
        Menu savedMenu = menuService.createMenu(menu);

        return "redirect:/owner/viewRest";
    }

    @GetMapping("viewRest/updateMenu/{menuNo}")
    public String updateMenu(Principal principal, @PathVariable("menuNo") int menuNo, Model model) {
        User loginUser = userService.findByUserId( principal.getName() ).get();

        model.addAttribute("user", loginUser);

        Dinning dinning = diningRestService.getByUserNo(loginUser);
        model.addAttribute("dinning", dinning);

        Menu menu = menuService.getMenuByMenuNo(menuNo);
        model.addAttribute("menu", menu);
        model.addAttribute("menuPage", "/menu/updateMenu");
        model.addAttribute("pageName", "식당 정보 확인");
        return "/owner/viewRest";
    }

    @PostMapping("viewRest/updateMenu/{menuNo}")
    public String updateMenu(Principal principal, @PathVariable("menuNo") int menuNo, Menu Menu, Model model) {
        User loginUser = userService.findByUserId( principal.getName() ).get();

        model.addAttribute("user", loginUser);

        Dinning dinning = diningRestService.getByUserNo(loginUser);
        model.addAttribute("dinning", dinning);

        Menu.setMenuNo(menuNo);
        Menu updateMenu = menuService.updateMenu(Menu);
        return "redirect:/owner/viewRest";
    }

    @GetMapping("viewRest/deleteMenu/{menuNo}")
    public String deleteMenu(Principal principal, @PathVariable("menuNo") int menuNo, Model model) {
        User loginUser = userService.findByUserId( principal.getName() ).get();

        model.addAttribute("user", loginUser);

        Dinning dinning = diningRestService.getByUserNo(loginUser);
        model.addAttribute("dinning", dinning);

        menuService.deleteMenu(menuNo);
        return "redirect:/owner/viewRest";
    }

    // ------------------------------------------------------------------------------------------

    @GetMapping("updateRest")
    public String updateRest(Principal principal, Model model) {
        User loginUser = userService.findByUserId( principal.getName() ).get();

        model.addAttribute("user", loginUser);

        Dinning dinning = diningRestService.getByUserNo(loginUser);
        model.addAttribute("dinning", dinning);
        model.addAttribute("pageName", "식당 정보 수정");
        return "/owner/updateRest";
    }

    @PostMapping("updateRest")
    public String updateRest(Principal principal, Dinning dinning, @RequestParam MultipartFile file) {
        User loginUser = userService.findByUserId( principal.getName() ).get();
        Dinning existRest = diningRestService.getByUserNo(loginUser);
        byte[] restImg;
        if(!file.isEmpty()) {
            try {
                restImg = file.getBytes();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            dinning.setRestImg( dinning.getRestImgMan().setRestImg(imageTableRepository, ImageFrom.REST, restImg) );
        }
        else
        {
            dinning.setRestImg( existRest.getRestImg() );
        }
        dinning.setUserNo(loginUser);
        if(existRest.getRestStatus().equals(String.valueOf(DinningStatus.REJECT))) dinning.setRestStatus(String.valueOf(DinningStatus.HOLD));
        else dinning.setRestStatus(existRest.getRestStatus());
        Dinning updateRestaurant = diningRestService.updateRestaurant(dinning);
        return "redirect:/owner/viewRest";
    }

    @GetMapping("deleteRest")
    public String deleteRestForm(Principal principal, Model model) {
        User loginUser = userService.findByUserId( principal.getName() ).get();
        model.addAttribute("user", loginUser);
        Dinning dinning = diningRestService.getByUserNo(loginUser);
        if(dinning == null) {
            model.addAttribute("msg", "등록된 가게가 없습니다. 이 기능은 가게 등록 후 사용 가능한 메뉴입니다.");
            model.addAttribute("location", "/owner/addRest");
            return "owner/transit";
        }
        model.addAttribute("dinning", dinning);
        model.addAttribute("pageName", "식당 폐점 신청");
        return "owner/deleteRest";
    }
    @PostMapping("deleteRest") // 삭제하면 외래키 에러 발생
    public String deleteRest(Principal principal, Model model) {
        User loginUser = userService.findByUserId(principal.getName()).get();
        Dinning dinning = diningRestService.getByUserNo(loginUser);

        Dinning updatedDinning = diningRestService.deleteApply(dinning.getRestNo());
        System.out.println(updatedDinning);

        return "redirect:/owner/home";
    }

    // ----------------------- 예약 관련 ------------------------------
    @GetMapping("reservList")
    public String reservList(Principal principal, Model model) {
        User loginUser = userService.findByUserId( principal.getName() ).get();
        model.addAttribute("user", loginUser);

        Dinning dinning = diningRestService.getByUserNo(loginUser);
        if(dinning == null) {
            model.addAttribute("msg", "등록된 가게가 없습니다. 이 기능은 가게 등록 후 사용 가능한 메뉴입니다.");
            model.addAttribute("location", "/owner/addRest");
            return "owner/transit";
        }
        model.addAttribute("dinning", dinning);

        List<Reservation> reserveList = status(reservationRepository.getReservationWithDateType((long) dinning.getRestNo()));
        reserveList.forEach(Reservation::updateDateType);

        Map< Integer, Integer > mapCount = new HashMap<>();
        reserveList.forEach( item->{
            mapCount.put(Math.toIntExact(item.getDateType()), mapCount.getOrDefault( Math.toIntExact(item.getDateType()), 0) + 1);
        });

        System.out.println( mapCount);
        model.addAttribute("mapCount", mapCount);
        model.addAttribute("reserveList", reserveList);

        model.addAttribute("pageName", "예약 목록");
        return "/owner/todayList";
    }
    @GetMapping("futureList")
    public String futureList(Principal principal, Model model) {
        User loginUser = userService.findByUserId( principal.getName() ).get();
        model.addAttribute("user", loginUser);

        Dinning dinning = diningRestService.getByUserNo(loginUser);
        if(dinning == null) {
            model.addAttribute("msg", "등록된 가게가 없습니다. 이 기능은 가게 등록 후 사용 가능한 메뉴입니다.");
            model.addAttribute("location", "/owner/addRest");
            return "owner/transit";
        }
        model.addAttribute("dinning", dinning);
        List<Reservation> reserveList = status(reservationRepository.getReservationWithDateType((long) dinning.getRestNo()));
        reserveList.forEach(Reservation::updateDateType);

        Map< Integer, Integer > mapCount = new HashMap<>();
        reserveList.forEach( item->{
            mapCount.put(Math.toIntExact(item.getDateType()), mapCount.getOrDefault( Math.toIntExact(item.getDateType()), 0) + 1);
        });

        model.addAttribute("mapCount", mapCount);
        model.addAttribute("reserveList", reserveList);

        model.addAttribute("pageName", "예약 목록");
        return "/owner/futureList";
    }
    @GetMapping("pastList")
    public String pastList(Principal principal, Model model) {
        User loginUser = userService.findByUserId( principal.getName() ).get();
        model.addAttribute("user", loginUser);

        Dinning dinning = diningRestService.getByUserNo(loginUser);
        if(dinning == null) {
            model.addAttribute("msg", "등록된 가게가 없습니다. 이 기능은 가게 등록 후 사용 가능한 메뉴입니다.");
            model.addAttribute("location", "/owner/addRest");
            return "owner/transit";
        }
        model.addAttribute("dinning", dinning);
        List<Reservation> reserveList = status(reservationRepository.getReservationWithDateType((long) dinning.getRestNo()));
        reserveList.forEach(Reservation::updateDateType);

        Map< Integer, Integer > mapCount = new HashMap<>();
        reserveList.forEach( item->{
            mapCount.put(Math.toIntExact(item.getDateType()), mapCount.getOrDefault( Math.toIntExact(item.getDateType()), 0) + 1);
        });

        model.addAttribute("mapCount", mapCount);
        model.addAttribute("reserveList", reserveList);

        model.addAttribute("pageName", "예약 목록");
        return "/owner/pastList";
    }

    @GetMapping("resExpired/{resNo}")
    public String resExpired(@PathVariable("resNo") int resNo, Principal principal, @RequestParam(value = "url", required = false)  String url) {
        User loginUser = userService.findByUserId(principal.getName()).get();
        Reservation reservation = reservationRepository.findById(resNo).get();
        reservation.setResStatus(String.valueOf(ReservationStatus.EXPIRED));
        reservation.setRes_rejection_reason(null);
        reservationRepository.save(reservation);
        return "redirect:"+url;
    }

    @GetMapping("resNoShow/{resNo}")
    public String resNoShow(@PathVariable("resNo") int resNo, Principal principal, @RequestParam(value = "url", required = false) String url) {
        User loginUser = userService.findByUserId(principal.getName()).get();
        Reservation reservation = reservationRepository.findById(resNo).get();
        reservation.setResStatus(String.valueOf(ReservationStatus.NO_SHOW));
        reservation.setRes_rejection_reason(null);
        reservationRepository.save(reservation);
        return "redirect:"+url;

    }

    @GetMapping("resCompleted/{resNo}")
    public String resCompleted(@PathVariable("resNo") int resNo, Principal principal, HttpServletRequest request , @RequestParam(value = "url", required = false)  String url) {
        User loginUser = userService.findByUserId(principal.getName()).get();
        Reservation reservation = reservationRepository.findById(resNo).get();
        reservation.setResStatus(String.valueOf(ReservationStatus.RESERVE_COMPLETED));
        reservation.setRes_rejection_reason(null);
        reservationRepository.save(reservation);

        sendMessage.Send(request, reservation.getUserNo().getUserId()
                , new ChatDTO((long) reservation.getRestNo().getRestNo(), null, null
                        , ReservationStatus.RESERVE_COMPLETED.getName(), reservation.getRestNo().getRestImg()
                        , reservation.getRestNo().getRestName(), LocalDateTime.now() ));

        return "redirect:"+url;
    }

    @GetMapping("resCancel/{resNo}/{reason}")
    public String resCancel(@PathVariable("resNo") int resNo, @PathVariable("reason") String reason, Principal principal,
                            HttpServletRequest request , @RequestParam(value = "url", required = false)  String url) {
        User loginUser = userService.findByUserId(principal.getName()).get();
        Reservation reservation = reservationRepository.findById(resNo).get();
        reservation.setResStatus(String.valueOf(ReservationStatus.REST_CANCEL));
        reservation.setRes_rejection_reason(reason);
        reservationRepository.save(reservation);

        sendMessage.Send(request, reservation.getUserNo().getUserId()
                , new ChatDTO( (long) reservation.getRestNo().getRestNo(), null, null
                        , reason, reservation.getRestNo().getRestImg()
                        , reservation.getRestNo().getRestName(), LocalDateTime.now() ));

        return "redirect:"+url;
    }

    // --------------------- 개인 정보 관리 -----------------------
    @GetMapping("userInfo")
    public String userInfo(Principal principal, Model model) {
        User loginUser = userService.findByUserId( principal.getName() ).get();

        model.addAttribute("user", loginUser);

        model.addAttribute("pageName", "개인 정보 확인");
        return "/owner/userInfo";
    }
    @GetMapping("userUpdate")
    public String userUpdate(Principal principal, Model model) {
        User loginUser = userService.findByUserId( principal.getName() ).get();

        model.addAttribute("user", loginUser);

        model.addAttribute("pageName", "개인 정보 수정");
        return "/owner/userUpdate";
    }
    @PostMapping("userUpdate")
    public String userUpdate(User user, @RequestParam MultipartFile file, @RequestParam String oldPw, Model model) {
        User existUser = userService.findByUserNo(user.getUserNo()).get();
        boolean changPw = false;

        if((!user.getUserPassword().isEmpty())) {
            if(passwordEncoder.matches(oldPw, existUser.getUserPassword())) {
                user.setUserPassword(passwordEncoder.encode(user.getUserPassword()));
                changPw = true;
            } else {
                model.addAttribute("msg", "현재 비밀번호가 입력한 비밀번호와 일치하지 않아 개인정보 수정에 실패하였습니다.\n" +
                        "다시 시도 부탁드립니다.\n" +
                        "비밀번호 외 다른 정보도 수정되지 않고 초기화 됩니다.");
                model.addAttribute("location", "/owner/userUpdate");
                return "owner/transit";
            }
        } else {
            user.setUserPassword(existUser.getUserPassword());
        }

        byte[] userImg;
        if(!file.isEmpty()) {
            try {
                userImg = file.getBytes();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            userImg = existUser.getUserImg();
        }
        user.setUserImg(userImg);
        User updateUser = userService.updateUser(user);
        if(changPw) {
            return "redirect:/logout";
        }
        return "redirect:/owner/userInfo";
    }

    @GetMapping("userDelete")
    public String userDeleteForm(Principal principal, Model model) {
        User loginUser = userService.findByUserId( principal.getName() ).get();
        model.addAttribute("user", loginUser);
        Dinning dinning = diningRestService.getByUserNo(loginUser);
        model.addAttribute("dinning", dinning);
        model.addAttribute("pageName", "회원 탈퇴 신청");
        return "owner/userDelete";
    }
    @PostMapping("userDelete")
    @Transactional
    public String userDelete(Principal principal, Model model) {
        System.out.println("회원 탈퇴");
        User loginUser = userService.findByUserId(principal.getName()).get();
        model.addAttribute("user", loginUser);
        Dinning dinning = diningRestService.getByUserNo(loginUser);
        model.addAttribute("dinning", dinning);
        if(dinning != null) {
            model.addAttribute("msg", "가게 폐점 신청이 완료되지 않아 회원 탈퇴가 불가능합니다.");
            model.addAttribute("location", "/owner/deleteRest");
            return "owner/transit";
        }

        DeleteUser deleteUser = new DeleteUser(loginUser.getUserNo(), loginUser.getUserId(), loginUser.getUserAuth(), loginUser.getUserStartDate(), loginUser.isUserBlock());
        deleteUserRepository.save(deleteUser);

        userService.deleteByUserNo(loginUser.getUserNo());
        return "redirect:/logout";
    }

    // ---------------------------- 이벤트 관련 --------------------------
    @GetMapping("eventList")
    public String event(Principal principal, Model model) {
        User loginUser = userService.findByUserId( principal.getName() ).get();
        model.addAttribute("user", loginUser);
        Dinning dinning = diningRestService.getByUserNo(loginUser);
        if(dinning == null) {
            model.addAttribute("msg", "등록된 가게가 없습니다. 이 기능은 가게 등록 후 사용 가능한 메뉴입니다.");
            model.addAttribute("location", "/owner/addRest");
            return "owner/transit";
        }
        model.addAttribute("dinning", dinning);

        List<Event> eventList = eventService.findByRestNo(dinning);
        model.addAttribute("eventList", eventList);

        model.addAttribute("pageName", "이벤트 목록");
        return "event/eventList";

    }

    @GetMapping("addEvent")
    public String addEvent(Principal principal, Model model) {
        User loginUser = userService.findByUserId( principal.getName() ).get();
        model.addAttribute("user", loginUser);
        Dinning dinning = diningRestService.getByUserNo(loginUser);
        if(dinning == null) {
            model.addAttribute("msg", "등록된 가게가 없습니다. 이 기능은 가게 등록 후 사용 가능한 메뉴입니다.");
            model.addAttribute("location", "/owner/addRest");
            return "owner/transit";
        }
        model.addAttribute("dinning", dinning);

        model.addAttribute("pageName", "이벤트 추가");

        return "event/addEvent";
    }

    @PostMapping("addEvent")
    public String addEvent(
            Principal principal, Event event, Model model,
            @RequestParam MultipartFile file, @RequestParam String strStartTime, @RequestParam String strEndTime
    ) {
        User loginUser = userService.findByUserId( principal.getName() ).get();
        Dinning dinning = diningRestService.getByUserNo(loginUser);

        byte[] eventImg;
        try {
            eventImg = file.getBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        event.setEventImg(eventImg);

        event.setEventStartTime(LocalDate.parse(strStartTime));
        event.setEventEndTime(LocalDate.parse(strEndTime));
        event.setRestNo(dinning);
        eventService.createEvent(event);
        return "redirect:/owner/eventList";
    }

    @GetMapping("viewEvent/{eventNo}")
    public String viewEvent(Principal principal, @PathVariable("eventNo") int eventNo, Model model) {
        User loginUser = userService.findByUserId( principal.getName() ).get();
        model.addAttribute("user", loginUser);

        Dinning dinning = diningRestService.getByUserNo(loginUser);
        model.addAttribute("dinning", dinning);

        Event event = eventService.findByEventNo(eventNo);
        model.addAttribute("event", event);
        model.addAttribute("pageName", "이벤트 상세보기");
        return "event/viewEvent";
    }

    @GetMapping("updateEvent/{eventNo}")
    public String updateEvent(Principal principal, @PathVariable("eventNo") int eventNo, Model model) {
        User loginUser = userService.findByUserId( principal.getName() ).get();
        model.addAttribute("user", loginUser);

        Dinning dinning = diningRestService.getByUserNo(loginUser);
        model.addAttribute("dinning", dinning);

        Event event = eventService.findByEventNo(eventNo);
        model.addAttribute("event", event);
        model.addAttribute("pageName", "이벤트 수정");
        return "event/updateEvent";
    }

    @PostMapping("updateEvent/{eventNo}")
    public String updateEvent(
            Principal principal, @PathVariable("eventNo") int eventNo, Event event,
            @RequestParam MultipartFile file, @RequestParam String strStartTime, @RequestParam String strEndTime
    ) {
        User loginUser = userService.findByUserId( principal.getName() ).get();
        Dinning dinning = diningRestService.getByUserNo(loginUser);
        Event exists = eventService.findByEventNo(eventNo);
        byte[] eventImg;
        if(!file.isEmpty()) {
            try {
                eventImg = file.getBytes();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            eventImg = exists.getEventImg();
        }
        event.setEventImg(eventImg);
        event.setEventStartTime(LocalDate.parse(strStartTime));
        event.setEventEndTime(LocalDate.parse(strEndTime));
        event.setRestNo(dinning);
        eventService.updateEvent(event);
        return "redirect:/owner/viewEvent/" + eventNo;
    }

    @GetMapping("deleteEvent/{eventNo}") // 삭제하면 외래키 에러 발생
    public String deleteEvent(@PathVariable("eventNo") int eventNo) {
        Event event = eventService.findByEventNo(eventNo);
        eventService.deleteEvent(event);
        return "redirect:/owner/eventList";
    }

    // ------------------------ 리뷰 관련 ---------------------------
    @GetMapping("reviewList")
    public String reviewList(Principal principal, Model model) {
        User loginUser = userService.findByUserId(principal.getName()).get();
        model.addAttribute("user", loginUser);

        Dinning dinning = diningRestService.getByUserNo(loginUser);
        if(dinning == null) {
            model.addAttribute("msg", "등록된 가게가 없습니다. 이 기능은 가게 등록 후 사용 가능한 메뉴입니다.");
            model.addAttribute("location", "/owner/addRest");
            return "owner/transit";
        }

        List<Review> reviewList = reviewRepository.findByRestNo(dinning);
        model.addAttribute("reviewList", reviewList);

        model.addAttribute("pageName", "식당 리뷰 관리");
        return "owner/reviewList";
    }

    @PostMapping("review/report/{revNo}")
    public String review_report (@PathVariable("revNo") int revNo, @RequestParam String revStatus) {
        Review review = reviewRepository.findById(revNo).get();
        review.setRevStatus(revStatus);

        reviewRepository.save(review);

        return "redirect:/owner/reviewList";
    }
}