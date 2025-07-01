package com.jetam6.ArcheusRepository;

import com.jetam6.ArcheusModel.Messages;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MessageRepository extends JpaRepository<Messages, Long> {
    List<Messages> findBySenderIdAndReceiverIdOrReceiverIdAndSenderId(
        Long s1, Long r1, Long s2, Long r2
    );
}
