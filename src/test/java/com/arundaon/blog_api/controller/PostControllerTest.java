package com.arundaon.blog_api.controller;

import com.arundaon.blog_api.entities.Post;
import com.arundaon.blog_api.entities.User;
import com.arundaon.blog_api.models.*;
import com.arundaon.blog_api.repositories.PostRepository;
import com.arundaon.blog_api.repositories.UserRepository;
import com.arundaon.blog_api.security.BCrypt;
import com.arundaon.blog_api.security.JWTProvider;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PostControllerTest {

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
    void getAllPosts() throws Exception {
        createTestPosts();
        mockMvc.perform(
                        get("/api/posts")
                                .accept(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk())
                .andDo(result->{
                    WebResponse<List<PostInfo>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });
                    assertEquals(6, response.getData().size());
                });
    }

    @Test
    void getSpecifiedPost() throws Exception {
        createTestPosts();
        User user1 = userRepository.findByEmail("test1@example.com").orElse(null);
        Post post1 = postRepository.findByContent("testpost1").orElse(null);
        mockMvc.perform(
                        get("/api/posts/"+ post1.getId())
                                .accept(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk())
                .andDo(result->{
                    WebResponse<PostInfo> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });
                    assertEquals(post1.getId(), response.getData().getId());
                    assertEquals(post1.getAuthor().getName(), response.getData().getAuthor());
                    assertEquals(post1.getContent(), response.getData().getContent());
                });
    }

    @Test
    void getNonexistentPost() throws Exception {
        createTestPosts();
        User user1 = userRepository.findByEmail("test1@example.com").orElse(null);
        Post post1 = postRepository.findByContent("testpost1").orElse(null);
        mockMvc.perform(
                        get("/api/posts/"+ post1.getId() + 10)
                                .accept(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isNotFound())
                .andDo(result->{
                    WebResponse<PostInfo> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });
                    assertNotNull(response.getErrors());
                });
    }

    @Test
    void createPost() throws Exception {
        createTestPosts();
        String token = obtainAccessToken("test1@example.com","password");
        PostRequest request = PostRequest.builder().content("testpost").build();
        System.out.println("TOKEN:"+token);

        mockMvc.perform(
                        post("/api/posts")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", token)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpectAll(status().isOk())
                .andDo(result->{
                    WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });
                    assertEquals("OK",response.getData());
                    assertNotNull(postRepository.findByContent("testpost").orElse(null));

                });
    }

    @Test
    void createInvalidPost() throws Exception{
        createTestPosts();
        String token = obtainAccessToken("test1@example.com","password");
        PostRequest request = PostRequest.builder().build(); // post content is empty

        mockMvc.perform(
                        post("/api/posts")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", token)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpectAll(status().isBadRequest())
                .andDo(result->{
                    WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });
                    assertNotNull(response.getErrors());

                });
    }

    @Test
    void createPostNoToken() throws Exception{
        createTestPosts();
        String token = obtainAccessToken("test1@example.com","password");
        PostRequest request = PostRequest.builder().content("newpost").build();

        mockMvc.perform(
                        post("/api/posts") // no token
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpectAll(status().isUnauthorized())
                .andDo(result->{
                    WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });
                    assertNotNull(response.getErrors());

                });
    }

    @Test
    void updatePost() throws Exception {
        createTestPosts();
        String token = obtainAccessToken("test1@example.com","password");
        PostRequest request = PostRequest.builder().content("updatedpost").build();
        Post post = postRepository.findByContent("testpost1").orElse(null);

        mockMvc.perform(
                        put("/api/posts/"+post.getId())
                                .accept(MediaType.APPLICATION_JSON)
                                .header("Authorization", token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpectAll(status().isOk())
                .andDo(result->{
                    WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });
                    assertEquals("OK",response.getData());

                    Post updatedPost = postRepository.findById(post.getId()).orElse(null);

                    assertEquals("updatedpost", updatedPost.getContent());
                    assertEquals(post.getCreatedAt(),updatedPost.getCreatedAt());
                    assertNotEquals(post.getUpdatedAt().toString(), updatedPost.getUpdatedAt().toString());
                    System.out.println("post: "+post.getUpdatedAt());
                    System.out.println("updated:" +updatedPost.getUpdatedAt());


                });
    }

    @Test
    void updateInvalidPost() throws Exception {
        createTestPosts();
        String token = obtainAccessToken("test1@example.com","password");
        PostRequest request = PostRequest.builder().content("updatedpost").build();
        Post post = postRepository.findByContent("testpost1").orElse(null);

        mockMvc.perform(
                        put("/api/posts/invalid")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", token)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpectAll(status().isBadRequest())
                .andDo(result->{
                    WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });
                    assertNotNull(response.getErrors());
                    Post updatedPost = postRepository.findById(post.getId()).orElse(null);

                    assertEquals(post.getContent(), updatedPost.getContent());
                    assertEquals(post.getCreatedAt(),updatedPost.getCreatedAt());
                    assertEquals(post.getUpdatedAt(), updatedPost.getUpdatedAt());
                });
    }

    @Test
    void updateNonexistentPost() throws Exception {
        createTestPosts();
        String token = obtainAccessToken("test1@example.com","password");
        PostRequest request = PostRequest.builder().content("updatedpost").build();
        Post post = postRepository.findByContent("testpost1").orElse(null);

        mockMvc.perform(
                        put("/api/posts/"+post.getId()+10)
                                .accept(MediaType.APPLICATION_JSON)
                                .header("Authorization", token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpectAll(status().isNotFound())
                .andDo(result->{
                    WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });
                    assertNotNull(response.getErrors());
                    Post updatedPost = postRepository.findById(post.getId()).orElse(null);

                    assertEquals(post.getContent(), updatedPost.getContent());
                    assertEquals(post.getCreatedAt(),updatedPost.getCreatedAt());
                    assertEquals(post.getUpdatedAt(), updatedPost.getUpdatedAt());
                });
    }

    @Test
    void updatePostNoToken() throws Exception {
        createTestPosts();
        String token = obtainAccessToken("test1@example.com","password");
        PostRequest request = PostRequest.builder().content("updatedpost").build();
        Post post = postRepository.findByContent("testpost1").orElse(null);

        mockMvc.perform(
                        put("/api/posts/"+post.getId())
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpectAll(status().isUnauthorized())
                .andDo(result->{
                    WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });
                    assertNotNull(response.getErrors());
                    Post updatedPost = postRepository.findById(post.getId()).orElse(null);

                    assertEquals(post.getContent(), updatedPost.getContent());
                    assertEquals(post.getCreatedAt(),updatedPost.getCreatedAt());
                    assertEquals(post.getUpdatedAt(), updatedPost.getUpdatedAt());

                });

    }
    @Test
    void updatePostNotOwner() throws Exception {
        createTestPosts();
        String token = obtainAccessToken("test2@example.com","password"); // user 2
        PostRequest request = PostRequest.builder().content("updatedpost").build();
        Post post = postRepository.findByContent("testpost1").orElse(null);

        mockMvc.perform(
                        put("/api/posts/"+post.getId())
                                .accept(MediaType.APPLICATION_JSON)
                                .header("Authorization", token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpectAll(status().isUnauthorized())
                .andDo(result->{
                    WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });
                    assertNotNull(response.getErrors());
                    Post updatedPost = postRepository.findById(post.getId()).orElse(null);

                    assertEquals(post.getContent(), updatedPost.getContent());
                    assertEquals(post.getCreatedAt(),updatedPost.getCreatedAt());
                    assertEquals(post.getUpdatedAt(), updatedPost.getUpdatedAt());

                });

    }

    @Test
    void deletePost() throws Exception {
        createTestPosts();
        String token = obtainAccessToken("test1@example.com","password");
        PostRequest request = PostRequest.builder().content("updatedpost").build();
        Post post = postRepository.findByContent("testpost1").orElse(null);

        mockMvc.perform(
                        delete("/api/posts/"+post.getId())
                                .accept(MediaType.APPLICATION_JSON)
                                .header("Authorization", token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpectAll(status().isOk())
                .andDo(result->{
                    WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });
                    assertEquals("OK",response.getData());

                    Post deletedPost = postRepository.findById(post.getId()).orElse(null);
                    assertNull(deletedPost);

                });
    }

    @Test
    void deleteNonexistentPost() throws Exception {
        createTestPosts();
        String token = obtainAccessToken("test1@example.com","password");
        PostRequest request = PostRequest.builder().content("updatedpost").build();
        Post post = postRepository.findByContent("testpost1").orElse(null);

        mockMvc.perform(
                        delete("/api/posts/"+post.getId()+10)
                                .accept(MediaType.APPLICATION_JSON)
                                .header("Authorization", token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpectAll(status().isNotFound())
                .andDo(result->{
                    WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });
                    assertNotNull(response.getErrors());
                });
    }

    @Test
    void deletePostNoToken() throws Exception {
        createTestPosts();
        String token = obtainAccessToken("test1@example.com","password");
        PostRequest request = PostRequest.builder().content("updatedpost").build();
        Post post = postRepository.findByContent("testpost1").orElse(null);

        mockMvc.perform(
                        delete("/api/posts/"+post.getId())
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpectAll(status().isUnauthorized())
                .andDo(result->{
                    WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });
                    assertNotNull(response.getErrors());

                    Post notDeletedPost = postRepository.findById(post.getId()).orElse(null);
                    assertNotNull(notDeletedPost);

                });
    }

    @Test
    void deletePostNotOwner() throws Exception {
        createTestPosts();
        String token = obtainAccessToken("test2@example.com","password"); // user 2 token
        PostRequest request = PostRequest.builder().content("updatedpost").build();
        Post post = postRepository.findByContent("testpost1").orElse(null);

        mockMvc.perform(
                        delete("/api/posts/"+post.getId())
                                .accept(MediaType.APPLICATION_JSON)
                                .header("Authorization", token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpectAll(status().isUnauthorized())
                .andDo(result->{
                    WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });
                    assertNotNull(response.getErrors());

                    Post notDeletedPost = postRepository.findById(post.getId()).orElse(null);
                    assertNotNull(notDeletedPost);

                });
    }

    private void createTestPosts(){

        User user1 = new User();
        user1.setPassword(BCrypt.hashpw("password",BCrypt.gensalt()));
        user1.setName("test name 1");
        user1.setEmail("test1@example.com");
        userRepository.save(user1);

        User user2 = new User();
        user2.setPassword(BCrypt.hashpw("password",BCrypt.gensalt()));
        user2.setName("test name 2");
        user2.setEmail("test2@example.com");
        userRepository.save(user2);


        user1 = userRepository.findByEmail("test1@example.com").orElse(null);
        user2 = userRepository.findByEmail("test2@example.com").orElse(null);

        for(int i = 1; i <= 6; i++){
            Post post = new Post();
            post.setContent("testpost"+i);

            if(i % 2 == 1){
                post.setAuthor(user1);
            }
            else{
                post.setAuthor(user2);
            }

            postRepository.save(post);
        }

    }


    private String obtainAccessToken(String email, String password) throws Exception {
        LoginRequest loginRequest = new LoginRequest(email, password);

        String result = mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn().getResponse().getContentAsString();

        WebResponse<TokenResponse> response = objectMapper.readValue(result, new TypeReference<>() {
        });

        return response.getData().getToken();
    }
}