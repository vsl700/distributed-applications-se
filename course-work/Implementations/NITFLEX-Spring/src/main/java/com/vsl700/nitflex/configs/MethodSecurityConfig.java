package com.vsl700.nitflex.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = false, securedEnabled = true, jsr250Enabled = false)
public class MethodSecurityConfig extends GlobalMethodSecurityConfiguration {
}
