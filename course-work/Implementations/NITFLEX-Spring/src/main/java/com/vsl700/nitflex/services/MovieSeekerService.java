package com.vsl700.nitflex.services;

import java.net.URL;

/**
 * Looks for a new Movie to be downloaded
 */
public interface MovieSeekerService {
    /**
     * Looks for a new Movie to be downloaded and returns the URL to its page
     * @return the address to a new movie's page
     */
    URL findMovieURL();
}
