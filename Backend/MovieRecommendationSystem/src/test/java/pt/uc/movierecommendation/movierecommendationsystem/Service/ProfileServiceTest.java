package pt.uc.movierecommendation.movierecommendationsystem.Service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import pt.uc.movierecommendation.movierecommendationsystem.Model.User;
import pt.uc.movierecommendation.movierecommendationsystem.Repository.UserRepository;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ProfileServiceTest {

    private UserRepository userRepository;
    private PasswordEncoder encoder;
    private ProfileService profileService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        this.encoder = mock(PasswordEncoder.class);
        profileService = new ProfileService(userRepository, encoder);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(String email) {
        var auth = new UsernamePasswordAuthenticationToken(email, null, java.util.List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    // ==================== TESTES getProfile ====================

    @Test
    void getProfile_shouldReturn401_whenNotAuthenticated() {
        SecurityContextHolder.clearContext();

        ResponseEntity<?> response = profileService.getProfile();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        Map<?,?> body = (Map<?,?>) response.getBody();
        assertThat(body.get("error")).isEqualTo("Unauthorized");
    }


    @Test
    void getProfile_shouldReturn404_whenUserNotFound() {
        String email = "naoexiste@gmail.com";
        authenticateAs(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        ResponseEntity<?> response = profileService.getProfile();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        Map<?,?> body = (Map<?,?>) response.getBody();
        assertThat(body.get("error")).isIn("User not found", "Utilizador não encontrado");
    }

    @Test
    void getProfile_shouldReturn200_withUserData() {
        String email = "testeexemplo@gmail.com";
        authenticateAs(email);

        User user = new User();
        user.setEmail(email);
        user.setUsername("user");
        user.setFirstName("Teste");
        user.setLastName("User");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        ResponseEntity<?> response = profileService.getProfile();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<?,?> body = (Map<?,?>) response.getBody();
        assertThat(body.get("email")).isEqualTo(email);
        assertThat(body.get("username")).isEqualTo("user");
        assertThat(body.get("firstName")).isEqualTo("Teste");
        assertThat(body.get("lastName")).isEqualTo("User");
        @SuppressWarnings("unchecked")
        Map<String, Object> safe = (Map<String, Object>) response.getBody();
        assertThat(safe).doesNotContainKey("password");
    }

    // ==================== TESTES updateProfile ====================

    @Test
    void updateProfile_shouldReturnFalse_whenNotAuthenticated() {
        SecurityContextHolder.clearContext();

        boolean result = profileService.updateProfile("novo@exemplo.com", null, null, null, null);

        assertThat(result).isFalse();
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateProfile_shouldReturnFalse_whenUserNotFound() {
        String email = "nao@existe.com";
        authenticateAs(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        boolean result = profileService.updateProfile("novo@exemplo.com", null, null, null, null);

        assertThat(result).isFalse();
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateProfile_shouldUpdateFields_andHashPassword() {
        String email = "teste@exemplo.com";
        authenticateAs(email);

        User user = new User();
        user.setEmail(email);
        user.setUsername("user");
        user.setFirstName("Teste");
        user.setLastName("User");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(encoder.encode("novaSenha")).thenReturn("HASHED");

        boolean result = profileService.updateProfile(
                "novo@exemplo.com",
                "novoUser",
                "novaSenha",
                "NovoNome",
                "NovoSobrenome"
        );

        assertThat(result).isTrue();
        verify(encoder).encode("novaSenha");
        verify(userRepository).save(any(User.class));

        assertThat(user.getEmail()).isEqualTo("novo@exemplo.com");
        assertThat(user.getUsername()).isEqualTo("novoUser");
        assertThat(user.getPassword()).isEqualTo("HASHED");
        assertThat(user.getFirstName()).isEqualTo("NovoNome");
        assertThat(user.getLastName()).isEqualTo("NovoSobrenome");
    }
}
