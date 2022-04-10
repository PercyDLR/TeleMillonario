package com.example.telemillonario.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/login")
public class LoginController {

    @GetMapping ("/signin")
    public String ingreso () {
        return "login/signin";
    }

    @GetMapping("signup")
    public String registro(){
        return "login/signup";
    }
}
