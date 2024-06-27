package com.arundaon.blog_api.controller;

import com.arundaon.blog_api.models.LoginRequest;
import com.arundaon.blog_api.models.RegisterRequest;
import com.arundaon.blog_api.models.TokenResponse;
import com.arundaon.blog_api.models.WebResponse;
import com.arundaon.blog_api.services.UserService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(path="/login",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public WebResponse<TokenResponse> login(@RequestBody LoginRequest request) {
        TokenResponse response = userService.login(request);
        return WebResponse.<TokenResponse>builder().data(response).build();
    }

    @PostMapping(path="/register",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public WebResponse<String> register(@RequestBody RegisterRequest request) {
        userService.register(request);
        return WebResponse.<String>builder().data("OK").build();
    }

}
