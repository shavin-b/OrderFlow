package com.orderflow.repository;

import com.orderflow.entity.DailyStatistic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyStatisticRepository extends JpaRepository<DailyStatistic, Long> {

    Optional<DailyStatistic> findByStatDate(LocalDate statDate);

    List<DailyStatistic> findByStatDateBetweenOrderByStatDateAsc(LocalDate startDate, LocalDate endDate);
}
