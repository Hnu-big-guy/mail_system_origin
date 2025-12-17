// FilterRuleRepository.java
package com.hnu.mail.repository;

import com.hnu.mail.model.FilterRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FilterRuleRepository extends JpaRepository<FilterRule, Long> {
    
    List<FilterRule> findByIsActiveTrueOrderByPriorityAsc();
    
    List<FilterRule> findByTypeAndIsActiveTrue(FilterRule.FilterType type);
}
