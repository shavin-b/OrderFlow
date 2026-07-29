package com.orderflow.repository;

import com.orderflow.entity.MonthlyStatistic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MonthlyStatisticRepository extends JpaRepository<MonthlyStatistic, Long> {

    Optional<MonthlyStatistic> findByYearMonth(String yearMonth);

    List<MonthlyStatistic> findAllByOrderByYearMonthAsc();
}
