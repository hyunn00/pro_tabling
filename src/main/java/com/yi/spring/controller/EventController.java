package com.yi.spring.controller;

import com.yi.spring.entity.Event;
import com.yi.spring.repository.EventRepository;
import com.yi.spring.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/event/*")
@RequiredArgsConstructor
public class EventController {
    private final EventRepository eventRepository;

    @GetMapping("home")
    public String event(Model model) {
        List<Event> newEvents = eventRepository.getNewEvents(); // 진행 중인 이벤트 목록
        List<Event> pastEvents = eventRepository.getPastEvents(); // 지난 이벤트 목록
        List<Event> futureEvents = eventRepository.getFutureEvents(); // 다가올 이벤트 목록
        model.addAttribute("newEvents", newEvents);
        model.addAttribute("pastEvents", pastEvents);
        model.addAttribute("futureEvents", futureEvents);
        return "event/home";
    }

    @GetMapping("detail/{eventNo}")
    public String detail(Model model, @PathVariable("eventNo") int eventNo) {
        Event event = eventRepository.findById(eventNo).get();
        model.addAttribute("event", event);
        return "event/eventDetail";
    }
}
