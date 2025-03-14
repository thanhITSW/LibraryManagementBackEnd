package nmtt.demo.service.account;

import nmtt.demo.entity.Account;
import nmtt.demo.entity.Role;
import nmtt.demo.repository.AccountRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import nmtt.demo.exception.AppException;
import nmtt.demo.enums.ErrorCode;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final AccountRepository accountRepository;

    public CustomUserDetailsService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    /**
     * Custom implementation of Spring Security's UserDetailsService interface.
     * This service is responsible for loading user-specific data when a user logs in.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws AppException {
        Account account = accountRepository
                .findAccountByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        String[] roleNames = account.getRoles()
                .stream()
                .map(Role::getName)
                .toArray(String[]::new);

        return User.builder()
                .username(account.getEmail())
                .password(account.getPassword())
                .roles(roleNames)
                .build();
    }
}