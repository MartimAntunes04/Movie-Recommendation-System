package pt.uc.movierecommendation.movierecommendationsystem.Controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import pt.uc.movierecommendation.movierecommendationsystem.Model.User;
import pt.uc.movierecommendation.movierecommendationsystem.Service.ProfileService;
import static org.mockito.ArgumentMatchers.anyString;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private ProfileService profileService;

    @InjectMocks
    private UserControler userController;


    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        objectMapper = new ObjectMapper();
    }

    // =================== GET /profile ===================

/*
    @Test
    void getProfileValidoDeveRetornarUser() throws Exception {
        String token = "Bearer tokenValido";

        Map<String, Object> mockResponse = Map.of(
                "email","teste@exemplo.com",
                "username","user",
                "password","123",
                "firstName","Teste",
                "lastName","User"
        );

        ResponseEntity<?> responseEntity = ResponseEntity.ok(mockResponse);

        when(profileService.getProfile(anyString()))
                .thenReturn(responseEntity);

        mockMvc.perform(get("/profile")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("teste@exemplo.com"))
                .andExpect(jsonPath("$.username").value("user"))
                .andExpect(jsonPath("$.firstName").value("Teste"))
                .andExpect(jsonPath("$.lastName").value("User"));
    }

    @Test
    void getProfileTokenInvalidoDeveRetornarUnauthorized() throws Exception {
        String token = "Bearer tokenInvalido";

        when(profileService.getProfile(token))
                .thenReturn(ResponseEntity.status(401).body(Map.of("error","Token inválido ou expirado")));

        mockMvc.perform(get("/profile")
                        .header("Authorization", token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Token inválido ou expirado"));
    }*/

    // =================== POST /profile/update ===================
    @Test
    void updateProfileValidoDeveRetornarSuccessTrue() throws Exception {
        String token = "Bearer tokenValido";
        User user = new User();
        user.setEmail("novo@exemplo.com");
        user.setUsername("novoUser");
        user.setPassword("123");
        user.setFirstName("Novo");
        user.setLastName("User");

        when(profileService.updateProfile(anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(true);

        mockMvc.perform(post("/profile/update")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void updateProfileFalhaDeveRetornarSuccessFalse() throws Exception {
        String token = "Bearer tokenValido";
        User user = new User();
        user.setEmail("novo@exemplo.com");

        when(profileService.updateProfile(
                anyString(),
                ArgumentMatchers.<String>any(),
                ArgumentMatchers.<String>any(),
                ArgumentMatchers.<String>any(),
                ArgumentMatchers.<String>any(),
                ArgumentMatchers.<String>any()))
                .thenReturn(false);

        mockMvc.perform(post("/profile/update")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));
    }
}
