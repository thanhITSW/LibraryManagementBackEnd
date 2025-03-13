package nmtt.demo.dto.request.Account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangePhoneRequest {
    @NotBlank(message = "Phone cannot be empty")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone must contain exactly 10 digits")
    String phone;
}