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
public class ChangePhoneRequest {
    @Size(min= 10, message = "Phone must be at least 10 characters")
    String phone;
}