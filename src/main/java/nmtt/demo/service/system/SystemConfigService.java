package nmtt.demo.service.system;

import nmtt.demo.entity.SystemConfig;

public interface SystemConfigService {
    public SystemConfig getConfig();

    public SystemConfig updateMaintenanceMode(boolean maintenanceMode);

}
