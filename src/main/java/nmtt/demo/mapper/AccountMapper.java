package nmtt.demo.mapper;

import nmtt.demo.dto.request.Account.AccountCreationRequest;
import nmtt.demo.dto.request.Account.AccountUpdateRequest;
import nmtt.demo.dto.response.Account.AccountResponse;
import nmtt.demo.entity.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AccountMapper {
    Account toAccount(AccountCreationRequest request);
    AccountResponse toAccountResponse(Account account);

    @Mapping(target = "roles", ignore = true)
    void updateAccount(@MappingTarget Account account, AccountUpdateRequest request);
}
