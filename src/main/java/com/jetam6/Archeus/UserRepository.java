package com.jetam6.Archeus;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<ArcheusUser, Long> {
    Optional<ArcheusUser> findByEmail(String email);
    
}
