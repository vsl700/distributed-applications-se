package com.vsl700.nitflex.repo;

import com.vsl700.nitflex.models.Privilege;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PrivilegeRepository extends MongoRepository<Privilege, String> {
    Optional<Privilege> findByName(String name);
}
