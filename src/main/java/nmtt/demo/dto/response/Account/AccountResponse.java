package nmtt.demo.dto.response.Account;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountResponse {
    String id;
    String email;
    String firstName;
    String lastName;
    LocalDate dob;
    String phone;
    Set<RoleResponse> roles;
    boolean active;
}
