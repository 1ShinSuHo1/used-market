package com.wonsu.used_market.user.controller;


import com.wonsu.used_market.user.service.UserService;
import org.springframework.web.bind.annotation.RestController;

@RestController

public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }


}
