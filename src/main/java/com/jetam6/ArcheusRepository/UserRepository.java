	package com.jetam6.ArcheusRepository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jetam6.ArcheusModel.ArcheusUser;

import java.util.Optional;

public interface UserRepository extends JpaRepository<ArcheusUser, Long> {
    Optional<ArcheusUser> findByEmail(String email);

}
