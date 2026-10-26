package com.bus.booking.repository;

import com.bus.booking.entity.RouteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RouteRepository extends JpaRepository<RouteEntity, Long> {

    List<RouteEntity> findByOriginAndDestination(String origin, String destination);

    List<RouteEntity> findByOperatorId(Long operatorId);

    List<RouteEntity> findByStatus(String status);

    @Query("SELECT r FROM RouteEntity r JOIN FETCH r.operator o WHERE o.id = :operatorId ORDER BY r.id ASC")
    List<RouteEntity> findByOperatorIdWithOperator(@Param("operatorId") Long operatorId);

    @Query("SELECT r FROM RouteEntity r JOIN FETCH r.operator o ORDER BY r.id ASC")
    List<RouteEntity> findAllWithOperator();

    @Query("SELECT r FROM RouteEntity r JOIN FETCH r.operator o WHERE r.id = :id")
    Optional<RouteEntity> findByIdWithOperator(@Param("id") Long id);
}
