package com.jetam6.ArcheusRepository;

import com.jetam6.ArcheusModel.Reaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface ReactionRepository extends JpaRepository<Reaction, Long> {
    Optional<Reaction> findByPostIdAndUserId(Long postId, String userId);
    List<Reaction> findByPostId(Long postId);
}
