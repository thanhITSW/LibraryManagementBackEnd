package nmtt.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "otp_phone")
public class OtpPhone {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "account_id")
    private String accountId;

    @Column(name = "phone")
    private String phone;

    @Column(name = "otp")
    private String otp;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
