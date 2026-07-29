package com.orderflow.repository;

import com.orderflow.entity.Customer;
import com.orderflow.entity.Customer.CustomerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByWaId(String waId);

    Optional<Customer> findByPhone(String phone);

    boolean existsByWaId(String waId);

    boolean existsByPhone(String phone);

    Page<Customer> findByStatus(CustomerStatus status, Pageable pageable);

    @Query("""
           SELECT c FROM Customer c
           WHERE (:search IS NULL
                  OR LOWER(c.name)  LIKE LOWER(CONCAT('%', :search, '%'))
                  OR LOWER(c.phone) LIKE LOWER(CONCAT('%', :search, '%'))
                  OR LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%')))
           """)
    Page<Customer> searchCustomers(@Param("search") String search, Pageable pageable);
}
