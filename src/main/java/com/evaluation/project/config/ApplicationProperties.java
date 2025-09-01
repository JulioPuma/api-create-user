package com.evaluation.project.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
public class ApplicationProperties {
  
  @Value("${app.regex.email}")
  private String emailRegex;
  
  @Value("${app.regex.password}")
  private String passwordRegex;
  
}
