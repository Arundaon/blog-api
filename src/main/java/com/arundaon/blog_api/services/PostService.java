package com.arundaon.blog_api.services;

import com.arundaon.blog_api.entities.Post;
import com.arundaon.blog_api.entities.User;
import com.arundaon.blog_api.models.PostInfo;
import com.arundaon.blog_api.models.PostRequest;
import com.arundaon.blog_api.repositories.PostRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PostService {
    private PostRepository postRepository;
    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }
    @Transactional(readOnly = true)
    public PostInfo getPost(Integer id){
        Post post = postRepository.findById(id).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,"post not found"));
        return PostInfo.builder()
                .id(post.getId())
                .content(post.getContent())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .author(post.getAuthor().getName())
                .build();

    }

    @Transactional(readOnly = true)
    public List<PostInfo> getAllPost(){
        return postRepository.findAll().stream().map(post-> {
            return PostInfo.builder()
                .id(post.getId())
                .content(post.getContent())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .author(post.getAuthor().getName())
                .build();}
            ).toList();
    }

    @Transactional
    public void create(User user, PostRequest request){
        Post post = new Post();
        post.setContent(request.getContent());
        post.setAuthor(user);
        postRepository.save(post);
    }

    @Transactional
    public void update(User user, Integer id, PostRequest request){
        Post post = postRepository.findById(id).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,"post not found"));

        if(!user.getId().equals(post.getAuthor().getId())){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"unauthorized");
        }

        post.setContent(request.getContent());
        post.setUpdatedAt(LocalDateTime.now());

        postRepository.save(post);
    }

    @Transactional
    public void remove(User user, Integer id){
        Post post = postRepository.findById(id).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,"post not found"));

        if(!user.getId().equals(post.getAuthor().getId())){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"unauthorized");
        }

        postRepository.deleteById(post.getId());
    }





}
