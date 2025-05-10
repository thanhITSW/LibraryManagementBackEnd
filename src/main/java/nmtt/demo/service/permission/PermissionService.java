package nmtt.demo.service.permission;

import nmtt.demo.dto.request.Account.PermissionRequest;
import nmtt.demo.dto.response.Account.PermissionResponse;

import java.util.List;
public interface PermissionService {
    public PermissionResponse create(PermissionRequest request);

    public List<PermissionResponse> getAll();

    public void delete(String permission);
}
