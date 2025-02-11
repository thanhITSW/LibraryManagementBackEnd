package nmtt.demo.repository;

import nmtt.demo.entity.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, String> {
    Optional<EmailVerification> findByAccountIdAndVerificationCode(String accountId, String verificationCode);
}
