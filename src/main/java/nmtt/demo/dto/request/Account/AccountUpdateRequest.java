package nmtt.demo.dto.request.Account;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class AccountUpdateRequest {
    @Email(message = "Email is invalid")
    private String email;

    String firstName;
    LocalDate dob;
    String lastName;
    List<String> roles;
    boolean active;

    @Size(min = 10, message = "Phone must be at least 10 characters")
    String phone;
}
