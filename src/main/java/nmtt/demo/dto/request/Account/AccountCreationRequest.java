package nmtt.demo.dto.request.Account;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AccountCreationRequest {
    @Email
    String email;

    @Size(min = 5, message = "PASSWORD_INVALID")
    String password;
    String firstName;
    LocalDate dob;
    String lastName;
}
