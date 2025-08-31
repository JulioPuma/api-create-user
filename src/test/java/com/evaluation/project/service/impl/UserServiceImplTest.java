package com.evaluation.project.service.impl;

import com.evaluation.project.config.ApplicationProperties;
import com.evaluation.project.model.dto.Phone;
import com.evaluation.project.model.dto.UserRequest;
import com.evaluation.project.model.entity.PhoneEntity;
import com.evaluation.project.model.entity.UserEntity;
import com.evaluation.project.repository.PhoneRepository;
import com.evaluation.project.repository.UserRepository;
import com.evaluation.project.util.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
  import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
  
  @Mock
  private UserRepository userRepository;
  @Mock
  private PhoneRepository phoneRepository;
  @Mock
  private JwtService jwtService;
  @Mock
  private ApplicationProperties applicationProperties;
  
  @InjectMocks
  private UserServiceImpl userService;
  
  @BeforeEach
  void setUp() {
  }
  
  private UserEntity buildUserEntity(UUID uuid) {
    LocalDateTime now = LocalDateTime.now().withNano(0);
    return UserEntity.builder()
      .uuid(uuid)
      .name("Juan Pérez")
      .email("juan.perez@example.com")
      .password("pass123")
      .createdAt(now)
      .modifiedAt(now)
      .lastLogin(now)
      .token("token1")
      .isActive(true)
      .build();
  }
  
  private PhoneEntity buildPhoneEntity(UUID uuid) {
    return PhoneEntity.builder()
      .phoneId(1)
      .uuid(uuid)
      .number("123456789")
      .cityCode("01")
      .countryCode("57")
      .build();
  }
  
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
  
  @Test
  void getUsers_returnsUserResponses() {
    UUID uuid = UUID.randomUUID();
    UserEntity userEntity = buildUserEntity(uuid);
    PhoneEntity phoneEntity = buildPhoneEntity(uuid);
    
    when(userRepository.findAll()).thenReturn(Flux.just(userEntity));
    when(phoneRepository.findAll()).thenReturn(Flux.just(phoneEntity));
    
    StepVerifier.create(userService.getUsers())
      .assertNext(userResponse -> {
        assertThat(userResponse.getUuid()).isEqualTo(uuid.toString());
        assertThat(userResponse.getUserInformation().getEmail()).isEqualTo(userEntity.getEmail());
        // phone mapping
        assertThat(userResponse.getUserInformation().getPhones()).hasSize(1);
        assertThat(userResponse.getUserInformation().getPhones().get(0).getNumber()).isEqualTo(phoneEntity.getNumber());
      })
      .verifyComplete();
  }
  
  @Test
  void getUser_returnsUserResponse() {
    UUID uuid = UUID.randomUUID();
    UserEntity userEntity = buildUserEntity(uuid);
    PhoneEntity phoneEntity = buildPhoneEntity(uuid);
    
    when(userRepository.findById(uuid)).thenReturn(Mono.just(userEntity));
    when(phoneRepository.findByUuid(uuid)).thenReturn(Flux.just(phoneEntity));
    
    StepVerifier.create(userService.getUser(uuid))
      .assertNext(userResponse -> {
        assertThat(userResponse.getUuid()).isEqualTo(uuid.toString());
        assertThat(userResponse.getUserInformation().getEmail()).isEqualTo(userEntity.getEmail());
        assertThat(userResponse.getUserInformation().getPhones()).hasSize(1);
        assertThat(userResponse.getUserInformation().getPhones().get(0).getNumber()).isEqualTo(phoneEntity.getNumber());
      })
      .verifyComplete();
  }
  
  @Test
  void createUser_success() {
    UserRequest request = buildUserRequest();
    UUID generatedUuid = UUID.randomUUID();
    // El repo save devuelve la entidad con uuid asignado
    UserEntity savedEntity = buildUserEntity(generatedUuid);
    PhoneEntity savedPhone = buildPhoneEntity(generatedUuid);
    
    when(applicationProperties.getEmailRegex()).thenReturn("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$");
    when(applicationProperties.getPasswordRegex()).thenReturn("[\\w\\W]+");
    when(userRepository.findByEmail(request.getEmail())).thenReturn(Mono.empty());
    when(jwtService.generate(any(UUID.class))).thenReturn("token1");
    when(userRepository.save(any(UserEntity.class))).thenReturn(Mono.just(savedEntity));
    when(phoneRepository.saveAll(anyList())).thenReturn(Flux.just(savedPhone));
    
    StepVerifier.create(userService.createUser(request))
      .assertNext(resp -> {
        assertThat(resp.getUserInformation().getEmail()).isEqualTo(request.getEmail());
        assertThat(resp.getToken()).isEqualTo("token1");
        assertThat(resp.getUuid()).isEqualTo(generatedUuid.toString());
        assertThat(resp.getUserInformation().getPhones()).hasSize(1);
      })
      .verifyComplete();
  }
  
  @Test
  void createUser_emailAlreadyExists_throwsException() {
    UserRequest request = buildUserRequest();
    UserEntity existing = buildUserEntity(UUID.randomUUID());
    
    when(userRepository.findByEmail(request.getEmail())).thenReturn(Mono.just(existing));
    when(applicationProperties.getEmailRegex()).thenReturn("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$");
    when(applicationProperties.getPasswordRegex()).thenReturn("[\\w\\W]+");
    
    StepVerifier.create(userService.createUser(request))
      .expectError(ApiException.class)
      .verify();
  }
  
  @Test
  void createUser_invalidEmail_throwsException() {
    UserRequest invalid = UserRequest.builder()
      .name("Juan Pérez")
      .email("invalid-email")
      .password("pass123")
      .phones(List.of())
      .build();
    
    when(applicationProperties.getEmailRegex()).thenReturn("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$");
    when(applicationProperties.getPasswordRegex()).thenReturn("[\\w\\W]+");
    
    StepVerifier.create(userService.createUser(invalid))
      .expectError(ApiException.class)
      .verify();
  }
}