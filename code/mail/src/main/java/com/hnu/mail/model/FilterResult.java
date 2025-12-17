// FilterResult.java
package com.hnu.mail.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FilterResult {
  private boolean blocked;
  private String message;
}
