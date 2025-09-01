package com.evaluation.project.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Setter
@Getter
@Builder
@Schema(name = "UserRequest", description = "Body with user information for creation or update")
public class UserRequest {
  @JsonProperty("name")
  @NotNull
  @Schema(description = "Full name of the user", example = "John Doe")
  private String name;
  
  @JsonProperty("email")
  @NotNull
  @Schema(description = "Email address of the user", example = "julio@gmail.com")
  private String email;
  
  @JsonProperty("password")
  @NotNull
  @Schema(description = "Password for the user account", example = "password123")
  private String password;
  
  @JsonProperty("phones")
  @Schema(description = "List of phone numbers associated with the user")
  private List<Phone> phones;
}
