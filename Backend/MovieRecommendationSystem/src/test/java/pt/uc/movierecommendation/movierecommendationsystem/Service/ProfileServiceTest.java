package pt.uc.movierecommendation.movierecommendationsystem.Service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import pt.uc.movierecommendation.movierecommendationsystem.Model.User;
import pt.uc.movierecommendation.movierecommendationsystem.Repository.UserRepository;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

public class ProfileServiceTest {

    private UserRepository userRepository;
    private JwtService jwtService;
    private ProfileService profileService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        jwtService = mock(JwtService.class);
        profileService = new ProfileService(userRepository, jwtService);
    }

    // ==================== TESTES getProfile ====================

    @Test
    void deveRetornarUnauthorizedSeTokenInvalido() {
        String tokenHeader = "Bearer token-invalido";

        when(jwtService.extractEmail("token-invalido")).thenReturn("email@exemplo.com");
        when(jwtService.isTokenValid("token-invalido", "email@exemplo.com")).thenReturn(false);

        ResponseEntity<?> response = profileService.getProfile(tokenHeader);

        assertThat(response.getStatusCodeValue()).isEqualTo(401);
        assertThat(response.getBody()).isEqualTo(Map.of("error", "Token inválido ou expirado"));
    }


    @Test
    void deveRetornarNotFoundSeUserNaoExistir() {
        String tokenHeader = "Bearer token-valido";

        when(jwtService.extractEmail("token-valido")).thenReturn("naoexiste@gmail.com");
        when(jwtService.isTokenValid("token-valido", "naoexiste@gmail.com")).thenReturn(true);
        when(userRepository.findByEmail("naoexiste@gmail.com")).thenReturn(Optional.empty());

        ResponseEntity<?> response = profileService.getProfile(tokenHeader);

        assertThat(response.getStatusCodeValue()).isEqualTo(404);
        assertThat(response.getBody()).isEqualTo(Map.of("error", "Utilizador não encontrado"));
    }

    @Test
    void deveRetornarPerfilSeUserExistirETokenValido() {
        String tokenHeader = "Bearer token-valido";

        User user = new User();
        user.setEmail("testeexemplo@gmail.com");
        user.setUsername("user");
        user.setPassword("password");
        user.setFirstName("Teste");
        user.setLastName("User");

        when(jwtService.extractEmail("token-valido")).thenReturn("testeexemplo@gmail.com");
        when(jwtService.isTokenValid("token-valido", "testeexemplo@gmail.com")).thenReturn(true);
        when(userRepository.findByEmail("testeexemplo@gmail.com")).thenReturn(Optional.of(user));

        ResponseEntity<?> response = profileService.getProfile(tokenHeader);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(Map.of(
                "email", "testeexemplo@gmail.com",
                "username", "user",
                "password", "password",
                "firstName", "Teste",
                "lastName", "User"
        ));
    }

    // ==================== TESTES updateProfile ====================

    @Test
    void updateProfileDeveFalharSeTokenInvalido() {
        String tokenHeader = "Bearer token-invalido";

        when(jwtService.extractEmail("token-invalido")).thenReturn("email@exemplo.com");
        when(jwtService.isTokenValid("token-invalido", "email@exemplo.com")).thenReturn(false);

        boolean resultado = profileService.updateProfile(tokenHeader, "novo@exemplo.com", null, null, null, null);

        assertThat(resultado).isFalse();
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateProfileDeveFalharSeUserNaoExistir() {
        String tokenHeader = "Bearer token-valido";

        when(jwtService.extractEmail("token-valido")).thenReturn("nao@existe.com");
        when(jwtService.isTokenValid("token-valido", "nao@existe.com")).thenReturn(true);
        when(userRepository.findByEmail("nao@existe.com")).thenReturn(Optional.empty());

        boolean resultado = profileService.updateProfile(tokenHeader, "novo@exemplo.com", null, null, null, null);

        assertThat(resultado).isFalse();
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateProfileDeveAtualizarCamposSeUserExistir() {
        String tokenHeader = "Bearer token-valido";

        User user = new User();
        user.setEmail("teste@exemplo.com");
        user.setUsername("user");
        user.setPassword("password");
        user.setFirstName("Teste");
        user.setLastName("User");

        when(jwtService.extractEmail("token-valido")).thenReturn("teste@exemplo.com");
        when(jwtService.isTokenValid("token-valido", "teste@exemplo.com")).thenReturn(true);
        when(userRepository.findByEmail("teste@exemplo.com")).thenReturn(Optional.of(user));

        boolean resultado = profileService.updateProfile(
                tokenHeader,
                "novo@exemplo.com",
                "novoUser",
                "novaSenha",
                "NovoNome",
                "NovoSobrenome"
        );

        assertThat(resultado).isTrue();
        verify(userRepository).save(user);

        assertThat(user.getEmail()).isEqualTo("novo@exemplo.com");
        assertThat(user.getUsername()).isEqualTo("novoUser");
        assertThat(user.getPassword()).isEqualTo("novaSenha");
        assertThat(user.getFirstName()).isEqualTo("NovoNome");
        assertThat(user.getLastName()).isEqualTo("NovoSobrenome");
    }
}
