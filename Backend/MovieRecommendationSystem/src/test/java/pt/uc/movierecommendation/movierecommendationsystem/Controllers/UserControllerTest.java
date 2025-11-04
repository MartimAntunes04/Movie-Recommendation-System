package pt.uc.movierecommendation.movierecommendationsystem.Controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import pt.uc.movierecommendation.movierecommendationsystem.Model.User;
import pt.uc.movierecommendation.movierecommendationsystem.Service.ProfileService;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


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

//     @Test
//     @SuppressWarnings("unchecked")
//     void getProfile_ok_returnsUser() throws Exception {
//         Map<String, Object> mockResponse = Map.of(
//                 "email","test@exemplo.com",
//                 "username","user",
//                 "firstName","Test",
//                 "lastName","User"
//         );

//         ResponseEntity<Map<String,Object>> respOk = ResponseEntity.ok(mockResponse);
//         when(profileService.getProfile()).thenReturn((ResponseEntity<?>) respOk);

//         mockMvc.perform(get("/profile"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.email").value("test@exemplo.com"))
//                .andExpect(jsonPath("$.username").value("user"))
//                .andExpect(jsonPath("$.firstName").value("Test"))
//                .andExpect(jsonPath("$.lastName").value("User"));
//     }

//     @Test
//     @SuppressWarnings("unchecked")
//     void getProfile_unauthorized_returns401() throws Exception {
//         ResponseEntity<Map<String,String>> resp401 =
//                 ResponseEntity.status(401).body(Map.of("error","Unauthorized"));

//         when(profileService.getProfile()).thenReturn((ResponseEntity<?>) resp401);

//         mockMvc.perform(get("/profile"))
//                .andExpect(status().isUnauthorized())
//                .andExpect(jsonPath("$.error").value("Unauthorized"));
//     }


    // =================== POST /profile/update ===================
    @Test
    void updateProfileValidoDeveRetornarSuccessTrue() throws Exception {
        User user = new User();
        user.setEmail("novo@exemplo.com");
        user.setUsername("novoUser");
        user.setPassword("123");
        user.setFirstName("Novo");
        user.setLastName("User");

        when(profileService.updateProfile(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(true);

        mockMvc.perform(post("/profile/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void updateProfileFalhaDeveRetornarSuccessFalse() throws Exception {
        User user = new User();
        user.setEmail("novo@exemplo.com");

        when(profileService.updateProfile(
                eq("novo@exemplo.com"),
                isNull(String.class),
                isNull(String.class),
                isNull(String.class),
                isNull(String.class)))
            .thenReturn(false);

        mockMvc.perform(post("/profile/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
