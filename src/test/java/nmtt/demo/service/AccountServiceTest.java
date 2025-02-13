package nmtt.demo.service;

import nmtt.demo.dto.request.Account.AccountCreationRequest;
import nmtt.demo.dto.response.Account.AccountResponse;
import nmtt.demo.entity.Account;
import nmtt.demo.exception.AppException;
import nmtt.demo.repository.AccountRepository;
import nmtt.demo.service.account.AccountService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertThrows;
@SpringBootTest
public class AccountServiceTest {
    @Autowired
    private AccountService accountService;

    @MockBean
    private AccountRepository accountRepository;

    private Account account;

    private AccountCreationRequest request;
    private AccountResponse accountResponse;

    private LocalDate dob;

    @BeforeEach
    void initData(){
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

        account = Account.builder()
                .id("1234567890")
                .email("john@gmail.com")
                .firstName("John")
                .lastName("Doe")
                .dob(dob)
                .build();
    }

    @Test
    void createAccount_validRequest_success(){
        //GIVEN
        when(accountRepository.existsByEmail(anyString())).thenReturn(false);
        when(accountRepository.save(any())).thenReturn(account);

        //WHEN
        var response = accountService.createAccount(request);

        //THEN
        Assertions.assertThat(response.getId()).isEqualTo("1234567890");
        Assertions.assertThat(response.getEmail()).isEqualTo("john@gmail.com");
    }

    @Test
    void createAccount_emailExisted_fail(){
        //GIVEN
        when(accountRepository.existsByEmail(anyString())).thenReturn(true);

        //WHEN
        var exception = assertThrows(AppException.class,
                () -> accountService.createAccount(request));

        Assertions.assertThat(exception.getErrorCode().getCode()).isEqualTo(1002);
    }
}
