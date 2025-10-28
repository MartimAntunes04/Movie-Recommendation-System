package pt.uc.movierecommendation.movierecommendationsystem.Controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import pt.uc.movierecommendation.movierecommendationsystem.Model.LoginRequest;
import pt.uc.movierecommendation.movierecommendationsystem.Model.SignUpRequest;
import pt.uc.movierecommendation.movierecommendationsystem.Service.AuthService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;



@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;


    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        objectMapper = new ObjectMapper();
    }

    // ==================== TESTES LOGIN ====================

    @Test
    void loginValidoDeveRetornarToken() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("teste@exemplo.com");
        request.setPassword("123");

        when(authService.login(request.getEmail(), request.getPassword()))
                .thenReturn("token-gerado");

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.token").value("token-gerado"));
    }

    @Test
    void loginInvalidoDeveRetornarUnauthorized() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("teste@exemplo.com");
        request.setPassword("senhaErrada");

        when(authService.login(request.getEmail(), request.getPassword()))
                .thenReturn(null);

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    // ==================== TESTES SIGNUP ====================

    @Test
    void signupValidoDeveCriarConta() throws Exception {
        SignUpRequest request = new SignUpRequest();
        request.setEmail("novo@exemplo.com");
        request.setUsername("novoUser");
        request.setPassword("123");
        request.setFirstName("Novo");
        request.setLastName("User");

        when(authService.signup(any(SignUpRequest.class))).thenReturn(true);

        mockMvc.perform(post("/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Conta criada com sucesso!"));
    }

    @Test
    void signupComEmailOuUsernameExistenteDeveRetornarBadRequest() throws Exception {
        SignUpRequest request = new SignUpRequest();
        request.setEmail("existente@exemplo.com");
        request.setUsername("userExistente");
        request.setPassword("123");
        request.setFirstName("Teste");
        request.setLastName("User");

        when(authService.signup(any(SignUpRequest.class))).thenReturn(false);

        mockMvc.perform(post("/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Email ou username já em uso!"));
    }
}
