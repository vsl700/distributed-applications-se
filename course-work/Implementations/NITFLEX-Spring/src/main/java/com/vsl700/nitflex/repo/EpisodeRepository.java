package com.vsl700.nitflex.repo;

import com.vsl700.nitflex.models.Episode;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface EpisodeRepository extends MongoRepository<Episode, String> {
    List<Episode> findAllBySeriesId(String seriesId);
    void deleteBySeriesId(String seriesId);
}
