package nmtt.demo.dto.request.Account;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nmtt.demo.entity.Role;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminCreationAccountRequest {
    @Email(message = "Email is invalid")
    private String email;

    @Size(min = 5, message = "Password must be at least 5 characters")
    private String password;

    private String firstName;
    private LocalDate dob;
    private String lastName;

    @Size(min= 10, message = "Phone must be at least 10 characters")
    private String phone;
    private boolean active;
    List<String> roles;
}
