package com.bus.booking.repository;

import com.bus.booking.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUsername(String username);
    Optional<UserEntity> findByEmail(String email);
    List<UserEntity> findByOperatorId(Long operatorId);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
