//package nmtt.demo.controller;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.RequiredArgsConstructor;
//import nmtt.demo.dto.request.Account.AuthenticationRequest;
//import nmtt.demo.dto.request.Account.IntrospectRequest;
//import nmtt.demo.dto.request.Account.LogoutRequest;
//import nmtt.demo.dto.request.Account.RefreshRequest;
//import nmtt.demo.dto.response.Account.AuthenticationResponse;
//import nmtt.demo.dto.response.Account.IntrospectResponse;
//import nmtt.demo.service.authentication.AuthenticationService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mockito;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;
//import org.springframework.security.core.AuthenticationException;
//import org.springframework.test.context.TestPropertySource;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
//import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
//
//@SpringBootTest
//@AutoConfigureMockMvc
//@RequiredArgsConstructor
//@TestPropertySource("/test.properties")
//public class AuthenticationControllerTest {
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockBean
//    private AuthenticationService authenticationService;
//
//    private ObjectMapper objectMapper;
//
//    private AuthenticationRequest authRequest;
//    private AuthenticationResponse authResponse;
//    private RefreshRequest refreshRequest;
//    private LogoutRequest logoutRequest;
//    private IntrospectRequest introspectRequest;
//    private IntrospectResponse introspectResponse;
//
//    @BeforeEach
//    void setUp() {
//        objectMapper = new ObjectMapper();
//
//        authRequest = new AuthenticationRequest("john@gmail.com", "password123");
//        authResponse = new AuthenticationResponse("access-token", true);
//
//        refreshRequest = new RefreshRequest("refresh-token");
//        logoutRequest = new LogoutRequest("refresh-token");
//
//        introspectRequest = new IntrospectRequest("access-token");
//        introspectResponse = new IntrospectResponse(true);
//    }
//
//    @Test
//    void authenticate_success() throws Exception {
//        Mockito.when(authenticationService.authenticate(Mockito.any())).thenReturn(authResponse);
//
//        mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(authRequest)))
//                .andExpect(MockMvcResultMatchers.status().isOk())
//                .andExpect(MockMvcResultMatchers.jsonPath("$.token").value("access-token"))
//                .andExpect(MockMvcResultMatchers.jsonPath("$.authenticated").value(true));
//    }
//
//    @Test
//    void authenticate_failure() throws Exception {
//        Mockito.when(authenticationService.authenticate(Mockito.any())).thenThrow(new AuthenticationException("Invalid credentials") {});
//
//        mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(authRequest)))
//                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
//    }
//
//    @Test
//    void introspect_success() throws Exception {
//        Mockito.when(authenticationService.introspect(Mockito.any())).thenReturn(introspectResponse);
//
//        mockMvc.perform(MockMvcRequestBuilders.post("/auth/introspect")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(introspectRequest)))
//                .andExpect(MockMvcResultMatchers.status().isOk())
//                .andExpect(MockMvcResultMatchers.jsonPath("$.valid").value(true));
//    }
//
//    @Test
//    void refreshToken_success() throws Exception {
//        Mockito.when(authenticationService.refreshToken(Mockito.any())).thenReturn(authResponse);
//
//        mockMvc.perform(MockMvcRequestBuilders.post("/auth/refresh")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(refreshRequest)))
//                .andExpect(MockMvcResultMatchers.status().isOk())
//                .andExpect(MockMvcResultMatchers.jsonPath("$.token").value("access-token"))
//                .andExpect(MockMvcResultMatchers.jsonPath("$.authenticated").value(true));
//    }
//
//    @Test
//    void logout_success() throws Exception {
//        Mockito.doNothing().when(authenticationService).logout(Mockito.any());
//
//        mockMvc.perform(MockMvcRequestBuilders.post("/auth/logout")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(logoutRequest)))
//                .andExpect(MockMvcResultMatchers.status().isNoContent());
//    }
//
//    @Test
//    void activeAccount_success() throws Exception {
//        Mockito.doNothing().when(authenticationService).activeAccount(Mockito.anyString());
//
//        mockMvc.perform(MockMvcRequestBuilders.get("/auth/12345"))
//                .andExpect(MockMvcResultMatchers.status().isOk())
//                .andExpect(MockMvcResultMatchers.content().string("Active account successfully"));
//    }
//}
