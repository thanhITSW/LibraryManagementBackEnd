package nmtt.demo.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class AccountUpdateRequest {
    String email;
    String password;
    String firstName;
    LocalDate dob;
    String lastName;
}
