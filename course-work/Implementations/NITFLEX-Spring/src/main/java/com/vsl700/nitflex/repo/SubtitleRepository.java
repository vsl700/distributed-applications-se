package com.vsl700.nitflex.repo;

import com.vsl700.nitflex.models.Subtitle;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface SubtitleRepository extends MongoRepository<Subtitle, String> {
    Optional<Subtitle> findByPath(String path);
    List<Subtitle> findAllByMovieId(String id);
    void deleteByMovieId(String id);
}
