package com.vsl700.nitflex.models.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserSettingsDTO {
    private String status;
    private String role;
    private int deviceLimit;
}
