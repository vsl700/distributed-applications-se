package com.vsl700.nitflex.repo;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.vsl700.nitflex.models.User;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByUsername(String username);
}
