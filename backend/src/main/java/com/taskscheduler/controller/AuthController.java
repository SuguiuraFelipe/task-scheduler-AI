package com.taskscheduler.controller;

import com.taskscheduler.dto.LoginRequest;
import com.taskscheduler.dto.LoginResponse;
import com.taskscheduler.dto.RegisterRequest;
import com.taskscheduler.dto.UserDTO;
import com.taskscheduler.entity.User;
import com.taskscheduler.repository.UserRepository;
import com.taskscheduler.security.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        Optional<User> user = userRepository.findByEmail(request.getEmail());

        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponse(null, null, null, "Usuário não encontrado", null));
        }

        User u = user.get();
        if (!passwordEncoder.matches(request.getPassword(), u.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponse(null, null, null, "Senha inválida", null));
        }

        String token = jwtUtil.generateToken(u.getEmail());
        log.info("User logged in: {}", u.getEmail());

        return ResponseEntity.ok(new LoginResponse(
                u.getId(),
                u.getEmail(),
                u.getName(),
                "Login bem-sucedido",
                token
        ));
    }

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        Optional<User> existing = userRepository.findByEmail(request.getEmail());

        if (existing.isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new LoginResponse(null, null, null, "Email já cadastrado", null));
        }

        User newUser = new User();
        newUser.setEmail(request.getEmail());
        newUser.setName(request.getName());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setCreatedAt(LocalDateTime.now());

        User saved = userRepository.save(newUser);
        String token = jwtUtil.generateToken(saved.getEmail());
        log.info("User registered: {}", saved.getEmail());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new LoginResponse(
                        saved.getId(),
                        saved.getEmail(),
                        saved.getName(),
                        "Usuário criado com sucesso",
                        token
                ));
    }

    @GetMapping("/me/{userId}")
    public ResponseEntity<UserDTO> getCurrentUser(@PathVariable Long userId) {
        return userRepository.findById(userId)
                .map(user -> ResponseEntity.ok(UserDTO.fromEntity(user)))
                .orElse(ResponseEntity.notFound().build());
    }
}
