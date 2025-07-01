package com.jetam6.ArcheusRepository;

import com.jetam6.ArcheusModel.ArcheusUser;
import com.jetam6.ArcheusModel.VerificationToken;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByToken(String token);
    void deleteByUser(ArcheusUser user);
}


