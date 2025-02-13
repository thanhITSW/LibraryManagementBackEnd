package nmtt.demo.service.permission;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nmtt.demo.dto.request.Account.PermissionRequest;
import nmtt.demo.dto.response.Account.PermissionResponse;
import nmtt.demo.entity.Permission;
import nmtt.demo.mapper.PermissionMapper;
import nmtt.demo.repository.PermissionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionServiceImpl implements PermissionService{
    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;

    /**
     * Creates a new permission based on the provided request and returns the created permission as a response.
     *
     * @param request The request containing the details of the permission to be created.
     * @return The created permission as a PermissionResponse.
     */
    @Override
    public PermissionResponse create(PermissionRequest request){
        Permission permission = permissionMapper.toPermission(request);
        permission = permissionRepository.save(permission);

        return permissionMapper.toPermissionResponse(permission);
    }

    /**
     * Retrieves all permissions and returns them as a list of PermissionResponse objects.
     *
     * @return A list of all permissions as PermissionResponse objects.
     */
    @Override
    public List<PermissionResponse> getAll(){
        var permissions = permissionRepository.findAll();
        return permissions
                .stream()
                .map(permissionMapper::toPermissionResponse)
                .toList();
    }

    /**
     * Deletes a permission by its identifier.
     *
     * @param permission The identifier of the permission to be deleted.
     */
    @Override
    public void delete(String permission){
        permissionRepository.deleteById(permission);
    }
}
