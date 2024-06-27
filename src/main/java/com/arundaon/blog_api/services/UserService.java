package com.arundaon.blog_api.services;

import com.arundaon.blog_api.entities.User;
import com.arundaon.blog_api.models.LoginRequest;
import com.arundaon.blog_api.models.RegisterRequest;
import com.arundaon.blog_api.models.TokenResponse;
import com.arundaon.blog_api.repositories.UserRepository;
import com.arundaon.blog_api.security.BCrypt;
import com.arundaon.blog_api.security.JWTProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {
    private final ValidationService validationService;
    private UserRepository userRepository;

    private  JWTProvider jwtProvider;

    public UserService(UserRepository userRepository, JWTProvider jwtProvider, ValidationService validationService) {
        this.userRepository = userRepository;
        this.jwtProvider = jwtProvider;
        this.validationService = validationService;
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        validationService.validate(request);

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "email or password is incorrect"));

        if(!BCrypt.checkpw(request.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "email or password is incorrect");
        }

        return TokenResponse.builder().token(jwtProvider.generateJwt(user.getId())).build();
    }

    @Transactional
    public void register(RegisterRequest request) {
        validationService.validate(request);

        if (userRepository.existsByEmail(request.getEmail())){
            throw new ResponseStatusException(HttpStatus.CONFLICT,"user already registered");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setName(request.getName());
        user.setPassword(BCrypt.hashpw(request.getPassword(),BCrypt.gensalt()));
        userRepository.save(user);
    }



}
