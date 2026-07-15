package com.library.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/")
    public String home(){
        return "Library Management System is Running";
    }

    @GetMapping("/hello")
    public String hello(){
        return "Hello, spring is working";
    }
}
