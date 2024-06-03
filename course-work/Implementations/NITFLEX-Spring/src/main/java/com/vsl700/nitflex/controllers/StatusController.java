package com.vsl700.nitflex.controllers;

import com.vsl700.nitflex.models.dto.UserStatusDTO;
import com.vsl700.nitflex.repo.UserRepository;
import com.vsl700.nitflex.services.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StatusController {
    @Autowired
    private AuthenticationService authService;

    @GetMapping("/userStatus")
    public UserStatusDTO getUserStatus(){
        return authService.getUserStatus();
    }
}
