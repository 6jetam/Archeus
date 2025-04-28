package com.jetam6.Archeus;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;
	@Autowired
	private PasswordEncoder passwordEncoder;
	@Autowired
	private RefreshTokenRepository refreshTokenRepository;

    // POST: registrácia používateľa
    @PostMapping("/register")
    public ResponseEntity<ArcheusUser> registerUser(@RequestBody ArcheusUser user) {
        System.out.println("Prijatý používateľ: " + user); // ← LOG
        
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        ArcheusUser savedUser = userRepository.save(user);
        System.out.println("Uložený používateľ: " + savedUser); // ← LOG
        return ResponseEntity.ok(savedUser);
    }
    // GET: vráti všetkých používateľov
    @GetMapping
    public List<ArcheusUser> getAllUsers() {
        return userRepository.findAll();
    }
    
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody ArcheusUser loginUser) {
        ArcheusUser user = userRepository.findByEmail(loginUser.getEmail()).orElse(null);

        if (user != null && passwordEncoder.matches(loginUser.getPassword(), user.getPassword())) {
        	String token = JwtService.generateToken(user.getEmail());
        	String refreshToken = JwtService.generateRefreshToken(user.getEmail());
        	
        	Map<String, String> tokens = new HashMap<>();
            tokens.put("accessToken", token);
            tokens.put("refreshToken", refreshToken);
            
            return ResponseEntity.ok(tokens);
            
        } else {
            return ResponseEntity.status(401).body(Map.of("error", "Neplatné prihlasovacie údaje"));
        }
    }
    
    @GetMapping("/me")
    public ResponseEntity<String> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName(); // získame meno (email)
        return ResponseEntity.ok("Aktuálne prihlásený používateľ: " + email);
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

    
}
