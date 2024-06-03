package com.vsl700.nitflex.services.implementations;

import com.vsl700.nitflex.components.SharedProperties;
import com.vsl700.nitflex.models.Episode;
import com.vsl700.nitflex.models.Movie;
import com.vsl700.nitflex.models.Subtitle;
import com.vsl700.nitflex.models.User;
import com.vsl700.nitflex.repo.EpisodeRepository;
import com.vsl700.nitflex.repo.MovieRepository;
import com.vsl700.nitflex.repo.SubtitleRepository;
import com.vsl700.nitflex.services.MovieLoaderService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@AllArgsConstructor
public class MovieLoaderServiceImpl implements MovieLoaderService {

    private MovieRepository movieRepo;

    private EpisodeRepository episodeRepo;

    private SubtitleRepository subtitleRepo;

    private SharedProperties sharedProperties;

    private final BiFunction<String, String, String> pathRelativizer =
            (homePath, fullPath) -> Paths.get(homePath).relativize(Paths.get(fullPath)).toString();

    private static final String[] subtitleFileExtensions = {
            "srt",
            "sub",
            "sbv",
            "ass",
            "ssa",
            "vtt",
            "idx",
            "usf",
            "stl",
            "aqt",
            "jss",
            "txt",
            "mpl",
            "psb",
            "dks",
            "pjs",
            "cdg",
            "rt",
            "svcd",
            "ttml",
            "vplayer",
            "giz",
            "vsf"
    };

    @Override
    public List<Movie> load(Path path){
        return load(path, null);
    }

    @Override
    public List<Movie> load(Path path, User requester) {
        // Check if it is either a collection or a nested folder
        String pathStr = path.toString(); // TODO Finish refactoring (substitute the String methods you call with Path)
        if(isCollection(pathStr)){
            // Handle the collection/nested folder
            // Each folder is treated as a separate Movie
            var allFiles = getFiles(pathStr, (dir, name) -> true, false);
            var dirs = allFiles.stream().filter(File::isDirectory).toList();

            List<Movie> result = new ArrayList<>();
            dirs.forEach(f -> result.addAll(load(Path.of(f.getAbsolutePath()), requester)));
            return result;
        }

        // Determine movie type
        Movie.MovieType type;
        long filmsCount = getFiles(pathStr, (dir, name) -> !isTrailer(name) && isVideoFile(name), false)
                .stream().filter(File::isFile).count();
        if(filmsCount > 1){
            type = Movie.MovieType.Series;
        }else{
            type = Movie.MovieType.Film;
        }

        // Create Movie object
        String relativePath = pathRelativizer
                .apply(sharedProperties.getMoviesFolder(), pathStr);
        String name = Paths.get(relativePath).getFileName().toString();
        long size = getFilesSize(pathStr);
        Movie movie = new Movie(name, type, relativePath, size);
        movie.setRequester(requester);

        movie = movieRepo.save(movie); // Obtain a Movie.Id

        // Load the Movie
        switch(type){
            case Film -> loadFilm(movie, pathStr);
            case Series -> loadSeries(movie, pathStr);
        }

        // Save the Movie
        movieRepo.save(movie);

        // Load the subtitles
        loadSubtitles(movie, pathStr);

        return List.of(movie);
    }

    @Override
    public void loadNewlyAdded(){
        loadNewlyAddedFromFolder(sharedProperties.getMoviesFolder());
    }

    @Override
    public void unloadNonExisting() {
        File home = new File(sharedProperties.getMoviesFolder());
        List<File> movieFolders = Arrays.stream(Objects.requireNonNull(home.listFiles()))
                .filter(File::isDirectory)
                .filter(f -> Objects.requireNonNull(f.listFiles()).length != 0)
                .toList();

        List<Movie> oldMovieRecords = movieRepo.findAll().stream()
                .filter(m -> movieFolders.stream()
                        .noneMatch(mf -> pathRelativizer
                                .apply(home.getAbsolutePath(), mf.getAbsolutePath())
                                .equals(m.getPath())
                                || isCollection(mf.getAbsolutePath())
                                && getFiles(mf.getAbsolutePath(), null, false).stream()
                                .anyMatch(mf1 -> pathRelativizer.apply(home.getAbsolutePath(), mf1.getAbsolutePath())
                                        .equals(m.getPath()))))
//                .filter(m -> !isCollection(m.getPath())
//                                || getFiles(m.getPath(), null, false).stream()
//                        .anyMatch(mf -> movieRepo.findByPath(pathRelativizer.apply(home.getAbsolutePath(), mf.getAbsolutePath())).isPresent()))
                .toList();
        movieRepo.deleteAll(oldMovieRecords);
        oldMovieRecords.stream()
                .filter(m -> m.getType().equals(Movie.MovieType.Series))
                .forEach(m -> episodeRepo.deleteBySeriesId(m.getId()));
        oldMovieRecords
                .forEach(m -> subtitleRepo.deleteByMovieId(m.getId()));
    }

    private void loadNewlyAddedFromFolder(String folderPath){
        File home = new File(folderPath);
        List<File> movieFolders = Arrays.stream(Objects.requireNonNull(home.listFiles()))
                .filter(File::isDirectory)
                .filter(f -> Objects.requireNonNull(f.listFiles()).length != 0)
                .toList();

        // Register new-found movies
        movieFolders.stream()
                .filter(f -> movieRepo                              // Have to give relativized path every time!
                        .findByPath(pathRelativizer
                                .apply(sharedProperties.getMoviesFolder(), f.getAbsolutePath()))
                        .isEmpty())
                .forEach(f -> {
                    if(isCollection(f.getAbsolutePath())){
                        loadNewlyAddedFromFolder(f.getAbsolutePath());
                        return;
                    }

                    load(Path.of(f.getAbsolutePath()));
                });
    }

    private void loadSubtitles(Movie movie, String absPath){
        getFilePaths(absPath, (dir, name) ->
                isSubtitleFile(name)
        , true).forEach(s -> {
            String fileName = Path.of(s).getFileName().toString();
            Subtitle subtitle = new Subtitle(movie.getId(), getSubtitleTypeByPath(movie, s), fileName.substring(0, fileName.lastIndexOf('.')), s);
            if(subtitle.getType().equals(Subtitle.SubtitleType.Episode))
                subtitle.setEpisodeId(getSubtitleEpisodeByPath(movie, s));

            subtitleRepo.save(subtitle);
        });
    }

    private void loadFilm(Movie movie, String absPath) {
        // Film path (MKV file)
        getFilePaths(absPath, (dir, name) ->
                !isTrailer(name) && isVideoFile(name)
        , true).stream().findFirst().ifPresentOrElse(movie::setFilmPath,
                () -> { throw new UnsupportedOperationException("Movie \"%s\" has no film file!".formatted(movie.getName())); });

        // Trailer path (if trailer is present)
        getFilePaths(absPath, (dir, name) ->
                isTrailer(name)
        , true).stream().findFirst().ifPresent(movie::setTrailerPath);
    }

    private void loadSeries(Movie movie, String absPath) {
        // Trailer path (if trailer is present)
        getFilePaths(absPath, (dir, name) ->
                        isTrailer(name)
                , true).stream().findFirst().ifPresent(movie::setTrailerPath);

        // Load episodes
        var episodePaths = getFilePaths(absPath, (dir, name) ->
                        !isTrailer(name) && isVideoFile(name)
                , true).stream().toList();

        episodePaths.forEach(episodePath -> {
            int seasonNumber = getSeasonNumber(episodePath);
            int episodeNumber = getEpisodeNumber(episodePath);

            Episode episode = new Episode(movie.getId(), seasonNumber, episodeNumber, episodePath);
            episodeRepo.save(episode);
        });
    }

    private int getSeasonNumber(String episodePath){
        String seasonEpisode = getMatcher(episodePath, "S\\d+E\\d+");

        if(seasonEpisode == null)
            return 0;

        String season = seasonEpisode.substring(seasonEpisode.indexOf("S") + 1, seasonEpisode.indexOf("E"));

        return Integer.parseInt(season);
    }

    private int getEpisodeNumber(String episodePath){
        String seasonEpisode = getMatcher(episodePath, "S\\d+E\\d+");

        if(seasonEpisode == null)
            return 0;

        String episode = seasonEpisode.substring(seasonEpisode.indexOf("E") + 1);

        return Integer.parseInt(episode);
    }

    private String getSubtitleEpisodeByPath(Movie movie, String pathStr){
        String subtitlePathNoFileExtension = pathStr.substring(0, pathStr.lastIndexOf("."));

        List<Episode> episodes = episodeRepo.findAllBySeriesId(movie.getId());
        String matcher = getMatcher(pathStr, "S\\d+E\\d+");

        // Return the id of an Episode that either has the same path as the subtitles (without the file extensions),
        // or check if the Episode's path and the Subtitle's path contain the same SddEdd pattern (have the same
        // season and episode numbers)
        return episodes.stream().filter(e -> e.getEpisodePath().substring(0, e.getEpisodePath().lastIndexOf(".")).equals(subtitlePathNoFileExtension) || matcher != null && e.getEpisodePath().contains(matcher))
                .findFirst()
                .orElseThrow()
                .getId();
    }

    private Subtitle.SubtitleType getSubtitleTypeByPath(Movie movie, String pathStr){
        Path path = Path.of(pathStr);
        String subtitlePathNoFileExtension = pathStr.substring(0, pathStr.lastIndexOf("."));

        if(movie.getTrailerPath() != null && movie.getTrailerPath().substring(0, movie.getTrailerPath().lastIndexOf(".")).equals(subtitlePathNoFileExtension))
            return Subtitle.SubtitleType.Trailer;

        if(movie.getType().equals(Movie.MovieType.Film) && movie.getFilmPath().substring(0, movie.getFilmPath().lastIndexOf(".")).equals(subtitlePathNoFileExtension))
            return Subtitle.SubtitleType.Film;

        List<Episode> episodes = episodeRepo.findAllBySeriesId(movie.getId());
        String matcher = getMatcher(pathStr, "S\\d+E\\d+");
        // Check if there's any Episode which has the same path as the subtitles (without the file extensions),
        // or if there's an Episode which path contains the same SddEdd pattern as the path of the subtitles (if the
        // season and episode numbers are the same)
        if(episodes.stream().anyMatch(e -> e.getEpisodePath().substring(0, e.getEpisodePath().lastIndexOf(".")).equals(subtitlePathNoFileExtension) || matcher != null && e.getEpisodePath().contains(matcher)))
            return Subtitle.SubtitleType.Episode;

        return Subtitle.SubtitleType.Undetermined;
    }

    private long getFilesSize(String path){
        return getFiles(path, null, true)
                .stream()
                .filter(File::isFile)
                .mapToLong(File::length)
                .sum();
    }

    private String getMatcher(String text, String... regexes){
        for(String regex : regexes){
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(text);

            if(matcher.find())
                return matcher.group();
        }

        return null;
    }

    private boolean isCollection(String path){
        var allFiles = getFiles(path, (dir, name) -> true, false);
        var dirs = allFiles.stream().filter(File::isDirectory).toList();
        long dirsCount = dirs.size();
        long moviesCount = getFiles(path, (dir, name) -> !isTrailer(name) && isVideoFile(name), false)
                .stream().filter(File::isFile).count();

        return dirsCount >= 1 && moviesCount == 0;
    }

    private boolean isSubtitleFile(String fileName){
        return Arrays.stream(subtitleFileExtensions).anyMatch(e -> fileName.endsWith(".%s".formatted(e)));
    }

    private boolean isVideoFile(String fileName){
        return fileName.endsWith(".mp4") || fileName.endsWith(".mkv") || fileName.endsWith(".avi");
    }

    private boolean isTrailer(String fileName){
        return fileName.equalsIgnoreCase("sample.mkv")
                || fileName.equalsIgnoreCase("sample.mp4")
                || fileName.equalsIgnoreCase("sample.avi");
    }

    private List<String> getFilePaths(String parentPath, FilenameFilter filenameFilter, boolean checkNestedFiles){
        return getFiles(parentPath, filenameFilter, checkNestedFiles).stream().map(f -> pathRelativizer.apply(parentPath, f.getAbsolutePath())).toList();
    }

    private List<File> getFiles(String parentPath, FilenameFilter filenameFilter, boolean checkNestedFiles){ // TODO Consider using Files::walk
        File file = new File(parentPath);

        var listFiles = file.listFiles(filenameFilter);
        if(listFiles == null)
            return List.of();

        List<File> result = new ArrayList<>(List.of(listFiles));

        if(!checkNestedFiles)
            return result;

        Arrays.stream(Objects.requireNonNull(file.listFiles())).filter(File::isDirectory).toList().forEach(e -> {
            result.addAll(getFiles(e.getAbsolutePath(), filenameFilter, checkNestedFiles).stream().toList());
        });

        return result;
    }
}
