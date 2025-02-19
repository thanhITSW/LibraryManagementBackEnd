package nmtt.demo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import nmtt.demo.dto.request.Account.AccountCreationRequest;
import nmtt.demo.dto.request.Account.AccountUpdateRequest;
import nmtt.demo.dto.response.Account.AccountResponse;
import nmtt.demo.mapper.AccountMapper;
import nmtt.demo.service.account.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;
import java.util.List;

@SpringBootTest
@Slf4j
@AutoConfigureMockMvc
@TestPropertySource("/test.properties")
public class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    private AccountCreationRequest request;
    private AccountResponse accountResponse;

    private AccountUpdateRequest updateRequest;

    private ObjectMapper objectMapper;

    private LocalDate dob;

    @BeforeEach
    void initData(){
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        dob = LocalDate.of(1995, 1, 2);

        request = AccountCreationRequest.builder()
                .email("john@gmail.com")
                .firstName("John")
                .lastName("Doe")
                .password("123456")
                .dob(dob)
                .build();

        accountResponse = AccountResponse.builder()
                .id("1234567890")
                .email("john@gmail.com")
                .firstName("John")
                .lastName("Doe")
                .dob(dob)
                .build();

        updateRequest = AccountUpdateRequest.builder()
                .firstName("Johnny")
                .lastName("Doe")
                .dob(dob)
                .build();
    }

    @Test
    void createUser_validRequest_success() throws Exception {
        //GIVEN
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        String content = objectMapper.writeValueAsString(request);

        Mockito.when(accountService.createAccount(ArgumentMatchers.any()))
                        .thenReturn(accountResponse);

        //WHEN, THEN
        mockMvc.perform(MockMvcRequestBuilders
                .post("/accounts")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(content))
                .andExpect(MockMvcResultMatchers.status().isCreated());
    }

    @Test
    void createUser_emailInvalid_fail() throws Exception {
        // GIVEN
        request.setEmail("joh");
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        String content = objectMapper.writeValueAsString(request);

        // WHEN, THEN
        mockMvc.perform(MockMvcRequestBuilders.post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(1000))
                .andExpect(MockMvcResultMatchers.jsonPath("result").value("Email is invalid"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getAllUsers_success() throws Exception {
        List<AccountResponse> accounts = List.of(accountResponse);
        Mockito.when(accountService.getAccount()).thenReturn(accounts);

        mockMvc.perform(MockMvcRequestBuilders.get("/accounts")
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(accounts.size()));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getMyInfo_success() throws Exception {
        Mockito.when(accountService.getMyInfo()).thenReturn(accountResponse);

        mockMvc.perform(MockMvcRequestBuilders.get("/accounts/my-info"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value("john@gmail.com"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void updateAccountById_success() throws Exception {
        Mockito.when(accountService.updateAccountById(Mockito.anyString(), Mockito.any()))
                .thenReturn(accountResponse);

        mockMvc.perform(MockMvcRequestBuilders.put("/accounts/1234567890")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value("John"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void deleteAccountById_success() throws Exception {
        Mockito.doNothing().when(accountService).deleteUserById(Mockito.anyString());

        mockMvc.perform(MockMvcRequestBuilders.delete("/accounts/1234567890"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("Account has been deleted"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void resetPass_success() throws Exception {
        Mockito.doNothing().when(accountService).resetPass(Mockito.anyString());

        mockMvc.perform(MockMvcRequestBuilders.post("/accounts/reset-pass")
                        .param("email", "john@gmail.com"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("New password has been send your email"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void requestChangeMail_success() throws Exception {
        Mockito.doNothing().when(accountService).requestChangeMail(Mockito.anyString(), Mockito.anyString());

        mockMvc.perform(MockMvcRequestBuilders.post("/accounts/request-change-mail")
                        .param("accountId", "1234567890")
                        .param("newEmail", "newemail@gmail.com"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("Verification code has been sent to the new email"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void verifyChangeMail_success() throws Exception {
        Mockito.doNothing().when(accountService).verifyChangeMail(Mockito.anyString(), Mockito.anyString());

        mockMvc.perform(MockMvcRequestBuilders.post("/accounts/verify-change-mail")
                        .param("accountId", "1234567890")
                        .param("verificationCode", "123456"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("Email has been successfully updated"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void searchUsers_success() throws Exception {
        List<AccountResponse> accountResponses = List.of(accountResponse);
        Page<AccountResponse> responsePage = new PageImpl<>(accountResponses);

        Mockito.when(accountService.searchMember(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(responsePage);

        mockMvc.perform(MockMvcRequestBuilders.get("/accounts/search")
                        .param("name", "John")
                        .param("bookTitle", "Introduction to Algorithms")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

}
