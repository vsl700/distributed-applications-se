package com.vsl700.nitflex.services.implementations;

import com.vsl700.nitflex.components.SharedProperties;
import com.vsl700.nitflex.models.Episode;
import com.vsl700.nitflex.models.Movie;
import com.vsl700.nitflex.models.Subtitle;
import com.vsl700.nitflex.models.dto.MovieSettingsDTO;
import com.vsl700.nitflex.repo.EpisodeRepository;
import com.vsl700.nitflex.repo.MovieRepository;
import com.vsl700.nitflex.repo.SubtitleRepository;
import com.vsl700.nitflex.services.MovieAPIService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

@Service
@AllArgsConstructor
public class MovieAPIServiceImpl implements MovieAPIService {
    private MovieRepository movieRepository;
    private EpisodeRepository episodeRepository;
    private SubtitleRepository subtitleRepository;
    private SharedProperties sharedProperties;

    @Override
    public List<Movie> getAllAvailableMovies() {
        return movieRepository.findAll().stream()
                .filter(m -> m.isTranscoded() || !sharedProperties.isTranscodingEnabled())
                .toList();
    }

    @Override
    public List<Movie> searchMovies(String search){
        return movieRepository.findAll().stream()
                .filter(m -> m.getName().toLowerCase()
                        .contains(search.toLowerCase()))
                .toList();
    }

    @Override
    public Movie getMovieById(String movieId) {
        return movieRepository.findById(movieId).orElseThrow(); // TODO Add custom exception
    }

    @Override
    public void deleteMovieById(String movieId) {
        Movie movie = movieRepository.findById(movieId).orElseThrow(); // TODO Add custom exception

        try {
            Files.walkFileTree(Path.of(sharedProperties.getMoviesFolder(), movie.getPath()),
                    new SimpleFileVisitor<>() {
                        @Override
                        public FileVisitResult postVisitDirectory(
                                Path dir, IOException exc) throws IOException {
                            Files.delete(dir);
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFile(
                                Path file, BasicFileAttributes attrs)
                                throws IOException {
                            Files.delete(file);
                            return FileVisitResult.CONTINUE;
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException(e); // TODO Add custom exception
        }

        movieRepository.delete(movie);
    }

    @Override
    public void updateMovieSettingsById(String movieId, MovieSettingsDTO movieSettingsDTO) {
        Movie movie = movieRepository.findById(movieId).orElseThrow(); // TODO Add custom exception

        movie.setName(movieSettingsDTO.getName());

        movieRepository.save(movie);
    }

    @Override
    public List<Episode> getEpisodesByMovieId(String movieId) {
        if(movieRepository.findById(movieId).isEmpty())
            throw new RuntimeException("Movie with id %s not found!".formatted(movieId)); // TODO Add custom exception

        return episodeRepository.findAllBySeriesId(movieId);
    }

    @Override
    public List<Subtitle> getAllSubtitlesByMovieId(String movieId) {
        if(movieRepository.findById(movieId).isEmpty())
            throw new RuntimeException("Movie with id %s not found!".formatted(movieId)); // TODO Add custom exception

        return subtitleRepository.findAllByMovieId(movieId);
    }

    @Override
    public List<Subtitle> getTrailerSubtitlesByMovieId(String movieId) {
        if(movieRepository.findById(movieId).isEmpty())
            throw new RuntimeException("Movie with id %s not found!".formatted(movieId)); // TODO Add custom exception

        return subtitleRepository.findAllByMovieId(movieId).stream()
                .filter(s -> s.getType().equals(Subtitle.SubtitleType.Trailer))
                .toList();
    }

    @Override
    public List<Subtitle> getFilmSubtitlesByMovieId(String movieId) {
        if(movieRepository.findById(movieId).isEmpty())
            throw new RuntimeException("Movie with id %s not found!".formatted(movieId)); // TODO Add custom exception

        return subtitleRepository.findAllByMovieId(movieId).stream()
                .filter(s -> s.getType().equals(Subtitle.SubtitleType.Film) || s.getType().equals(Subtitle.SubtitleType.Undetermined))
                .toList();
    }

    @Override
    public List<Subtitle> getEpisodeSubtitlesByMovieAndEpisodeId(String movieId, String episodeId) {
        if(episodeRepository.findAllBySeriesId(movieId).stream().noneMatch(e -> e.getId().equals(episodeId)))
            throw new RuntimeException("Episode %s not found in movie %s!".formatted(episodeId, movieId)); // TODO Add custom exception

        return subtitleRepository.findAllByMovieId(movieId).stream()
                .filter(s -> episodeId.equals(s.getEpisodeId()) || s.getType().equals(Subtitle.SubtitleType.Undetermined))
                .toList();
    }
}
