package com.vsl700.nitflex.services;

import com.vsl700.nitflex.components.SharedProperties;
import com.vsl700.nitflex.components.WebsiteCredentials;
import com.vsl700.nitflex.services.implementations.MovieSeekerServiceImpl;
import com.vsl700.nitflex.services.implementations.WebClientServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.when;

@Disabled
@ExtendWith(MockitoExtension.class)
public class MovieSeekerServiceImplTests {

    private MovieSeekerService movieSeekerService;
    private WebClientService webClientService;

    @Mock
    private SharedProperties sharedProperties;
    @Mock
    private WebsiteCredentials.Zamunda zamundaCredentials;

    @BeforeEach
    public void setUp(){
        webClientService = new WebClientServiceImpl();
        movieSeekerService = new MovieSeekerServiceImpl(webClientService, zamundaCredentials, sharedProperties);

        when(sharedProperties.getMovieSizeLimit()).then(invocation -> -1);

        when(zamundaCredentials.getUsername()).then(invocation -> "username");
        when(zamundaCredentials.getPassword()).then(invocation -> "password");
    }

    @Test
    public void doesNotThrow_Test(){
        assertDoesNotThrow(() -> {
            URL link = movieSeekerService.findMovieURL();
            System.out.println(link);
        });
    }

    @RepeatedTest(30)
    public void repeated_Test(){
        URL link = movieSeekerService.findMovieURL();
        System.out.println(link);
    }
}
