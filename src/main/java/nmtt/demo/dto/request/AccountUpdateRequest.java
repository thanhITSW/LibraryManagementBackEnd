package nmtt.demo.dto.request;

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
}
