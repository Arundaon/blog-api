package com.arundaon.blog_api.resolvers;

import com.arundaon.blog_api.entities.User;
import com.arundaon.blog_api.repositories.UserRepository;
import com.arundaon.blog_api.security.JWTProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.server.ResponseStatusException;

@Component
public class UserArgumentResolver implements HandlerMethodArgumentResolver {
    private UserRepository userRepository;
    private JWTProvider jwtProvider;

    public UserArgumentResolver(UserRepository userRepository, JWTProvider jwtProvider) {
        this.userRepository = userRepository;
        this.jwtProvider = jwtProvider;
    }


    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return User.class.equals(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest servletRequest =(HttpServletRequest)webRequest.getNativeRequest();
        String token = servletRequest.getHeader("Authorization");
        if(token == null){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"unauthorized");
        }

        System.out.println(token);

        User user = userRepository.findById(jwtProvider.getUserId(token))
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.UNAUTHORIZED,"unauthorized"));

//        if(user.getExpiredAt() < System.currentTimeMillis()){
//            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Unauthorized");
//        }
        return user;
    }
}
