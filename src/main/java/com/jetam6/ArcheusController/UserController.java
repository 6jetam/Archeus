package com.jetam6.ArcheusController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.jetam6.ArcheusModel.ArcheusUser;
import com.jetam6.ArcheusModel.Messages;
import com.jetam6.ArcheusModel.PasswordChangeRequest;
import com.jetam6.ArcheusModel.Post;
import com.jetam6.ArcheusModel.RefreshToken;
import com.jetam6.ArcheusModel.RefreshTokenRequest;
import com.jetam6.ArcheusModel.RegisterRequest;
import com.jetam6.ArcheusModel.VerificationToken;
import com.jetam6.ArcheusRepository.PostRepository;
import com.jetam6.ArcheusRepository.RefreshTokenRepository;
import com.jetam6.ArcheusRepository.UserRepository;
import com.jetam6.ArcheusRepository.VerificationTokenRepository;
import com.jetam6.ArcheusService.EmailService;
import com.jetam6.ArcheusService.JwtService;
import com.jetam6.ArcheusService.MessageService;

import org.springframework.web.bind.annotation.PathVariable;


import jakarta.annotation.PostConstruct;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;
	@Autowired
	private PasswordEncoder passwordEncoder;
	@Autowired
	private RefreshTokenRepository refreshTokenRepository;
	@Autowired
	private VerificationTokenRepository verificationTokenRepository;
	@Autowired
	private EmailService emailService;
	@Autowired
	private PostRepository postRepository;
	@Autowired
	private MessageService messageService;
	@Autowired
	private AuthenticationManager authenticationManager;


	@PostMapping("/register")
	public ResponseEntity<?> registerUser(@RequestBody RegisterRequest request) {
	    System.out.println("Prijatý používateľ: " + request.getMeno() + " " + request.getPriezvisko());

	    // ✅ Kontrola duplicity e-mailu
	    if (userRepository.findByEmail(request.getEmail()).isPresent()) {
	        return ResponseEntity
	            .badRequest()
	            .body(Map.of("error", "Používateľ s týmto e-mailom už existuje."));
	    }

	    String fullName = request.getMeno() + " " + request.getPriezvisko();
	    
	    ArcheusUser user = new ArcheusUser();
	    user.setFullName(fullName);
	    user.setEmail(request.getEmail());
	    user.setPassword(passwordEncoder.encode(request.getPassword()));
	    user.setRole(ArcheusUser.Role.ROLE_USER);
	    user.setEnabled(false);
	   

	    ArcheusUser savedUser = userRepository.save(user);
	    System.out.println("Uložený používateľ: " + savedUser);

	    verificationTokenRepository.deleteByUser(user);

	    String token = UUID.randomUUID().toString();

	    VerificationToken verificationToken = VerificationToken.builder()
	        .token(token)
	        .user(savedUser)
	        .expiryDate(Instant.now().plus(24, ChronoUnit.HOURS))
	        .build();

	    verificationTokenRepository.save(verificationToken);
	    emailService.sendVerificationEmail(savedUser.getEmail(), token);

	    System.out.println("Overovací odkaz: http://localhost:8085/api/users/verify?token=" + token);

	    return ResponseEntity.ok(savedUser);
	}

    // GET: vráti všetkých používateľov
    @GetMapping
    public List<ArcheusUser> getAllUsers() {
        return userRepository.findAll();
    }
    
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody ArcheusUser loginUser) {
        try {
            // ✅ oficiálny Spring Security spôsob
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginUser.getEmail(),
                            loginUser.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            String token = JwtService.generateToken(loginUser.getEmail());
            String refreshToken = JwtService.generateRefreshToken(loginUser.getEmail());

            Map<String, String> tokens = new HashMap<>();
            tokens.put("accessToken", token);
            tokens.put("refreshToken", refreshToken);

            return ResponseEntity.ok(tokens);

        } catch (AuthenticationException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Neplatné prihlasovacie údaje"));
        }
    }
    @GetMapping("/me")
    public ResponseEntity<ArcheusUser> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        ArcheusUser user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            return ResponseEntity.status(404).build();
        }

        return ResponseEntity.ok(user);
    }

    
    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestBody PasswordChangeRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        ArcheusUser user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            return ResponseEntity.status(404).body("Používateľ neexistuje.");
        }

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            return ResponseEntity.status(400).body("Staré heslo nesedí.");
        }

        if (request.getNewPassword().length() < 6) {
            return ResponseEntity.status(400).body("Nové heslo musí mať aspoň 6 znakov.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return ResponseEntity.ok("Heslo bolo úspešne zmenené.");
    }
    
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        ArcheusUser user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            return ResponseEntity.status(404).body("Používateľ neexistuje.");
        }

        userRepository.delete(user);

        return ResponseEntity.ok("Účet bol úspešne vymazaný.");
    }
    
    @PostMapping("/refreshToken")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
        String requestToken = request.getRefreshToken();

        RefreshToken storedToken = refreshTokenRepository.findByToken(requestToken).orElse(null);

        if (storedToken == null || storedToken.getExpiryDate().isBefore(Instant.now())) {
            return ResponseEntity.status(401).body(Map.of("error", "Neplatný alebo expirovaný refresh token"));
        }

        // Získame email z tokenu
        String email = JwtService.extractEmail(requestToken);

        // Vygenerujeme nový access token
        String newAccessToken = JwtService.generateToken(email);

        // Vymažeme starý refresh token
        refreshTokenRepository.delete(storedToken);

        // Vygenerujeme nový refresh token
        String newRefreshTokenString = JwtService.generateRefreshToken(email);
        Instant newExpiryDate = Instant.now().plus(7, ChronoUnit.DAYS);

        RefreshToken newRefreshToken = new RefreshToken();
        newRefreshToken.setToken(newRefreshTokenString);
        newRefreshToken.setExpiryDate(newExpiryDate);
        ArcheusUser user = userRepository.findByEmail(email).orElseThrow();
        newRefreshToken.setUser(user);

        refreshTokenRepository.save(newRefreshToken);

        Map<String, String> response = new HashMap<>();
        response.put("accessToken", newAccessToken);
        response.put("refreshToken", newRefreshTokenString);

        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/map")
    public ResponseEntity<Map<Long, String>> getUserIdNameMap() {
        List<ArcheusUser> users = userRepository.findAll();
        Map<Long, String> map = new HashMap<>();
        users.forEach(u -> map.put(u.getId(), u.getFullName()));
        return ResponseEntity.ok(map);
    }
    
    @PutMapping("/messages/{id}")
    public ResponseEntity<Messages> updateMessage(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        String newContent = body.get("content");
        Messages updated = messageService.updateMessage(id, newContent);
        return ResponseEntity.ok(updated); 
    }
    
    @GetMapping("/verify")
    public ResponseEntity<String> verifyUser(@RequestParam String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token).orElse(null);

        if (verificationToken == null || verificationToken.getExpiryDate().isBefore(Instant.now())) {
            return ResponseEntity.status(400).body("Token je neplatný alebo expirovaný.");
        }

        ArcheusUser user = verificationToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);
        verificationTokenRepository.delete(verificationToken);

        return ResponseEntity.ok("Účet bol úspešne overený.");
    }
    
    @PostMapping(value = "/api/posts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Post> createPost(
            @RequestParam("text") String text,
            @RequestParam(value = "image", required = false) MultipartFile imageFile) {

        String imageUrl = null;

        if (imageFile != null && !imageFile.isEmpty()) {
            String filename = UUID.randomUUID() + "_" + StringUtils.cleanPath(imageFile.getOriginalFilename());
            Path uploadPath = Paths.get("uploads");

            try {
                Files.createDirectories(uploadPath); // vytvorí priečinok, ak neexistuje
                Path filePath = uploadPath.resolve(filename);
                Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                imageUrl = "/uploads/" + filename;
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }

        Post post = new Post();
        post.setText(text);
        post.setImageUrl(imageUrl);
        post.setCreatedAt(LocalDateTime.now());

        Post savedPost = postRepository.save(post);
        return ResponseEntity.ok(savedPost);
    }
    
    @GetMapping("/test-token")
    public ResponseEntity<String> testToken() {
        // 1. Nájde testovacieho používateľa podľa ID (zmeň na existujúce ID z DB)
        ArcheusUser user = userRepository.findById(1L)
            .orElseThrow(() -> new RuntimeException("Používateľ s ID 1 neexistuje"));

        // 2. Vygeneruje token
        String token = UUID.randomUUID().toString();

        // 3. Vytvorí a uloží VerificationToken
        VerificationToken vt = VerificationToken.builder()
            .token(token)
            .user(user)
            .expiryDate(Instant.now().plus(1, ChronoUnit.DAYS))
            .build();


        verificationTokenRepository.save(vt);

        // 4. Vráti odpoveď
        return ResponseEntity.ok("Token pre používateľa " + user.getEmail() + " bol uložený: " + token);
    }
    
    @PutMapping("/api/user")
    public ResponseEntity<String> updateUser(@RequestBody ArcheusUser updatedUser) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        ArcheusUser user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body("Používateľ nenájdený.");
        }

        user.setNickname(updatedUser.getNickname());
        user.setAltEmail(updatedUser.getAltEmail());
        user.setBio(updatedUser.getBio());
        userRepository.save(user);

        return ResponseEntity.ok("Údaje uložené.");
    }
    
    @Transactional
    @GetMapping("/test-email")
    public ResponseEntity<String> sendTestEmail(@RequestParam("to") String email) {
        ArcheusUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Používateľ s týmto e-mailom neexistuje"));
        
        verificationTokenRepository.deleteByUser(user);

        String token = UUID.randomUUID().toString();

        VerificationToken vt = VerificationToken.builder()
                .token(token)
                .user(user)
                .expiryDate(Instant.now().plus(1, ChronoUnit.DAYS))
                .build();

        verificationTokenRepository.save(vt);
        emailService.sendVerificationEmail(email, token);

        return ResponseEntity.ok("Testovací verifikačný e-mail odoslaný na: " + email);
    }
    
    @PostConstruct
    public void testRepo() {
        System.out.println(">>> UserRepository načítaný: " + (userRepository != null));
    }
    
    @GetMapping("/api/protected")
    public ResponseEntity<String> protectedData() {
        return ResponseEntity.ok("Toto je chránený jupii obsah");
    }
    

    
}
