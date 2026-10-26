package com.bus.booking.repository;

import com.bus.booking.entity.PaymentEntity;
import com.bus.booking.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {
    Optional<PaymentEntity> findByTicketId(Long ticketId);
    Optional<PaymentEntity> findByTicketIdAndStatus(Long ticketId, PaymentStatus status);
    Optional<PaymentEntity> findByTransactionCode(String transactionCode);
    boolean existsByTransactionCode(String transactionCode);
    List<PaymentEntity> findByStatus(PaymentStatus status);
}
