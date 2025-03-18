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
public class ChangePasswordRequest {
    @Size(min = 5, message = "Password must be at least 5 characters")
    private String oldPassword;

    @Size(min = 5, message = "Password must be at least 5 characters")
    private String newPassword;
}
