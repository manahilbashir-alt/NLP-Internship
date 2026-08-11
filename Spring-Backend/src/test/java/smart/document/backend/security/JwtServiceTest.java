package smart.document.backend.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private final JwtService jwtService = new JwtService();

    private UserDetails testUser() {
        return User.withUsername("test@example.com")
                .password("irrelevant")
                .roles("USER")
                .build();
    }

    @Test
    void generatesTokenAndExtractsCorrectUsername() {
        UserDetails user = testUser();

        String token = jwtService.generateToken(user);

        assertNotNull(token);
        assertEquals("test@example.com", jwtService.extractUsername(token));
    }

    @Test
    void tokenIsValidForMatchingUser() {
        UserDetails user = testUser();

        String token = jwtService.generateToken(user);

        assertTrue(jwtService.isTokenValid(token, user));
    }

    @Test
    void tokenIsInvalidForDifferentUser() {
        UserDetails user = testUser();
        UserDetails otherUser = User.withUsername("other@example.com")
                .password("irrelevant")
                .roles("USER")
                .build();

        String token = jwtService.generateToken(user);

        assertFalse(jwtService.isTokenValid(token, otherUser));
    }
}
