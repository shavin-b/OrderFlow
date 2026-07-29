package com.orderflow.repository;

import com.orderflow.entity.Reply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReplyRepository extends JpaRepository<Reply, Long> {

    List<Reply> findByAutomationRuleIdOrderByReplyOrderAsc(Long ruleId);
}
