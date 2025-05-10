package nmtt.demo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "email_verification")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmailVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    String id;

    @Column(name = "account_id", nullable = false)
    String accountId;

    @Email
    @Column(name = "new_email", nullable = false)
    String newEmail;

    @Column(name = "verification_code", nullable = false)
    String verificationCode;
}