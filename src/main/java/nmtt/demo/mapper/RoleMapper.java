package nmtt.demo.mapper;

import nmtt.demo.dto.request.Account.RoleRequest;
import nmtt.demo.dto.response.Account.RoleResponse;
import nmtt.demo.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    @Mapping(target = "permissions", ignore = true)
    Role toRole(RoleRequest request);

    RoleResponse toRoleResponse(Role role);
}
