package nmtt.demo.mapper;

import nmtt.demo.dto.request.AccountCreationRequest;
import nmtt.demo.dto.request.AccountUpdateRequest;
import nmtt.demo.dto.response.AccountResponse;
import nmtt.demo.entity.Account;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AccountMapper {
    Account toAccount(AccountCreationRequest request);
    AccountResponse toAccountResponse(Account account);
    void updateAccount(@MappingTarget Account account, AccountUpdateRequest request);
}
