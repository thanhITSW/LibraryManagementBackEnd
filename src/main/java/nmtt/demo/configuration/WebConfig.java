package nmtt.demo.configuration;

import lombok.RequiredArgsConstructor;
import nmtt.demo.components.MaintenanceInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    private final MaintenanceInterceptor maintenanceInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(maintenanceInterceptor)
                .addPathPatterns("/**") // Apply to all APIs
                .excludePathPatterns("/api/admin/system-config/**")
                .excludePathPatterns("/api/common/system-config")
                .excludePathPatterns("/api/common/auth/login")
                .excludePathPatterns("/api/common/auth/logout");// Allow access to config APIs
    }
}