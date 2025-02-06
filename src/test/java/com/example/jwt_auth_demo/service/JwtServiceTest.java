package com.example.jwt_auth_demo.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    @Mock
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtService.jwtSecret = "5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437";
        jwtService.jwtExpiration = 3600000;
        jwtService.jwtIssuer = "test-issuer";
        jwtService.adminRole = "ROLE_ADMIN";
        jwtService.userRole = "ROLE_USER";
    }

    @Test
    void generateToken_ShouldGenerateTokenForAdminUser() {
        when(userDetails.getUsername()).thenReturn("admin");

        String token = jwtService.generateToken(userDetails);

        assertNotNull(token);
        List<String> roles = jwtService.extractRoles(token);
        assertTrue(roles.contains("ROLE_ADMIN"));
    }

    @Test
    void generateToken_ShouldGenerateTokenForRegularUser() {
        // Mock UserDetails to simulate a regular user
        when(userDetails.getUsername()).thenReturn("user");

        String token = jwtService.generateToken(userDetails);

        assertNotNull(token);
        List<String> roles = jwtService.extractRoles(token);
        assertTrue(roles.contains("ROLE_USER"));
    }

    @Test
    void extractUsername_ShouldReturnCorrectUsername() {
        String token = jwtService.generateToken(userDetails);

        String username = jwtService.extractUsername(token);

        assertEquals(null, username);
    }

    @Test
    void extractExpiration_ShouldReturnCorrectExpirationDate() {
        String token = jwtService.generateToken(userDetails);

        Date expiration = jwtService.extractExpiration(token);

        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    void validateToken_ShouldReturnTrueForValidToken() {
        when(userDetails.getUsername()).thenReturn("user");
        String token = jwtService.generateToken(userDetails);

        boolean isValid = jwtService.validateToken(token, userDetails);

        assertTrue(isValid);
    }

    @Test
    void validateToken_ShouldReturnFalseForInvalidToken() {
        // Given a token
        when(userDetails.getUsername()).thenReturn("user");
        String token = jwtService.generateToken(userDetails);

        UserDetails invalidUser = User.builder().username("invalidUser").password("password").build();

        boolean isValid = jwtService.validateToken(token, invalidUser);

        assertFalse(isValid);
    }


    @Test
    void extractRoles_ShouldReturnCorrectRoles() {
        when(userDetails.getUsername()).thenReturn("user");
        String token = jwtService.generateToken(userDetails);

        List<String> roles = jwtService.extractRoles(token);

        assertTrue(roles.contains("ROLE_USER"));
    }
}
