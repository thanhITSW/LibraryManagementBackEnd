package nmtt.demo.repository;

import nmtt.demo.entity.OtpPhone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface OtpPhoneRepository extends JpaRepository<OtpPhone, Long> {
    Optional<OtpPhone> findByOtpAndAccountIdAndCreatedAtAfter(String otp, String accountId, LocalDateTime localDateTime);
}
