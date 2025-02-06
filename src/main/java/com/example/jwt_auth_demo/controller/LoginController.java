package com.example.jwt_auth_demo.controller;

import com.example.jwt_auth_demo.entity.AuthReponse;
import com.example.jwt_auth_demo.entity.AuthRequest;
import com.example.jwt_auth_demo.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "User Authentication", description = "Operations related to JWT Authentication")
@RestController
@RequestMapping("/auth")
public class LoginController {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;


    /**
     * Public Welcome API.
     * <p>
     * This endpoint is accessible without authentication and provides
     * a welcome message to users.
     *
     * @return ResponseEntity containing a welcome message.
     */
    @Operation(
            summary = "Public Welcome API",
            description = "Returns a welcome message. This endpoint does not require authentication.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully returns the welcome message")
            }
    )
    @GetMapping("/welcome")
    public String welcome() {
        return "Welcome this endpoint is not secure";
    }

    /**
     * Authenticate and Generate JWT Token.
     * This endpoint verifies user credentials and generates a JWT token upon successful authentication.
     *
     * @param authRequest The authentication request containing username and password.
     * @return A JWT token if authentication is successful.
     */
    @Operation(
            summary = "Authenticate and Generate JWT Token",
            description = "Validates user credentials and returns a JWT token if authentication is successful.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully generated JWT token"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid credentials")
            })
    @PostMapping("/login")
    public ResponseEntity<?> getToken(@RequestBody AuthRequest authRequest) { // Return ResponseEntity<?>
        log.info("Inside Login controller generate token");

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
            );

            if (authentication.isAuthenticated()) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                String token = jwtService.generateToken(userDetails);
                AuthReponse authReponse = new AuthReponse();
                authReponse.setToken(token);
                return ResponseEntity.ok(authReponse); // Return the token in the response body
            } else {
                throw new UsernameNotFoundException("Invalid user request!");
            }
        } catch (AuthenticationException e) {
            log.error("Authentication failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication failed: " + e.getMessage());
        } catch (Exception e) {
            log.error("An error occurred: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred");
        }
    }

    /**
     * JWT-Protected Test Endpoint.
     * <p>
     * This endpoint is accessible only with a valid JWT token.
     * It can be used to verify if the provided token is valid and the user is authenticated.
     *
     * @return ResponseEntity with a success message if the request is authenticated.
     */
    @Operation(
            summary = "Get Info",
            description = "Checks if the provided JWT token is valid. Accessible only for authenticated users.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "JWT is valid, access granted"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token")
            }
    )
    @GetMapping("/getInfo")
    public ResponseEntity<String> getInfo() {
        return ResponseEntity.ok("Valid JWT Token");
    }


}
