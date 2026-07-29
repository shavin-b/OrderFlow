package com.orderflow.repository;

import com.orderflow.entity.AnalyticsRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AnalyticsRecordRepository extends JpaRepository<AnalyticsRecord, Long> {

    List<AnalyticsRecord> findByRecordedAtBetween(LocalDateTime start, LocalDateTime end);

    List<AnalyticsRecord> findByMetricCategory(String category);

    @Query("SELECT a.keywordPattern, COUNT(a) FROM AnalyticsRecord a WHERE a.keywordPattern IS NOT NULL AND a.recordedAt BETWEEN :start AND :end GROUP BY a.keywordPattern ORDER BY COUNT(a) DESC")
    List<Object[]> findTopKeywordUsage(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
