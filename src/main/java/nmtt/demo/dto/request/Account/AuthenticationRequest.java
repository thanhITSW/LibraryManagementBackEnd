package nmtt.demo.dto.request.Account;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthenticationRequest {
    @Email(message = "Email is invalid")
    private String email;

    @Size(min = 5, message = "Password must be at least 5 characters")
    private String password;
}
