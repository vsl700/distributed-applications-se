package com.vsl700.nitflex.components;

import com.vsl700.nitflex.models.Movie;
import com.vsl700.nitflex.services.MovieLoaderService;
import com.vsl700.nitflex.services.MovieSeekerService;
import com.vsl700.nitflex.services.MovieTranscoderService;
import com.vsl700.nitflex.services.URLMovieDownloaderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class AutoMovieDownloader {
    private static final Logger LOG = LoggerFactory.getLogger(AutoMovieDownloader.class);

    @Autowired
    private MovieSeekerService movieSeekerService;
    @Autowired
    private URLMovieDownloaderService urlMovieDownloaderService;
    @Autowired
    private MovieLoaderService movieLoaderService;
    @Autowired
    private MovieTranscoderService movieTranscoderService;
    @Autowired
    private SharedProperties sharedProperties;

    @Scheduled(initialDelayString = "${nitflex.download-interval}",
            fixedRateString = "${nitflex.download-interval}",
            timeUnit = TimeUnit.DAYS)
    public void run(){
        LOG.info("Auto-download triggered!");

        Path path = urlMovieDownloaderService.downloadFromPageURL(movieSeekerService.findMovieURL());
        List<Movie> newMovies = movieLoaderService.load(path);
        if(sharedProperties.isTranscodingEnabled())
            newMovies.forEach(m -> movieTranscoderService.transcode(m));
    }
}
