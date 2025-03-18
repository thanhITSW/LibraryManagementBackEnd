package nmtt.demo.dto.request.Account;

import jakarta.validation.constraints.*;
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

    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Email is invalid")
    private String email;

    @Size(min = 5, message = "Password must be at least 5 characters")
    private String password;

    @NotBlank(message = "First name cannot be empty")
    @Size(max = 50, message = "First name must not exceed 50 characters")
    private String firstName;

    @NotNull(message = "Date of birth cannot be null")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dob;

    @NotBlank(message = "Last name cannot be empty")
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    private String lastName;

    @NotBlank(message = "Phone cannot be empty")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone must contain exactly 10 digits")
    private String phone;

    private boolean active;
    List<String> roles;
}
