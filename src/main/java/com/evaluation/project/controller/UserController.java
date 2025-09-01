package com.evaluation.project.controller;

import com.evaluation.project.model.dto.UserRequest;
import com.evaluation.project.model.dto.UserResponse;
import com.evaluation.project.service.impl.UserServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

  private final UserServiceImpl userService;
  
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public Flux<UserResponse> getAllUsers() {
    return userService.getUsers();
  }

  @GetMapping(value = "/id/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<UserResponse> getUserById (@PathVariable("uuid") UUID uuid) {
    return userService.getUser(uuid);
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<UserResponse> createUser(@Valid @RequestBody UserRequest userRequest) {
    return userService.createUser(userRequest);
  }


}
