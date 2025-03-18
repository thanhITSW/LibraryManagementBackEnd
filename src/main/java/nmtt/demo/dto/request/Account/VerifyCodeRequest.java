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
public class VerifyCodeRequest {
    @Size(min= 6, message = "Code must be at least 6 numbers")
    String otp;
}
