package nmtt.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmailVerification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    String accountId;
    @Email
    String newEmail;
    String verificationCode;
}
