package nmtt.demo.configuration;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import nmtt.demo.entity.Account;
import nmtt.demo.enums.Role;
import nmtt.demo.repository.AccountRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInitConfig {

    PasswordEncoder passwordEncoder;

    @Bean
    ApplicationRunner applicationRunner(AccountRepository accountRepository){
        return args -> {
            if(accountRepository.findAccountByEmail("admin@gmail.com").isEmpty()){
                var roles = new HashSet<String>();
                roles.add(Role.ADMIN.name());
                Account account = Account.builder()
                        .email("admin@gmail.com")
                        .roles(roles)
                        .password(passwordEncoder.encode("admin"))
                        .build();

                accountRepository.save(account);
                log.warn("admin user has been created with default password: admin, please change it");
            }
        };
    }
}