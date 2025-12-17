// SystemLogRepository.java
package com.hnu.mail.repository;

import com.hnu.mail.model.SystemLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemLogRepository extends JpaRepository<SystemLog, Long> {
    Page<SystemLog> findByType(SystemLog.LogType type, Pageable pageable);
    Page<SystemLog> findByModule(String module, Pageable pageable);
    Page<SystemLog> findByUsername(String username, Pageable pageable);
    void deleteAllByCreatedAtBefore(java.time.LocalDateTime dateTime);
}
