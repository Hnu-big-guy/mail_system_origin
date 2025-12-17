// FilterRuleDto.java
package com.hnu.mail.dto;

import com.hnu.mail.model.FilterRule;
import lombok.Data;

@Data
public class FilterRuleDto {
    
    private Long id;
    private FilterRule.FilterType type;
    private String pattern;
    private FilterRule.Action action;
    private Boolean isActive;
    private Integer priority;
    private String description;
    
    public static FilterRuleDto fromEntity(FilterRule rule) {
        FilterRuleDto dto = new FilterRuleDto();
        dto.setId(rule.getId());
        dto.setType(rule.getType());
        dto.setPattern(rule.getPattern());
        dto.setAction(rule.getAction());
        dto.setIsActive(rule.getIsActive());
        dto.setPriority(rule.getPriority());
        dto.setDescription(rule.getDescription());
        return dto;
    }
    
    public FilterRule toEntity() {
        FilterRule rule = new FilterRule();
        rule.setId(this.getId());
        rule.setType(this.getType());
        rule.setPattern(this.getPattern());
        rule.setAction(this.getAction());
        rule.setIsActive(this.getIsActive());
        rule.setPriority(this.getPriority());
        rule.setDescription(this.getDescription());
        return rule;
    }
}