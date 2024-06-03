package com.vsl700.nitflex.controllers.streaming;

import com.vsl700.nitflex.components.SharedProperties;
import com.vsl700.nitflex.models.Movie;
import com.vsl700.nitflex.models.Subtitle;
import com.vsl700.nitflex.repo.MovieRepository;
import com.vsl700.nitflex.repo.SubtitleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
public class SubtitlesStreamingController {
    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private SubtitleRepository subtitleRepository;

    @Autowired
    private SharedProperties sharedProperties;

    @Secured("ROLE_WATCH_CONTENT_PRIVILEGE")
    @GetMapping("stream/subs/{id}/{subtitlesId}")
    public ResponseEntity<Resource> getSubtitlesFile(@PathVariable String id, @PathVariable String subtitlesId) throws URISyntaxException {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(); // TODO Add custom exception

        Subtitle subtitle = subtitleRepository.findById(subtitlesId)
                .orElseThrow(); // TODO Add custom exception

        Path subtitlePath = Paths.get(sharedProperties.getMoviesFolder(), movie.getPath(), subtitle.getPath());

        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new FileSystemResource(subtitlePath));
    }
}
