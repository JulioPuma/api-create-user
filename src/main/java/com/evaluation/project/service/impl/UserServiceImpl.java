package com.evaluation.project.service.impl;

import com.evaluation.project.config.ApplicationProperties;
import com.evaluation.project.model.dto.Phone;
import com.evaluation.project.model.dto.UserRequest;
import com.evaluation.project.model.dto.UserResponse;
import com.evaluation.project.model.entity.PhoneEntity;
import com.evaluation.project.model.entity.UserEntity;
import com.evaluation.project.repository.PhoneRepository;
import com.evaluation.project.repository.UserRepository;
import com.evaluation.project.util.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl {
  
  private final UserRepository userRepository;
  private final PhoneRepository phoneRepository;
  private final JwtService jwtService;
  private final ApplicationProperties applicationProperties;

  @Transactional(readOnly = true)
  public Flux<UserResponse> getUsers() {
    return
    Mono.zip(
            userRepository.findAll().collectList(),
            phoneRepository.findAll().collectList(),
            (userEntities, phoneEntities) ->
              userEntities.stream()
              .map(userEntity -> mapToUserResponse(userEntity, phoneEntities))
              .toList())
        .flatMapMany(Flux::fromIterable)
        .doOnError(throwable -> log.error("Error fetching users: {}", throwable.getMessage()))
        .subscribeOn(Schedulers.boundedElastic());
  }

  @Transactional(readOnly = true)
  public Mono<UserResponse> getUser(UUID uuid) {
    return Mono.zip(
            userRepository.findById(uuid),
            phoneRepository.findByUuid(uuid).collectList(),
            this::mapToUserResponse)
          .doOnError(throwable -> log.error("Error fetching user with UUID {}: {}", uuid, throwable.getMessage()))
          .subscribeOn(Schedulers.boundedElastic());
  }

  @Transactional
  public Mono<UserResponse> createUser(UserRequest userRequest) {
         return
            validateEmailUniqueness(userRequest.getEmail())
            .then(validatePasswordFormat(userRequest.getPassword()))
            .then(buildUserEntity(userRequest)
                  .flatMap(userRepository::save)
                  .doOnError(throwable -> log.error("Error saving user: {}", throwable.getMessage()))
                  .flatMap(userEntity ->
                        buildPhoneEntity(userRequest.getPhones(), userEntity.getUuid())
                        .flatMapMany(phoneRepository::saveAll)
                        .doOnError(throwable -> log.error("Error saving phones: {}", throwable.getMessage()))
                        .collectList()
                        .map(phoneEntities -> mapToUserResponse(userEntity, phoneEntities))))
            .subscribeOn(Schedulers.boundedElastic())
            .doOnSuccess(userResponse -> log.info("User created successfully: {}", userResponse.getUuid()))
            .doOnTerminate(() -> log.debug("createUser process finished"));
  }

  private Mono<Void> validateEmailUniqueness(String email) {
    if(email.matches(applicationProperties.getEmailRegex())){
      return
              userRepository.findByEmail(email)
              .flatMap(existingUser -> Mono.error(new ApiException("Email already in use")))
              .then();
    }
    return Mono.error(new ApiException("Invalid email format"));
  }
  
  private Mono<Void> validatePasswordFormat(String password) {
    if(password.matches(applicationProperties.getPasswordRegex())){
      return Mono.empty();
    }
    return Mono.error(new ApiException("Password does not meet complexity requirements"));
  }

  private Mono<UserEntity> buildUserEntity(UserRequest userRequest) {
    return Mono.just(
      UserEntity.builder()
      .name(userRequest.getName())
      .email(userRequest.getEmail())
      .password(userRequest.getPassword())
      .createdAt(LocalDateTime.now())
      .modifiedAt(null)
      .lastLogin(LocalDateTime.now())
      .token(jwtService.generate(UUID.randomUUID()))
      .isActive(Boolean.TRUE)
      .build());
  }

  private Mono<List<PhoneEntity>> buildPhoneEntity(List<Phone> phones, UUID uuid) {
    return Mono.just(
            phones.stream()
            .map(phone ->
                    PhoneEntity.builder()
                            .uuid(uuid)
                            .number(phone.getNumber())
                            .cityCode(phone.getCityCode())
                            .countryCode(phone.getCountryCode())
                            .build())
            .toList());
  }

  private UserResponse mapToUserResponse(
          UserEntity userEntity,
          List<PhoneEntity> phoneEntities) {
    return UserResponse.builder()
            .uuid(userEntity.getUuid().toString())
            .userInformation(mapToUserRequest(userEntity, phoneEntities))
            .created(userEntity.getCreatedAt())
            .modified(userEntity.getModifiedAt())
            .lastLogin(userEntity.getLastLogin())
            .token(userEntity.getToken())
            .isActive(userEntity.isActive())
            .build();
  }

  private UserRequest mapToUserRequest(
          UserEntity userEntity,
          List<PhoneEntity> phoneEntities){
    return UserRequest.builder()
      .name(userEntity.getName())
      .email(userEntity.getEmail())
      .password(userEntity.getPassword())
      .phones(phoneEntities.stream()
              .filter(phoneEntity -> phoneEntity.getUuid().equals(userEntity.getUuid()))
              .map(this::mapToPhone)
              .toList())
      .build();
  }

  private Phone mapToPhone(PhoneEntity phoneEntity) {
    return Phone.builder()
      .number(phoneEntity.getNumber())
      .cityCode(phoneEntity.getCityCode())
      .countryCode(phoneEntity.getCountryCode())
      .build();
  }
}
