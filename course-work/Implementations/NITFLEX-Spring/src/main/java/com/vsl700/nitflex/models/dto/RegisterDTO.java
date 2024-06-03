package com.vsl700.nitflex.models.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RegisterDTO {
    private String username;
    private String password;
    private String role;
    private int deviceLimit;
}
