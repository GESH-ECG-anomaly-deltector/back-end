package com.gesh.backend.repository;

import com.gesh.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByPhone(String phone);

    Optional<User> findByEmail(String email);

    Optional<User> findByNationalCode(String nationalCode);

    Optional<User> findByPhoneOrNationalCode(String phone, String nationalCode);

    Optional<User> findByProfileId(String profileId);

    boolean existsByPhone(String phone);

    boolean existsByEmail(String email);
}