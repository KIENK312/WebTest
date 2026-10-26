package com.bus.booking.repository;

import com.bus.booking.entity.BusEntity;
import com.bus.booking.enums.BusStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BusRepository extends JpaRepository<BusEntity, Long> {

    Optional<BusEntity> findByPlateNumber(String plateNumber);

    List<BusEntity> findByOperatorId(Long operatorId);

    List<BusEntity> findByStatus(BusStatus status);

    List<BusEntity> findByOperatorIdAndStatus(Long operatorId, BusStatus status);

    boolean existsByPlateNumber(String plateNumber);

    @Query("SELECT b FROM BusEntity b JOIN FETCH b.operator o WHERE o.id = :operatorId ORDER BY b.id ASC")
    List<BusEntity> findByOperatorIdWithOperator(@Param("operatorId") Long operatorId);

    @Query("SELECT b FROM BusEntity b JOIN FETCH b.operator o ORDER BY b.id ASC")
    List<BusEntity> findAllWithOperator();

    @Query("SELECT b FROM BusEntity b JOIN FETCH b.operator o WHERE b.id = :id")
    Optional<BusEntity> findByIdWithOperator(@Param("id") Long id);
}
