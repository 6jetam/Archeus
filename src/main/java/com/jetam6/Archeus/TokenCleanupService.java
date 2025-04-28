package com.jetam6.Archeus;

import java.time.Instant;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class TokenCleanupService {
	private final RefreshTokenRepository refreshTokenRepository;

    public TokenCleanupService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    // Spustí sa automaticky každú hodinu
    @Scheduled(fixedRate = 3600000) // každých 3600 sekúnd (1 hodina)
    public void cleanExpiredRefreshTokens() {
        Instant now = Instant.now();
        refreshTokenRepository.findAll().stream()
            .filter(token -> token.getExpiryDate().isBefore(now))
            .forEach(refreshTokenRepository::delete);
    }
}
