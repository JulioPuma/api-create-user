package com.evaluation.project.controller;

import com.evaluation.project.model.dto.Phone;
import com.evaluation.project.model.dto.UserRequest;
import com.evaluation.project.model.dto.UserResponse;
import com.evaluation.project.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@WebFluxTest(UserController.class)
class UserControllerTest {
  
  @Autowired
  private WebTestClient webTestClient;
  
  @MockBean
  private UserServiceImpl userService;
  
  private UserRequest buildUserRequest() {
    Phone phone = Phone.builder()
      .number("123456789")
      .cityCode("01")
      .countryCode("57")
      .build();
    return UserRequest.builder()
      .name("Juan Pérez")
      .email("juan.perez@example.com")
      .password("pass123")
      .phones(List.of(phone))
      .build();
  }
  
  private UserResponse buildUserResponse(UUID uuid, UserRequest userRequest) {
    LocalDateTime now = LocalDateTime.now().withNano(0); // para evitar diferencias de milisegundos
    return UserResponse.builder()
      .uuid(uuid.toString())
      .userInformation(userRequest)
      .created(now)
      .modified(now)
      .lastLogin(now)
      .token("token1")
      .isActive(true)
      .build();
  }
  
  @Test
  void getAllUsers_returnsList() {
    UserRequest userRequest1 = buildUserRequest();
    UserResponse user1 = buildUserResponse(UUID.randomUUID(), userRequest1);
    
    UserRequest userRequest2 = UserRequest.builder()
      .name("Ana Gómez")
      .email("ana.gomez@example.com")
      .password("pass456")
      .phones(Collections.emptyList())
      .build();
    UserResponse user2 = buildUserResponse(UUID.randomUUID(), userRequest2);
    
    Mockito.when(userService.getUsers()).thenReturn(Flux.just(user1, user2));
    
    webTestClient.get()
      .uri("/users")
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk()
      .expectBodyList(UserResponse.class)
      .consumeWith(response -> {
        List<UserResponse> users = response.getResponseBody();
        assertThat(users).hasSize(2);
        assertThat(users).extracting(UserResponse::getUuid)
          .containsExactly(user1.getUuid(), user2.getUuid());
      });
  }
  
  @Test
  void getUserById_returnsUser() {
    UUID uuid = UUID.randomUUID();
    UserRequest userRequest = buildUserRequest();
    UserResponse expected = buildUserResponse(uuid, userRequest);
    
    Mockito.when(userService.getUser(uuid)).thenReturn(Mono.just(expected));
    
    webTestClient.get()
      .uri("/users/id/{uuid}", uuid)
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk()
      .expectBody(UserResponse.class)
      .consumeWith(response -> {
        UserResponse actual = response.getResponseBody();
        assertThat(actual).isNotNull();
        assertThat(actual.getUuid()).isEqualTo(expected.getUuid());
        assertThat(actual.getUserInformation().getEmail()).isEqualTo(expected.getUserInformation().getEmail());
        assertThat(actual.getUserInformation().getName()).isEqualTo(expected.getUserInformation().getName());
        assertThat(actual.getUserInformation().getPhones()).isEqualTo(expected.getUserInformation().getPhones());
        assertThat(actual.getIsActive()).isEqualTo(expected.getIsActive());
      });
  }
  
  @Test
  void createUser_returnsCreatedUser() {
    UserRequest request = buildUserRequest();
    UserResponse expected = buildUserResponse(UUID.randomUUID(), request);
    
    Mockito.when(userService.createUser(Mockito.any(UserRequest.class))).thenReturn(Mono.just(expected));
    
    webTestClient.post()
      .uri("/users")
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(request)
      .exchange()
      .expectStatus().isOk()
      .expectBody(UserResponse.class)
      .consumeWith(response -> {
        UserResponse actual = response.getResponseBody();
        assertThat(actual).isNotNull();
        assertThat(actual.getUserInformation().getEmail()).isEqualTo(expected.getUserInformation().getEmail());
        assertThat(actual.getUserInformation().getName()).isEqualTo(expected.getUserInformation().getName());
        assertThat(actual.getUserInformation().getPhones()).isEqualTo(expected.getUserInformation().getPhones());
        assertThat(actual.getIsActive()).isEqualTo(expected.getIsActive());
      });
  }
}