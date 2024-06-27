package com.arundaon.blog_api.controller;

import com.arundaon.blog_api.entities.User;
import com.arundaon.blog_api.models.LoginRequest;
import com.arundaon.blog_api.models.RegisterRequest;
import com.arundaon.blog_api.models.TokenResponse;
import com.arundaon.blog_api.models.WebResponse;
import com.arundaon.blog_api.repositories.PostRepository;
import com.arundaon.blog_api.repositories.UserRepository;
import com.arundaon.blog_api.security.BCrypt;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {
    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @BeforeEach
    void setUp() {
        postRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void registerSuccess() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                        .email("test@example.com")
                        .name("test name")
                        .password("password").build();

        mockMvc.perform(
                        post("/api/users/register")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpectAll(status().isOk())
                .andDo(result->{
                    WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });
                    assertEquals("OK",response.getData());
                });
    }

    @Test
    void registerInvalidInput() throws Exception{
        RegisterRequest request = RegisterRequest.builder()
                .email("test@example.com")
                .name("test name")
                .password("pass").build(); // password min 8 chars

        mockMvc.perform(
                        post("/api/users/register")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpectAll(status().isBadRequest())
                .andDo(result->{
                    WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });
                    assertNotNull(response.getErrors());
                });
    }

    @Test
    void userAlreadyRegistered() throws Exception{
        User user = new User(9999, BCrypt.hashpw("password",BCrypt.gensalt()),"test name", "test@example.com", null);
        userRepository.save(user);
        RegisterRequest request = RegisterRequest.builder()
                .email("test@example.com")
                .name("test name")
                .password("password").build();

        mockMvc.perform(
                        post("/api/users/register")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpectAll(status().isConflict())
                .andDo(result->{
                    WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });
                    assertNotNull(response.getErrors());
                });
    }

    @Test
    void loginSuccessful() throws Exception{
        User user = new User(9999, BCrypt.hashpw("password",BCrypt.gensalt()),"test name", "test@example.com", null);
        userRepository.save(user);
        LoginRequest request = LoginRequest.builder()
                .email("test@example.com")
                .password("password").build();

        mockMvc.perform(
                        post("/api/users/login")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpectAll(status().isOk())
                .andDo(result->{
                    WebResponse<TokenResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });
                    assertNotNull(response.getData().getToken());
                });
    }

    @Test
    void loginInvalidInput() throws Exception{
        User user = new User(9999, BCrypt.hashpw("password",BCrypt.gensalt()),"test name", "test@example.com", null);
        userRepository.save(user);

        LoginRequest request = LoginRequest.builder()
                .email("test_example.com") // invalid email
                .password("pass").build();

        mockMvc.perform(
                        post("/api/users/login")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpectAll(status().isBadRequest())
                .andDo(result->{
                    WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });
                    assertNotNull(response.getErrors());
                });
    }

    @Test
    void loginNonexistentEmail() throws Exception{
        LoginRequest request = LoginRequest.builder()
                .email("testnonexistent@example.com")
                .password("password").build();

        mockMvc.perform(
                        post("/api/users/login")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpectAll(status().isUnauthorized())
                .andDo(result->{
                    WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });
                    assertNotNull(response.getErrors());
                });
    }

    @Test
    void loginWrongPassword() throws Exception{
        User user = new User(9999, BCrypt.hashpw("password",BCrypt.gensalt()),"test name", "test@example.com", null);
        userRepository.save(user);

        LoginRequest request = LoginRequest.builder()
                .email("test@example.com")
                .password("wrongpassword").build();

        mockMvc.perform(
                        post("/api/users/login")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpectAll(status().isUnauthorized())
                .andDo(result->{
                    WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });
                    assertNotNull(response.getErrors());
                });
    }

}