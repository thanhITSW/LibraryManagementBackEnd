package nmtt.demo.dto.request.Account;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class AccountUpdateRequest {
    @Email(message = "Email is invalid")
    private String email;

    @Size(min = 5, message = "Password must be at least 5 characters")
    private String password;

    String firstName;
    LocalDate dob;
    String lastName;
    List<String> roles;
    boolean active;
}
