package nmtt.demo.repository;

import nmtt.demo.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, String>
        , JpaSpecificationExecutor<Account> {
    boolean existsByEmail(String email);
    Optional<Account> findAccountByEmail(String email);
}
