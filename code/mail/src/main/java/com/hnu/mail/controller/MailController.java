// MailController.java
package com.hnu.mail.controller;

import com.hnu.mail.dto.*;
import com.hnu.mail.model.Mail;
import com.hnu.mail.security.UserPrincipal;
import com.hnu.mail.service.MailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/mail")
@RequiredArgsConstructor
public class MailController {

  private final MailService mailService;

  @PostMapping("/send")
  public ResponseEntity<?> sendMail(
      @Valid @RequestBody SendMailRequest request,
      @AuthenticationPrincipal UserPrincipal userPrincipal) {

    try {
      Mail mail = mailService.sendMail(
          request,
          userPrincipal.getId(),
          null
      );
      return ResponseEntity.ok(new ApiResponse(true, "邮件发送成功"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(new ApiResponse(false, e.getMessage()));
    }
  }

  @PostMapping("/send-with-attachments")
  public ResponseEntity<?> sendMailWithAttachments(
      @Valid @RequestPart SendMailRequest request,
      @RequestPart(required = false) List<MultipartFile> attachments,
      @AuthenticationPrincipal UserPrincipal userPrincipal) {

    try {
      Mail mail = mailService.sendMail(
          request,
          userPrincipal.getId(),
          attachments
      );
      return ResponseEntity.ok(new ApiResponse(true, "邮件发送成功"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(new ApiResponse(false, e.getMessage()));
    }
  }

  @GetMapping("/inbox")
  public ResponseEntity<?> getInbox(
      @PageableDefault(size = 20) Pageable pageable,
      @AuthenticationPrincipal UserPrincipal userPrincipal) {

    Page<MailDto> mails = mailService.getUserMails(
        userPrincipal.getId(),
        "INBOX",
        pageable
    );
    return ResponseEntity.ok(mails);
  }

  @GetMapping("/sent")
  public ResponseEntity<?> getSentMails(
      @PageableDefault(size = 20) Pageable pageable,
      @AuthenticationPrincipal UserPrincipal userPrincipal) {

    Page<MailDto> mails = mailService.getUserMails(
        userPrincipal.getId(),
        "SENT",
        pageable
    );
    return ResponseEntity.ok(mails);
  }

  @GetMapping("/draft")
  public ResponseEntity<?> getDraftMails(
      @PageableDefault(size = 20) Pageable pageable,
      @AuthenticationPrincipal UserPrincipal userPrincipal) {

    Page<MailDto> mails = mailService.getUserMails(
        userPrincipal.getId(),
        "DRAFT",
        pageable
    );
    return ResponseEntity.ok(mails);
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> getMail(
      @PathVariable Long id,
      @AuthenticationPrincipal UserPrincipal userPrincipal) {

    try {
      MailDto mail = mailService.readMail(id, userPrincipal.getId());
      return ResponseEntity.ok(mail);
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(new ApiResponse(false, e.getMessage()));
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> deleteMail(
      @PathVariable Long id,
      @AuthenticationPrincipal UserPrincipal userPrincipal) {

    try {
      mailService.deleteMail(id, userPrincipal.getId());
      return ResponseEntity.ok(new ApiResponse(true, "邮件已删除"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(new ApiResponse(false, e.getMessage()));
    }
  }

  @PutMapping("/{id}/read")
  public ResponseEntity<?> markAsRead(
      @PathVariable Long id,
      @AuthenticationPrincipal UserPrincipal userPrincipal) {

    try {
      MailDto mail = mailService.readMail(id, userPrincipal.getId());
      return ResponseEntity.ok(mail);
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(new ApiResponse(false, e.getMessage()));
    }
  }

  @PutMapping("/{id}/move")
  public ResponseEntity<?> moveToFolder(
      @PathVariable Long id,
      @RequestParam String folder,
      @AuthenticationPrincipal UserPrincipal userPrincipal) {

    try {
      mailService.moveToFolder(id, folder, userPrincipal.getId());
      return ResponseEntity.ok(new ApiResponse(true, "邮件已移动"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(new ApiResponse(false, e.getMessage()));
    }
  }

  @PostMapping("/draft")
  public ResponseEntity<?> saveDraft(
      @Valid @RequestBody SendMailRequest request,
      @AuthenticationPrincipal UserPrincipal userPrincipal) {

    try {
      Mail mail = mailService.saveDraft(
          request,
          userPrincipal.getId(),
          null
      );
      return ResponseEntity.ok(new ApiResponse(true, "草稿保存成功"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(new ApiResponse(false, e.getMessage()));
    }
  }

  @PostMapping("/draft-with-attachments")
  public ResponseEntity<?> saveDraftWithAttachments(
      @Valid @RequestPart SendMailRequest request,
      @RequestPart(required = false) List<MultipartFile> attachments,
      @AuthenticationPrincipal UserPrincipal userPrincipal) {

    try {
      Mail mail = mailService.saveDraft(
          request,
          userPrincipal.getId(),
          attachments
      );
      return ResponseEntity.ok(new ApiResponse(true, "草稿保存成功"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(new ApiResponse(false, e.getMessage()));
    }
  }
}