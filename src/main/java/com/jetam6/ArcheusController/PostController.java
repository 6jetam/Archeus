package com.jetam6.ArcheusController;

import com.jetam6.ArcheusModel.ArcheusUser;
import com.jetam6.ArcheusModel.Comment;
import com.jetam6.ArcheusModel.Post;
import com.jetam6.ArcheusModel.Reaction;
import com.jetam6.ArcheusRepository.CommentRepository;
import com.jetam6.ArcheusRepository.PostRepository;
import com.jetam6.ArcheusRepository.ReactionRepository;
import com.jetam6.ArcheusRepository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.*;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/posts")
@CrossOrigin(origins = "*")
public class PostController {

	@Autowired
	private PostRepository postRepository;

	@Autowired
	private CommentRepository commentRepository;

	@Autowired
	private ReactionRepository reactionRepository;

	@Autowired
	private UserRepository userRepository;

	private final Path uploadDir = Paths.get("uploads");

	// 📌 Vytvorenie príspevku
	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<Post> createPost(@RequestParam("text") String text,
			@RequestParam(value = "image", required = false) MultipartFile image) {
		Post post = new Post();
		post.setText(text);
		post.setCreatedAt(LocalDateTime.now());

		if (image != null && !image.isEmpty()) {
			try {
				String filename = UUID.randomUUID() + "_" + StringUtils.cleanPath(image.getOriginalFilename());
				Path filePath = uploadDir.resolve(filename);
				Files.createDirectories(uploadDir);
				Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
				post.setImageUrl("/api/posts/image/" + filename);
			} catch (IOException e) {
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
			}
		}

		return ResponseEntity.ok(postRepository.save(post));
	}

	// 📌 Získanie všetkých príspevkov
	@GetMapping
	public List<Post> getAllPosts() {
		return postRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
	}

	// 📌 Načítanie obrázku
	@GetMapping("/image/{filename:.+}")
	public ResponseEntity<Resource> getImage(@PathVariable String filename) {
		try {
			Path filePath = uploadDir.resolve(filename).normalize();
			Resource resource = new UrlResource(filePath.toUri());

			if (resource.exists()) {
				return ResponseEntity.ok()
						.contentType(MediaTypeFactory.getMediaType(resource).orElse(MediaType.APPLICATION_OCTET_STREAM))
						.body(resource);
			} else {
				return ResponseEntity.notFound().build();
			}

		} catch (MalformedURLException e) {
			return ResponseEntity.badRequest().build();
		}
	}

	// 📌 Pridanie komentára k príspevku
	@PostMapping("/{postId}/comments")
	public ResponseEntity<Comment> addComment(@PathVariable Long postId, @RequestBody Comment comment) {
		return postRepository.findById(postId).map(post -> {
			comment.setPost(post);

			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			String email = auth.getName(); // ← email z tokenu

			ArcheusUser user = userRepository.findByEmail(email)
					.orElseThrow(() -> new RuntimeException("Používateľ nenájdený"));

			comment.setAuthorName(user.getFullName()); // ← teraz sa uloží meno, napr. "Matej Šoltés"
			
			comment.setAuthorEmail(user.getEmail());  
			comment.setCreatedAt(LocalDateTime.now());
			return ResponseEntity.ok(commentRepository.save(comment));
		}).orElse(ResponseEntity.notFound().build());
	}

	// 📌 Získanie komentárov pre daný post
	@GetMapping("/{postId}/comments")
	public List<Comment> getComments(@PathVariable Long postId) {
		return commentRepository.findByPostId(postId);
	}

	// 📌 Úprava komentára
	@PutMapping("/comments/{commentId}")
	public ResponseEntity<Comment> updateComment(@PathVariable Long commentId, @RequestBody Comment updatedComment) {
		Optional<Comment> optionalComment = commentRepository.findById(commentId);
		if (optionalComment.isEmpty()) {
			return ResponseEntity.notFound().build();
		}

		Comment comment = optionalComment.get();

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String currentUsername = auth.getName();

		boolean isAdmin = auth.getAuthorities().stream().anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN"));

		System.out.println("➡️ Aktuálne prihlásený používateľ: " + currentUsername);
		System.out.println("📝 Autor komentára: " + comment.getAuthor());
		System.out.println("🛡️ Má admin rolu? " + isAdmin);

//        if (!comment.getAuthor().equals(currentUsername) && !isAdmin) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
//        }

		comment.setText(updatedComment.getText());
		comment.setUpdatedAt(LocalDateTime.now());

		return ResponseEntity.ok(commentRepository.save(comment));
	}

	// 📌 Vymazanie komentára
	@DeleteMapping("/{postId}/comments/{commentId}")
	public ResponseEntity<Void> deleteComment(@PathVariable Long postId, @PathVariable Long commentId) {
		if (!commentRepository.existsById(commentId)) {
			return ResponseEntity.notFound().build();
		}

		commentRepository.deleteById(commentId);
		return ResponseEntity.noContent().build();
	}

	// 📌 Pridanie alebo update reakcie na post
	@PostMapping("/{postId}/reactions")
	public ResponseEntity<Reaction> addOrUpdateReaction(@PathVariable Long postId, @RequestBody Reaction newReaction) {
		return postRepository.findById(postId).map(post -> {
			Optional<Reaction> existing = reactionRepository.findByPostIdAndUserId(postId, newReaction.getUserId());

			Reaction reaction = existing.orElse(new Reaction());
			reaction.setPost(post);
			reaction.setUserId(newReaction.getUserId());
			reaction.setType(newReaction.getType());

			return ResponseEntity.ok(reactionRepository.save(reaction));
		}).orElse(ResponseEntity.notFound().build());
	}
}
