package com.vsl700.nitflex.components;

import com.vsl700.nitflex.repo.MovieRepository;
import com.vsl700.nitflex.services.MovieTranscoderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class InitialMoviesTranscoder {

    private static final Logger LOG = LoggerFactory.getLogger(InitialMoviesLoader.class);

    @Autowired
    private MovieRepository movieRepo;
    @Autowired
    private MovieTranscoderService movieTranscoderService;
    @Autowired
    private SharedProperties sharedProperties;

    @EventListener
    public void eventListener(ApplicationReadyEvent event){
        if(!sharedProperties.isTranscodingEnabled())
            return;

        LOG.info("Transcoding non-transcoded movies...");
        movieRepo.findAll().forEach(m -> {
            LOG.info("Checking %s...".formatted(m.getName()));
            if(!m.isTranscoded()) {
                LOG.info("Transcoding %s...".formatted(m.getName()));
                movieTranscoderService.transcode(m);
            }
        });
        LOG.info("Movie transcoding is done!");
    }
}
