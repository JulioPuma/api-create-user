package com.evaluation.project.service.impl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtService {

    private final Algorithm alg;
    private final long ttlMinutes;

    public JwtService(
            @Value("${app.jwt.secret}") String secretPlain, // puede ser texto plano
            @Value("${app.jwt.ttlMinutes:60}") long ttlMinutes) {
        this.alg = Algorithm.HMAC256(secretPlain);
        this.ttlMinutes = ttlMinutes;
    }

    public String generate(UUID userId) {
        Instant now = Instant.now();
        return JWT.create()
                .withSubject(userId.toString())
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(now.plusSeconds(ttlMinutes * 60)))
                .sign(alg);
    }
}