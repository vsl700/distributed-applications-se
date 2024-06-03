package com.vsl700.nitflex.services;

import com.vsl700.nitflex.models.Episode;
import com.vsl700.nitflex.models.Movie;
import com.vsl700.nitflex.models.Subtitle;
import com.vsl700.nitflex.models.dto.MovieSettingsDTO;

import java.util.Collection;
import java.util.List;

public interface MovieAPIService {
    List<Movie> getAllAvailableMovies();
    List<Movie> searchMovies(String search);
    Movie getMovieById(String movieId);
    void deleteMovieById(String movieId);
    void updateMovieSettingsById(String movieId, MovieSettingsDTO movieSettingsDTO);
    List<Episode> getEpisodesByMovieId(String movieId);
    List<Subtitle> getAllSubtitlesByMovieId(String movieId);
    List<Subtitle> getTrailerSubtitlesByMovieId(String movieId);
    List<Subtitle> getFilmSubtitlesByMovieId(String movieId);
    List<Subtitle> getEpisodeSubtitlesByMovieAndEpisodeId(String movieId, String episodeId);
}
