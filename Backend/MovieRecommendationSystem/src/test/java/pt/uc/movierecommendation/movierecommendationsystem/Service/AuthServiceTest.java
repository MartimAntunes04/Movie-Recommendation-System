package pt.uc.movierecommendation.movierecommendationsystem.Service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.uc.movierecommendation.movierecommendationsystem.Model.SignUpRequest;
import pt.uc.movierecommendation.movierecommendationsystem.Model.User;
import pt.uc.movierecommendation.movierecommendationsystem.Repository.UserRepository;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

public class AuthServiceTest {

    private UserRepository userRepository;
    private JwtService jwtService;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        jwtService = mock(JwtService.class);
        authService = new AuthService(userRepository, jwtService);
    }

    // --- LOGIN TESTES ---

    @Test
    void loginCorreto() {
        String email = "test@exemplo.com";
        String password = "123";

        User user = new User();
        user.setEmail(email);
        user.setPassword(password);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(jwtService.generateToken(email)).thenReturn("token-gerado");

        String token = authService.login(email, password);

        assertThat(token).isEqualTo("token-gerado");
        verify(userRepository).findByEmail(email);
        verify(jwtService).generateToken(email);
    }

    @Test
    void passwordErrada(){
        String email = "test@exemplo.com";

        User user = new User();
        user.setEmail(email);
        user.setPassword("123");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        String token = authService.login(email, "1234");

        assertThat(token).isNull();  //devera retornar null
        verify(jwtService, never()).generateToken(anyString());
    }

    @Test
    void emailErrado(){
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        String token = authService.login("naoexiste@gmail.com", "123");

        assertThat(token).isNull();
    }

    // --- SIGNUP TESTES ---

    @Test
    void emaileEusernameunicos(){
        SignUpRequest request = new SignUpRequest();
        request.setEmail("novo@exemplo.com");
        request.setUsername("novoUser");
        request.setPassword("password");
        request.setFirstName("Novo");
        request.setLastName("User");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);

        boolean resultado = authService.signup(request);

        assertThat(resultado).isTrue();
        verify(userRepository).save(any(User.class));
    }

    @Test
    void deveFalharQuandoEmailOuUsernameJaExistem() {
        SignUpRequest request = new SignUpRequest();
        request.setEmail("jaexiste@exemplo.com");
        request.setUsername("userExistente");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);

        boolean resultado = authService.signup(request);

        assertThat(resultado).isFalse();
        verify(userRepository, never()).save(any(User.class));
    }
}
