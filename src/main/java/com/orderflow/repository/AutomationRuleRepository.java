package com.orderflow.repository;

import com.orderflow.entity.AutomationRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AutomationRuleRepository extends JpaRepository<AutomationRule, Long> {

    @Query("SELECT DISTINCT r FROM AutomationRule r LEFT JOIN FETCH r.keywords LEFT JOIN FETCH r.replies WHERE r.active = true ORDER BY r.priority DESC")
    List<AutomationRule> findAllActiveWithKeywordsAndReplies();

    List<AutomationRule> findByActiveOrderByPriorityDesc(boolean active);
}
