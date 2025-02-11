package nmtt.demo.dto.request.Account;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class AccountUpdateRequest {
    String email;
    String password;
    String firstName;
    LocalDate dob;
    String lastName;
    List<String> roles;
    boolean active;
}
