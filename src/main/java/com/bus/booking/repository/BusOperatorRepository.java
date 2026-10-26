package com.bus.booking.repository;

import com.bus.booking.entity.BusOperatorEntity;
import com.bus.booking.enums.OperatorStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BusOperatorRepository extends JpaRepository<BusOperatorEntity, Long> {
    Optional<BusOperatorEntity> findByCode(String code);
    List<BusOperatorEntity> findByStatus(OperatorStatus status);
    boolean existsByCode(String code);
}
