package com.orderflow.repository;

import com.orderflow.entity.Greeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GreetingRepository extends JpaRepository<Greeting, Long> {

    Optional<Greeting> findFirstByActiveTrueOrderByIdAsc();

    List<Greeting> findByActiveTrue();
}
