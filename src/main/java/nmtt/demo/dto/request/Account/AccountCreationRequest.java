package nmtt.demo.dto.request.Account;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountCreationRequest {
    @Email(message = "Email is invalid")
    private String email;

    @Size(min = 5, message = "Password must be at least 5 characters")
    private String password;

    private String firstName;
    private LocalDate dob;
    private String lastName;
}
