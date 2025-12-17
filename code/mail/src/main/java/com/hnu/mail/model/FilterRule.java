// FilterRule.java
package com.hnu.mail.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "filter_rules")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FilterRule {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  private FilterType type;

  @Size(max = 200)
  private String pattern;

  @Enumerated(EnumType.STRING)
  private Action action;

  private Boolean isActive = true;

  private Integer priority = 1;

  @Size(max = 500)
  private String description;

  public enum FilterType {
    EMAIL, SUBJECT, CONTENT, IP_ADDRESS
  }

  public enum Action {
    BLOCK, ALLOW, MOVE_TO_SPAM, MARK_AS_READ
  }
}