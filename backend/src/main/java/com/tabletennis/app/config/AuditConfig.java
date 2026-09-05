package com.tabletennis.app.config;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.security.core.context.SecurityContextHolder;
import jakarta.servlet.http.*;
import org.slf4j.*;
import java.util.Set;
@Configuration
public class AuditConfig implements WebMvcConfigurer {
    private static final Logger audit=LoggerFactory.getLogger("ADMIN_AUDIT");
    @Override public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override public void afterCompletion(HttpServletRequest request,HttpServletResponse response,Object handler,Exception ex) {
                var auth=SecurityContextHolder.getContext().getAuthentication();
                if(auth!=null && auth.getAuthorities().stream().anyMatch(a->a.getAuthority().equals("ROLE_ADMIN"))
                    && Set.of("POST","PUT","DELETE").contains(request.getMethod())) {
                    audit.info("actor={} method={} path={} status={}",auth.getName(),request.getMethod(),request.getRequestURI().replaceAll("[\\r\\n]",""),response.getStatus());
                }
            }
        }).addPathPatterns("/api/v1/**");
    }
}
