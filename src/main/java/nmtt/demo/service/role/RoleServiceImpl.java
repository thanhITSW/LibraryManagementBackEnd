package nmtt.demo.service.role;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nmtt.demo.dto.request.Account.RoleRequest;
import nmtt.demo.dto.response.Account.RoleResponse;
import nmtt.demo.mapper.RoleMapper;
import nmtt.demo.repository.PermissionRepository;
import nmtt.demo.repository.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleServiceImpl implements RoleService{
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RoleMapper roleMapper;

    /**
     * Creates a new role with the given permissions.
     *
     * @param request The request containing the role data and the list of permission IDs.
     * @return The response with the created role data.
     */
    @Override
    public RoleResponse create(RoleRequest request){
        var role = roleMapper.toRole(request);

        var permissions = permissionRepository
                .findAllById(request
                        .getPermissions());
        role.setPermissions(new HashSet<>(permissions));

        role = roleRepository.save(role);
        return roleMapper.toRoleResponse(role);
    }

    /**
     * Retrieves all roles and returns them as a list of RoleResponse.
     *
     * @return A list of RoleResponse containing all roles in the system.
     */
    @Override
    public List<RoleResponse> getAll() {
        return roleRepository.findAll()
                .stream()
                .map(roleMapper::toRoleResponse)
                .toList();
    }

    /**
     * Deletes a role by its ID.
     *
     * @param role The ID of the role to be deleted.
     */
    @Override
    public void delete(String role) {
        roleRepository.deleteById(role);
    }
}
