package com.vsl700.nitflex.services;

import com.vsl700.nitflex.models.Movie;
import com.vsl700.nitflex.models.User;

import java.nio.file.Path;
import java.util.List;

public interface MovieLoaderService {
    /**
     * Loads a concrete Movie or collection of Movies in a specific path.
     * Used when server loads a Movie automatically
     * @param initialPath absolute path to the Movie to be loaded
     * @return a list of the just loaded Movies
     */
    List<Movie> load(Path initialPath);

    /**
     * Loads a concrete Movie or collection of Movies in a specific path.
     * Used when a User requested the Movie
     * @param initialPath absolute path to the Movie to be loaded
     * @param requester the User that requested the Movie
     * @return a list of the just loaded Movies
     */
    List<Movie> load(Path initialPath, User requester);

    /**
     * Scans the whole movies folder and registers (loads) all movies
     * that are not registered
     */
    void loadNewlyAdded();

    /**
     * Scans the whole movies folder and unloads all movies that are
     * deleted from the folder
     */
    void unloadNonExisting();
}
