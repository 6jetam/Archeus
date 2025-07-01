package com.jetam6.ArcheusController;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jetam6.ArcheusModel.Messages;
import com.jetam6.ArcheusService.MessageService;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @GetMapping
    public List<Messages> getConversation(@RequestParam Long user1, @RequestParam Long user2) {
        return messageService.getConversation(user1, user2);
    }

    @PostMapping
    public Messages sendMessage(@RequestBody Messages message) {
        return messageService.saveMessage(message);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        messageService.deleteMessage(id);
    }

    @PutMapping("/{id}")
    public Messages update(@PathVariable Long id, @RequestBody Map<String, String> body) {
        System.out.println("➡️ PUT /messages/" + id);
        System.out.println("📥 Content in request: " + body.get("content"));
        return messageService.updateMessage(id, body.get("content"));
    }
}
