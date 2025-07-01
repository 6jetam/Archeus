package com.jetam6.ArcheusModel;

import jakarta.persistence.*;

@Entity
public class Reaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;    // like, love, haha, etc.
    private String userId;  // môže byť email alebo meno používateľa

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;

    // Gettery a settery
    public Long getId() { return id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public Post getPost() { return post; }
    public void setPost(Post post) { this.post = post; }
}
