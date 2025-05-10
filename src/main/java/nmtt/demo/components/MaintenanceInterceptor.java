package nmtt.demo.components;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import nmtt.demo.service.system.SystemConfigService;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class MaintenanceInterceptor implements HandlerInterceptor {
    private final SystemConfigService systemConfigService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (systemConfigService.getConfig().isMaintenanceMode()) {
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("status", HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            responseBody.put("message", "System is under maintenance. Please try again later.");

            String jsonResponse = new ObjectMapper().writeValueAsString(responseBody);
            response.getWriter().write(jsonResponse);
            return false;
        }
        return true;
    }
}
