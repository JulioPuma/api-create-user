package com.evaluation.project.model.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Setter
@Getter
@Builder
@AllArgsConstructor
@Table(name = "USERS")
public class UserEntity {
  @Id
  private UUID uuid;
  private String name;
  private String email;
  private String password;
  private LocalDateTime createdAt;
  private LocalDateTime modifiedAt;
  private LocalDateTime lastLogin;
  private String token;
  private boolean isActive;
}
