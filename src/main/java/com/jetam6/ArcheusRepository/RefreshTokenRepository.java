package com.jetam6.ArcheusRepository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.jetam6.ArcheusModel.ArcheusUser;
import com.jetam6.ArcheusModel.RefreshToken;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
   
    void deleteByUser(ArcheusUser user);
    
}








