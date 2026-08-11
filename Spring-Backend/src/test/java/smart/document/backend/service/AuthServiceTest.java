package smart.document.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import smart.document.backend.dto.SignupRequest;
import smart.document.backend.repository.UserRepository;
import smart.document.backend.security.JwtService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    private final JwtService jwtService = new JwtService();

    @Test
    void signupRejectsDuplicateEmail() {
        AuthService authService = new AuthService(
                userRepository, passwordEncoder, authenticationManager, jwtService);

        SignupRequest request = new SignupRequest();
        request.setName("Jane");
        request.setEmail("jane@example.com");
        request.setPassword("password123");

        when(userRepository.existsByEmail("jane@example.com")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> authService.signup(request));
        verify(userRepository, never()).save(any());
    }
}
