package com.yi.spring.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/policy/*")
public class PolicyController {
    @GetMapping("/privacy")
    public String privacy() {

        return "policy/privacy";
    }

    @GetMapping("/service")
    public String service() {

        return "policy/service";
    }

    @GetMapping("/location")
    public String location() {

        return "policy/location";
    }

}
