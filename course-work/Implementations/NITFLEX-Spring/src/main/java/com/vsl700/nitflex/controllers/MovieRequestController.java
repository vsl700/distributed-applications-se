package com.vsl700.nitflex.controllers;

import com.vsl700.nitflex.components.AutoMovieDownloader;
import com.vsl700.nitflex.components.SharedProperties;
import com.vsl700.nitflex.models.User;
import com.vsl700.nitflex.models.dto.MovieRequestDTO;
import com.vsl700.nitflex.repo.UserRepository;
import com.vsl700.nitflex.services.AuthenticationService;
import com.vsl700.nitflex.services.MovieLoaderService;
import com.vsl700.nitflex.services.MovieTranscoderService;
import com.vsl700.nitflex.services.URLMovieDownloaderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.net.URL;

@RestController
public class MovieRequestController {
    private static final Logger LOG = LoggerFactory.getLogger(AutoMovieDownloader.class);

    @Autowired
    private URLMovieDownloaderService urlMovieDownloaderService;

    @Autowired
    private MovieLoaderService movieLoaderService;

    @Autowired
    private MovieTranscoderService movieTranscoderService;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SharedProperties sharedProperties;

    @Secured("ROLE_WATCH_CONTENT_PRIVILEGE")
    @PostMapping("/request")
    public void requestMovie(@RequestBody MovieRequestDTO movieRequestDTO) throws MalformedURLException { // TODO Add custom exception
        User user = userRepository.findByUsername(authenticationService.getCurrentUserName()).orElseThrow();
        LOG.info("Movie request by %s: (%s)".formatted(user.getUsername(), movieRequestDTO.getUrl()));

        urlMovieDownloaderService.downloadFromPageURLAsync(new URL(movieRequestDTO.getUrl()), path -> {
            LOG.info("Loading newly downloaded movie(s)...");
            var newMovies = movieLoaderService.load(path, user);

            if(sharedProperties.isTranscodingEnabled()) {
                LOG.info("Transcoding newly downloaded movie(s)...");
                newMovies.forEach(m -> movieTranscoderService.transcode(m));
                LOG.info("Transcoding of newly downloaded movie(s) is done!");
            }

            LOG.info("Newly downloaded movie(s) are now available to watch!");
        });
    }
}
