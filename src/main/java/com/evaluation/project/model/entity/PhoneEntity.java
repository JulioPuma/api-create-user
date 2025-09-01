package com.evaluation.project.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@Table(name = "PHONES")
public class PhoneEntity {
  @Id
  private Integer phoneId;
  private UUID uuid;
  private String number;
  private String cityCode;
  private String countryCode;
}
