package nmtt.demo.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AccountCreationRequest {
    String username;

    @Size(min = 6, message = "PASSWORD_INVALID")
    String password;
    String firstName;
    LocalDate dob;
    String lastName;
}
