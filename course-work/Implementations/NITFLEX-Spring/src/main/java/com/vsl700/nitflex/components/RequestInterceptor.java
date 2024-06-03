package com.vsl700.nitflex.components;

import com.vsl700.nitflex.models.User;
import com.vsl700.nitflex.repo.UserRepository;
import com.vsl700.nitflex.services.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@AllArgsConstructor
public class RequestInterceptor implements HandlerInterceptor {
    private UserRepository userRepo;
    private AuthenticationService authService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler){
        if(authService.getCurrentUserName() != null && userRepo.findByUsername(authService.getCurrentUserName()).orElseThrow().getStatus().equals(User.UserStatus.BANNED)){
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        return true;
    }
}
