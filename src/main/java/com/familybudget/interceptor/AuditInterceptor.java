package com.familybudget.interceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuditInterceptor implements HandlerInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(AuditInterceptor.class);
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String user = request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : "ANON";
        logger.info("AUDIT: [{}] {} {} from IP: {}", user, request.getMethod(), request.getRequestURI(), request.getRemoteAddr());
        return true;
    }
}