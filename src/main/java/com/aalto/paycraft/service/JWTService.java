package com.aalto.paycraft.service;

import com.aalto.paycraft.entity.Employer;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.function.Function;

@Service
public class JWTService {

    // Fetch the secret string from the application properties file
    @Value("${secret-string}")
    private String SECRET_STRING;

    private SecretKey SECRET_KEY;

    // Token validity times: 1 hour for access tokens, 24 hours for refresh tokens
    private static final long ACCESS_TOKEN_VALIDITY_TIME = 3_600_000; // 1hr
    private static final long REFRESH_TOKEN_VALIDITY_TIME = 86_400_000; // 24hrs

    // Initialize the secret key once the secret string is loaded
    @PostConstruct
    public void init() {
        if (SECRET_STRING == null) {
            throw new IllegalStateException("SECRET_STRING is not configured");
        }
        // Decode the base64-encoded secret string into a byte array
        byte[] keyByte = Base64.getDecoder().decode(SECRET_STRING.getBytes(StandardCharsets.UTF_8));
        this.SECRET_KEY = new SecretKeySpec(keyByte, "HmacSHA256");
    }

    // Create a JWT token for an employer profile
    public String createJWT(Employer employer) {
        return generateToken(employer);
    }

    // Generate a JWT with claims from the Employer entity
    private String generateToken(Employer employer) {
        HashMap<String, Object> claims = new HashMap<>();
        claims.put("userID", employer.getEmployerId());
        claims.put("firstName", employer.getFirstName());
        claims.put("lastName", employer.getLastName());
        claims.put("email", employer.getEmailAddress());
        claims.put("phoneNumber", employer.getPhoneNumber());

        return Jwts.builder()
                .claims(claims)
                .subject(employer.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))  // Token issue time
                .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_VALIDITY_TIME))  // Token expiration
                .signWith(SECRET_KEY)  // Sign the token with the secret key
                .compact();  // Generate the compact JWT string
    }

    // Generate a refresh token with custom claims
    public String generateRefreshToken(HashMap<String, Object> claims, Employer employer) {
        return Jwts.builder()
                .claims(claims)
                .subject(employer.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))  // Issue time for refresh token
                .expiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_VALIDITY_TIME))  // Refresh token expiration
                .signWith(SECRET_KEY)  // Sign with secret key
                .compact();
    }

    // Extract claims from the JWT token using a provided function
    public <T> T extractClaims(String token, Function<Claims, T> claimsTFunction) {
        return claimsTFunction.apply(Jwts.parser().verifyWith(SECRET_KEY).build().parseSignedClaims(token).getPayload());
    }

    // Extract the username (subject) from the JWT token
    public String extractUsername(String token) {
        return extractClaims(token, Claims::getSubject);
    }

    // Validate token by checking the username and whether the token is expired
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    // Check if the token has expired
    public boolean isTokenExpired(String token) {
        return extractClaims(token, Claims::getExpiration).before(new Date());
    }
}