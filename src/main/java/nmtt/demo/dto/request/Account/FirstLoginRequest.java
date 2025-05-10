package nmtt.demo.dto.request.Account;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FirstLoginRequest {
    @Size(min = 5, message = "Password must be at least 5 characters")
    private String newPassword;
}
