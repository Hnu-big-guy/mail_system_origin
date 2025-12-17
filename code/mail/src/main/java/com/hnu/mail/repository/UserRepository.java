// UserRepository.java
package com.hnu.mail.repository;

import com.hnu.mail.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByUsername(String username);

  Optional<User> findByEmail(String email);

  Boolean existsByUsername(String username);

  Boolean existsByEmail(String email);

  @Query("SELECT u FROM User u WHERE u.role = 'ADMIN'")
  List<User> findAllAdmins();

  List<User> findByStatus(User.UserStatus status);

  @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :startDate")
  Long countUsersAfter(LocalDateTime startDate);

  @Query("SELECT u.id FROM User u")
  List<Long> findAllUserIds();

  Long countByStatus(User.UserStatus status);
}