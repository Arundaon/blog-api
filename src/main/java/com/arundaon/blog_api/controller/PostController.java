package com.arundaon.blog_api.controller;

import com.arundaon.blog_api.entities.User;
import com.arundaon.blog_api.models.PostRequest;
import com.arundaon.blog_api.models.PostInfo;
import com.arundaon.blog_api.models.WebResponse;
import com.arundaon.blog_api.services.PostService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    PostService postService;
    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public WebResponse<List<PostInfo>> getPosts() {
        return WebResponse.<List<PostInfo>>builder().data(postService.getAllPost()).build();
    }

    @GetMapping(path="/{postId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public WebResponse<PostInfo> getPost(@PathVariable int postId) {
        return WebResponse.<PostInfo>builder().data(postService.getPost(postId)).build();
    }


    //  which must be authenticated user and user id must match post authorId

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public WebResponse<String> createPost(User user, @RequestBody PostRequest request) {
        postService.create(user, request);
        return WebResponse.<String>builder().data("OK").build();
    }

    @PutMapping(path ="/{postId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public WebResponse<String> updatePost(User user, @PathVariable int postId, @RequestBody PostRequest request) {
        postService.update(user, postId, request);
        return WebResponse.<String>builder().data("OK").build();
    }

    @DeleteMapping(path="/{postId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public WebResponse<String> deletePost(User user, @PathVariable int postId) {
        postService.remove(user,postId);
        return WebResponse.<String>builder().data("OK").build();
    }







}
