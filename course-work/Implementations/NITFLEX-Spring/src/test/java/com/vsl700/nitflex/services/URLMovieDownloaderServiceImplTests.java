package com.vsl700.nitflex.services;

import com.vsl700.nitflex.components.SharedProperties;
import com.vsl700.nitflex.components.WebsiteCredentials;
import com.vsl700.nitflex.exceptions.InvalidTorrentException;
import com.vsl700.nitflex.services.implementations.TorrentMovieDownloaderServiceImpl;
import com.vsl700.nitflex.services.implementations.URLMovieDownloaderServiceImpl;
import com.vsl700.nitflex.services.implementations.WebClientServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;

import java.io.File;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@Disabled
@ExtendWith(MockitoExtension.class)
public class URLMovieDownloaderServiceImplTests {

    //@Mock
    private SharedProperties sharedProperties;
    //@Mock
    private WebsiteCredentials.Zamunda zamundaCredentials;
    //@Mock
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
        //doNothing().when(movieDownloaderService).downloadFromTorrentFilePath(anyString());
    }

    @Test
    public void downloadFromPageURL_Test(){
        // Setup
        AtomicBoolean flag = new AtomicBoolean(false);
        doAnswer((invocation) -> {
            flag.set(true);
            return null;
        }).when(torrentMovieDownloaderService).downloadFromTorrentFilePath(any());

        doReturn(-1).when(sharedProperties).getMovieSizeLimit();

        // Assert
        assertDoesNotThrow(() ->
                urlMovieDownloaderService.downloadFromPageURL(new URL("https://zamunda.net/banan?id=747087&hit=1&t=movie")));

        assertThat(flag.get()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"https://zamunda.net/banan?id=747087&hit=1&t=movie",
                            "https://zamunda.net/banan?id=747384&hit=1&t=movie",
                            "https://zamunda.net/banan?id=747134&hit=1&t=movie"})
    public void downloadFromPageURL_RepeatedTest(String url){
        // Setup
        AtomicBoolean flag = new AtomicBoolean(false);
        doAnswer((invocation) -> {
            flag.set(true);
            return null;
        }).when(torrentMovieDownloaderService).downloadFromTorrentFilePath(any());

        doReturn(-1).when(sharedProperties).getMovieSizeLimit();

        // Assert
        assertDoesNotThrow(() ->
                urlMovieDownloaderService.downloadFromPageURL(new URL(url)));

        assertThat(flag.get()).isTrue();
    }

    @Test
    public void downloadFromPageURL_Throws_MovieSize_Test(){
        // Setup
        doReturn(15).when(sharedProperties).getMovieSizeLimit();

        // Assert
        assertThrows(InvalidTorrentException.class, () ->
                urlMovieDownloaderService.downloadFromPageURL(new URL("https://zamunda.net/banan?id=747087&hit=1&t=movie"))); // 20GB
    }

}
