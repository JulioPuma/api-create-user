package com.evaluation.project.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@EqualsAndHashCode
@Schema(name = "Phone", description = "Phone details associated with a user")
public class Phone {

  @JsonProperty("number")
  @Schema(description = "Phone number", example = "1234567890")
  private String number;

  @JsonProperty("cityCode")
  @Schema(description = "City code of the phone number", example = "1")
  private String cityCode;

  @JsonProperty("countryCode")
  @Schema(description = "Country code of the phone number", example = "57")
  private String countryCode;
}
