package com.evaluation.project.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Setter
@Getter
@Builder
@Schema(name = "UserResponse", description = "Response payload for user information")
public class UserResponse {

  @Schema(description = "Unique identifier for the user", example = "550e8400-e29b-41d4-a716-446655440000")
  private String uuid;
  
  @Schema(description = "Full name of the user", example = "John Doe")
  private UserRequest userInformation;
  
  @Schema(description = "Timestamp when the user was created", example = "2023-10-01T12:00:00")
  private LocalDateTime created;
  
  @Schema(description = "Timestamp when the user was last modified", example = "2023-10-02T15:30:00")
  private LocalDateTime modified;
  
  @Schema(description = "Timestamp when the user last logged in", example = "2023-10-03T09:45:00")
  private LocalDateTime lastLogin;
  
  @Schema(description = "Authentication token for the user", example = "")
  private String token;
  
  @Schema(description = "Indicates if the user account is active", example = "true")
  private Boolean isActive;
}
