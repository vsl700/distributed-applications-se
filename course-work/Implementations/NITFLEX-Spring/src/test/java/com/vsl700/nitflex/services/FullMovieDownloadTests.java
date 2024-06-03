package com.vsl700.nitflex.services;

import com.vsl700.nitflex.components.SharedProperties;
import com.vsl700.nitflex.components.WebsiteCredentials;
import com.vsl700.nitflex.services.implementations.TorrentMovieDownloaderServiceImpl;
import com.vsl700.nitflex.services.implementations.URLMovieDownloaderServiceImpl;
import com.vsl700.nitflex.services.implementations.WebClientServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.CALLS_REAL_METHODS;

@Disabled
@ExtendWith(MockitoExtension.class)
/**
 * This class is used to test the URLMovieDownloaderServiceImpl
 */
public class FullMovieDownloadTests {

    private SharedProperties sharedProperties;
    private WebsiteCredentials.Zamunda zamundaCredentials;
    private URLMovieDownloaderServiceImpl urlMovieDownloaderService;
    private TorrentMovieDownloaderService torrentMovieDownloaderService;
    private WebClientService webClientService;

    @BeforeEach
    public void setUp(){
        sharedProperties = mock(SharedProperties.class,
                Mockito.withSettings().strictness(Strictness.LENIENT));
        zamundaCredentials = mock(WebsiteCredentials.Zamunda.class,
                Mockito.withSettings().strictness(Strictness.LENIENT));
        torrentMovieDownloaderService = mock(TorrentMovieDownloaderServiceImpl.class,
                Mockito.withSettings().useConstructor(sharedProperties)
                        .defaultAnswer(CALLS_REAL_METHODS)
                        .strictness(Strictness.LENIENT));
        webClientService = new WebClientServiceImpl();

        when(sharedProperties.getMoviesFolder()).then(invocation -> "D:\\Videos\\Torrent Test");

        when(zamundaCredentials.getUsername()).then(invocation -> "username");
        when(zamundaCredentials.getPassword()).then(invocation -> "password");

        urlMovieDownloaderService = mock(URLMovieDownloaderServiceImpl.class,
                Mockito.withSettings().useConstructor(webClientService, torrentMovieDownloaderService, sharedProperties, zamundaCredentials)
                        .defaultAnswer(CALLS_REAL_METHODS)
                        .strictness(Strictness.LENIENT));
    }

    @Test
    public void fullDownload_Test() {
        doReturn(-1).when(sharedProperties).getMovieSizeLimit();

        assertDoesNotThrow(() ->
                urlMovieDownloaderService.downloadFromPageURL(new URL("https://zamunda.net/banan?id=747087&hit=1&t=movie")));
    }
}
