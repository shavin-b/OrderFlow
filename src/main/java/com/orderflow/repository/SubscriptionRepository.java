package com.orderflow.repository;

import com.orderflow.entity.Subscription;
import com.orderflow.entity.Subscription.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    List<Subscription> findByUserId(Long userId);

    @Query("SELECT s FROM Subscription s WHERE s.user.id = :userId ORDER BY s.endDate DESC")
    Optional<Subscription> findLatestByUserId(@Param("userId") Long userId);

    List<Subscription> findByStatus(SubscriptionStatus status);
}
