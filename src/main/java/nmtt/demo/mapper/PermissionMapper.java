package nmtt.demo.mapper;

import nmtt.demo.dto.request.AccountCreationRequest;
import nmtt.demo.dto.request.AccountUpdateRequest;
import nmtt.demo.dto.request.PermissionRequest;
import nmtt.demo.dto.response.AccountResponse;
import nmtt.demo.dto.response.PermissionResponse;
import nmtt.demo.entity.Account;
import nmtt.demo.entity.Permission;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PermissionMapper {
    Permission toPermission(PermissionRequest request);
    PermissionResponse toPermissionResponse(Permission permission);
}
