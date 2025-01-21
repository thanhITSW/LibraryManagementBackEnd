package nmtt.demo.mapper;

import nmtt.demo.dto.request.Account.PermissionRequest;
import nmtt.demo.dto.response.Account.PermissionResponse;
import nmtt.demo.entity.Permission;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PermissionMapper {
    Permission toPermission(PermissionRequest request);
    PermissionResponse toPermissionResponse(Permission permission);
}
