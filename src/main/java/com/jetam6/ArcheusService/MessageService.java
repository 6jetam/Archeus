package com.jetam6.ArcheusService;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.jetam6.ArcheusModel.ArcheusUser;
import com.jetam6.ArcheusModel.Messages;
import com.jetam6.ArcheusRepository.MessageRepository;
import com.jetam6.ArcheusRepository.UserRepository;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;
    
    @Autowired
    private UserRepository userRepository;

    public List<Messages> getConversation(Long user1, Long user2) {
        return messageRepository.findBySenderIdAndReceiverIdOrReceiverIdAndSenderId(user1, user2, user1, user2);
    }

    public Messages saveMessage(Messages message) {
        ArcheusUser currentUser = getCurrentUser();
        message.setSenderId(currentUser.getId());


        message.setCreatedAt(LocalDateTime.now());

        return messageRepository.save(message);
    }
    
    public void deleteMessage(Long id) {
        Messages msg = messageRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Správa neexistuje"));

        ArcheusUser currentUser = getCurrentUser();

        if (!msg.getSenderId().equals(currentUser.getId())) {
            throw new RuntimeException("Nemáš oprávnenie vymazať túto správu");
        }

        messageRepository.deleteById(id);
    }

    public Messages updateMessage(Long id, String newContent) {
        Messages msg = messageRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Správa neexistuje"));

        ArcheusUser currentUser = getCurrentUser();

        if (!msg.getSenderId().equals(currentUser.getId())) {
            throw new RuntimeException("Nemáš oprávnenie upraviť túto správu");
        }

        msg.setContent(newContent);
        
        Messages updated = messageRepository.save(msg);

        System.out.println("✅ Updated message ID: " + updated.getId());
        System.out.println("📝 New content: " + updated.getContent());

        return updated;
    }

    
    private ArcheusUser getCurrentUser() {
        return userRepository.findByEmail(getCurrentUserEmail())
            .orElseThrow(() -> new RuntimeException("Prihlásený používateľ neexistuje"));
    }
    
    private String getCurrentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

   
}
