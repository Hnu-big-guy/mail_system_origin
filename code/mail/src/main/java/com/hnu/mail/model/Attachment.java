// Attachment.java
package com.hnu.mail.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "attachments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Attachment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank
  @Size(max = 255)
  private String filename;

  @Size(max = 100)
  private String fileType;

  private Long fileSize;

  @Size(max = 500)
  private String filePath;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "mail_id")
  private Mail mail;

  private LocalDateTime uploadedAt = LocalDateTime.now();
}