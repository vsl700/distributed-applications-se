package com.vsl700.nitflex.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.util.List;

@Document
@RequiredArgsConstructor
@Getter
@Setter
public class Role {
    @Id
    private String id;
    @NonNull
    private String name;

    @DocumentReference
    private List<Privilege> privileges;
}
