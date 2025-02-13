package nmtt.demo.service.role;

import nmtt.demo.dto.request.Account.RoleRequest;
import nmtt.demo.dto.response.Account.RoleResponse;
import java.util.List;

public interface RoleService {

    public RoleResponse create(RoleRequest request);
    public List<RoleResponse> getAll();
    public void delete(String role);
}
