package com.vsl700.nitflex.configs;

import com.vsl700.nitflex.components.RequestInterceptor;
import com.vsl700.nitflex.repo.UserRepository;
import com.vsl700.nitflex.services.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class RequestInterceptorConfig implements WebMvcConfigurer {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private AuthenticationService authService;

    // Register an interceptor with the registry, Interceptor name : RequestInterceptor
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RequestInterceptor(userRepo, authService));
    }
    //* We can register any number of interceptors with our spring application context
}
