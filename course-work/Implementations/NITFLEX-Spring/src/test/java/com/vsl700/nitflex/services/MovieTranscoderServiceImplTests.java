package com.vsl700.nitflex.services;

import com.vsl700.nitflex.components.SharedProperties;
import com.vsl700.nitflex.models.Episode;
import com.vsl700.nitflex.models.Movie;
import com.vsl700.nitflex.models.Subtitle;
import com.vsl700.nitflex.repo.EpisodeRepository;
import com.vsl700.nitflex.repo.MovieRepository;
import com.vsl700.nitflex.repo.SubtitleRepository;
import com.vsl700.nitflex.services.implementations.MovieTranscoderServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Disabled
@ExtendWith(MockitoExtension.class)
public class MovieTranscoderServiceImplTests {
    private MovieTranscoderServiceImpl movieTranscoderService;

    private MovieRepository movieRepo;
    private EpisodeRepository episodeRepo;
    private SubtitleRepository subtitleRepo;
    private SharedProperties sharedProperties;

    private ArrayList<Movie> movies;
    private ArrayList<Episode> episodes;
    private ArrayList<Subtitle> subtitles;

    @BeforeEach
    public void setUp(){
        movieRepo = mock(MovieRepository.class,
                Mockito.withSettings().useConstructor()
                        .defaultAnswer(CALLS_REAL_METHODS)
                        .strictness(Strictness.LENIENT));
        episodeRepo = mock(EpisodeRepository.class,
                Mockito.withSettings().useConstructor()
                        .defaultAnswer(CALLS_REAL_METHODS)
                        .strictness(Strictness.LENIENT));
        subtitleRepo = mock(SubtitleRepository.class,
                Mockito.withSettings().useConstructor()
                        .defaultAnswer(CALLS_REAL_METHODS)
                        .strictness(Strictness.LENIENT));
        sharedProperties = mock(SharedProperties.class,
                Mockito.withSettings().useConstructor()
                        .defaultAnswer(CALLS_REAL_METHODS)
                        .strictness(Strictness.LENIENT));

        movieTranscoderService = mock(MovieTranscoderServiceImpl.class,
                Mockito.withSettings().useConstructor(movieRepo, episodeRepo, subtitleRepo, sharedProperties)
                        .defaultAnswer(CALLS_REAL_METHODS)
                        .strictness(Strictness.LENIENT));

        movies = new ArrayList<>();
        doAnswer((invocation) -> {
            Movie movie = invocation.getArgument(0, Movie.class);
            if(movies.contains(movie))
                return movie;

            movie.setId("850fd9g9b90gibgf0dju9" + Math.random());
            movies.add(movie);
            return movie;
        }).when(movieRepo).save(any(Movie.class));

        doReturn(movies).when(movieRepo).findAll();

        doAnswer(invocation -> {
            String path = invocation.getArgument(0, String.class);

            return movies.stream().filter(m -> m.getPath().equals(path)).findFirst();
        }).when(movieRepo).findByPath(any(String.class));

        doAnswer(invocation -> {
            Iterable<Movie> moviesArg = invocation.getArgument(0);
            movies.removeIf(m -> StreamSupport.stream(moviesArg.spliterator(), false)
                    .anyMatch(m1 -> m1.getId().equals(m.getId())));

            return null;
        }).when(movieRepo).deleteAll(any());

        episodes = new ArrayList<>();
        when(episodeRepo.save(any())).then((invocation) -> {
            Episode episode = invocation.getArgument(0, Episode.class);
            if(episodes.contains(episode))
                return episode;

            episodes.add(episode);
            return episode;
        });

        doAnswer(invocation -> {
            String seriesId = invocation.getArgument(0, String.class);
            episodes.removeIf(e -> e.getSeriesId().equals(seriesId));

            return null;
        }).when(episodeRepo).deleteBySeriesId(any(String.class));

        when(episodeRepo.findAllBySeriesId(anyString())).then(invocation -> {
            String seriesId = invocation.getArgument(0, String.class);
            return episodes.stream().filter(e -> e.getSeriesId().equals(seriesId)).toList();
        });

        subtitles = new ArrayList<>();
        when(subtitleRepo.save(any())).then((invocation) -> {
            Subtitle subtitle = invocation.getArgument(0, Subtitle.class);
            if(subtitles.contains(subtitle))
                return subtitle;

            subtitles.add(subtitle);
            return subtitle;
        });

        doAnswer(invocation -> {
            String movieId = invocation.getArgument(0, String.class);
            subtitles.removeIf(s -> s.getMovieId().equals(movieId));

            return null;
        }).when(subtitleRepo).deleteByMovieId(any(String.class));

        when(subtitleRepo.findAllByMovieId(anyString())).then(invocation -> {
            String movieId = invocation.getArgument(0, String.class);
            return subtitles.stream().filter(s -> s.getMovieId().equals(movieId)).toList();
        });

        when(subtitleRepo.findByPath(anyString())).then(invocation -> {
            String path = invocation.getArgument(0, String.class);
            return subtitles.stream().filter(s -> s.getPath().equals(path)).findFirst();
        });

        when(sharedProperties.getMoviesFolder()).thenReturn("D:\\NITFLEX Tests\\nitflex");
        when(sharedProperties.isTranscodingEnabled()).thenReturn(true);
    }

    @Test
    public void film_withTrailer_Test(){
        Movie movie = new Movie("filmTrailer", Movie.MovieType.Film, "filmTrailer", 10L);
        movie.setFilmPath("2024-01-28 14-35-19.mkv");
        movie.setTrailerPath("sample.mkv");
        movieRepo.save(movie);

        movieTranscoderService.transcode(movie);

        Assertions.assertAll(() -> {
            assertThat(movie.isTranscoded()).isTrue();
            assertThat(movie.getTrailerPath()).isEqualTo("sample");
            assertThat(movie.getFilmPath()).isEqualTo("2024-01-28 14-35-19");
            assertThat(movie.getSize()).isEqualTo(417184L);
        });
    }

    @Test
    public void film_withTrailer_MPDValidation_Test() throws IOException {
        Movie movie = new Movie("filmTrailerMPDCheck", Movie.MovieType.Film, "filmTrailerMPDCheck", 10L);
        movie.setFilmPath("2024-01-28 14-35-19.mkv");
        movie.setTrailerPath("sample.mkv");
        movieRepo.save(movie);

        movieTranscoderService.transcode(movie);

        String filmManifestContent = Files.readString(Path.of("D:\\NITFLEX Tests\\nitflex\\filmTrailer\\2024-01-28 14-35-19\\manifest.mpd"));
        String trailerManifestContent = Files.readString(Path.of("D:\\NITFLEX Tests\\nitflex\\filmTrailer\\sample\\manifest.mpd"));

        Assertions.assertAll(() -> {
            assertThat(countWordOccurrences(filmManifestContent, "initialization=\"init-$RepresentationID$.m4s\"")).isEqualTo(2);
            assertThat(countWordOccurrences(filmManifestContent, "media=\"chunk-$RepresentationID$-$Number%05d$.m4s\"")).isEqualTo(2);
            assertThat(countWordOccurrences(trailerManifestContent, "initialization=\"init-$RepresentationID$.m4s\"")).isEqualTo(2);
            assertThat(countWordOccurrences(trailerManifestContent, "media=\"chunk-$RepresentationID$-$Number%05d$.m4s\"")).isEqualTo(2);
        });
    }

    @Test
    public void film_withSubtitles_Test(){
        Movie movie = new Movie("filmSubtitles", Movie.MovieType.Film, "filmSubtitles", 10L);
        movie.setFilmPath("2024-01-28 14-35-19.mkv");
        movieRepo.save(movie);

        Subtitle nestedSubtitle1 = new Subtitle(movie.getId(), Subtitle.SubtitleType.Undetermined, "English", "Subs\\English.srt");
        Subtitle nestedSubtitle2 = new Subtitle(movie.getId(), Subtitle.SubtitleType.Undetermined, "French", "Subs\\French.srt");
        Subtitle nestedSubtitle3 = new Subtitle(movie.getId(), Subtitle.SubtitleType.Undetermined, "Chinese", "Subs\\Chinese.srt");
        Subtitle nestedSubtitle4 = new Subtitle(movie.getId(), Subtitle.SubtitleType.Undetermined, "hacker-din", "Subs2\\hacker-din.srt");
        Subtitle nestedSubtitle5 = new Subtitle(movie.getId(), Subtitle.SubtitleType.Undetermined, "hacker-din_eng", "Subs2\\hacker-din_eng.srt");
        Subtitle subtitle = new Subtitle(movie.getId(), Subtitle.SubtitleType.Film, "2024-01-28 14-35-19", "2024-01-28 14-35-19.srt");
        subtitleRepo.save(nestedSubtitle1);
        subtitleRepo.save(nestedSubtitle2);
        subtitleRepo.save(nestedSubtitle3);
        subtitleRepo.save(nestedSubtitle4);
        subtitleRepo.save(nestedSubtitle5);
        subtitleRepo.save(subtitle);

        movieTranscoderService.transcode(movie);

        Assertions.assertAll(() -> {
            assertThat(movie.isTranscoded()).isTrue();
            assertThat(movie.getTrailerPath()).isNull();
            assertThat(movie.getFilmPath()).isEqualTo("2024-01-28 14-35-19");
            assertThat(movie.getSize()).isEqualTo(937247L);

            //assertThat(subtitles.size()).isEqualTo(6); // Checking the mock method

            assertThat(nestedSubtitle1.getPath()).isEqualTo("Subs\\English.vtt");
            assertThat(nestedSubtitle2.getPath()).isEqualTo("Subs\\French.vtt");
            assertThat(nestedSubtitle3.getPath()).isEqualTo("Subs\\Chinese.vtt");
            assertThat(nestedSubtitle4.getPath()).isEqualTo("Subs2\\hacker-din.vtt");
            assertThat(nestedSubtitle5.getPath()).isEqualTo("Subs2\\hacker-din_eng.vtt");
            assertThat(subtitle.getPath()).isEqualTo("2024-01-28 14-35-19.vtt");
        });
    }

    @Test
    public void film_withTrailerAndSubtitles_Test(){
        Movie movie = new Movie("filmTrailerSubtitles", Movie.MovieType.Film, "filmTrailerSubtitles", 10L);
        movie.setFilmPath("2024-01-28 14-35-19.mkv");
        movie.setTrailerPath("sample.mkv");
        movieRepo.save(movie);

        Subtitle nestedSubtitle1 = new Subtitle(movie.getId(), Subtitle.SubtitleType.Undetermined, "English", "Subs\\English.srt");
        Subtitle nestedSubtitle2 = new Subtitle(movie.getId(), Subtitle.SubtitleType.Undetermined, "French", "Subs\\French.srt");
        Subtitle nestedSubtitle3 = new Subtitle(movie.getId(), Subtitle.SubtitleType.Undetermined, "Chinese", "Subs\\Chinese.srt");
        Subtitle nestedSubtitle4 = new Subtitle(movie.getId(), Subtitle.SubtitleType.Undetermined, "hacker-din", "Subs2\\hacker-din.srt");
        Subtitle nestedSubtitle5 = new Subtitle(movie.getId(), Subtitle.SubtitleType.Undetermined, "hacker-din_eng", "Subs2\\hacker-din_eng.srt");
        Subtitle subtitle = new Subtitle(movie.getId(), Subtitle.SubtitleType.Film, "2024-01-28 14-35-19", "2024-01-28 14-35-19.srt");
        subtitleRepo.save(nestedSubtitle1);
        subtitleRepo.save(nestedSubtitle2);
        subtitleRepo.save(nestedSubtitle3);
        subtitleRepo.save(nestedSubtitle4);
        subtitleRepo.save(nestedSubtitle5);
        subtitleRepo.save(subtitle);

        movieTranscoderService.transcode(movie);

        Assertions.assertAll(() -> {
            assertThat(movie.isTranscoded()).isTrue();
            assertThat(movie.getTrailerPath()).isEqualTo("sample");
            assertThat(movie.getFilmPath()).isEqualTo("2024-01-28 14-35-19");
            assertThat(movie.getSize()).isEqualTo(1145839L);

            assertThat(nestedSubtitle1.getPath()).isEqualTo("Subs\\English.vtt");
            assertThat(nestedSubtitle2.getPath()).isEqualTo("Subs\\French.vtt");
            assertThat(nestedSubtitle3.getPath()).isEqualTo("Subs\\Chinese.vtt");
            assertThat(nestedSubtitle4.getPath()).isEqualTo("Subs2\\hacker-din.vtt");
            assertThat(nestedSubtitle5.getPath()).isEqualTo("Subs2\\hacker-din_eng.vtt");
            assertThat(subtitle.getPath()).isEqualTo("2024-01-28 14-35-19.vtt");
        });
    }

    @Test
    public void series_Test(){
        Movie movie = new Movie("series", Movie.MovieType.Series, "series", 10L);
        movieRepo.save(movie);

        Episode episode1 = new Episode(movie.getId(), 0, 0, "S01E01.mkv");
        Episode episode2 = new Episode(movie.getId(), 0, 0, "S01E02.mkv");
        Episode episode3 = new Episode(movie.getId(), 0, 0, "S01E03.mkv");
        Episode episode4 = new Episode(movie.getId(), 0, 0, "S02E01.mkv");
        Episode episode5 = new Episode(movie.getId(), 0, 0, "S02E02.mkv");
        episodeRepo.save(episode1);
        episodeRepo.save(episode2);
        episodeRepo.save(episode3);
        episodeRepo.save(episode4);
        episodeRepo.save(episode5);

        movieTranscoderService.transcode(movie);

        Assertions.assertAll(() -> {
            assertThat(movie.isTranscoded()).isTrue();
            assertThat(movie.getTrailerPath()).isNull();
            assertThat(movie.getFilmPath()).isNull();
            assertThat(movie.getSize()).isEqualTo(1047056L);

            assertThat(episode1.getEpisodePath()).isEqualTo("S01E01");
            assertThat(episode2.getEpisodePath()).isEqualTo("S01E02");
            assertThat(episode3.getEpisodePath()).isEqualTo("S01E03");
            assertThat(episode4.getEpisodePath()).isEqualTo("S02E01");
            assertThat(episode5.getEpisodePath()).isEqualTo("S02E02");
        });
    }

    @Test
    public void series_withTrailer_Test(){
        Movie movie = new Movie("seriesTrailer", Movie.MovieType.Series, "seriesTrailer", 10L);
        movie.setTrailerPath("sample.mkv");
        movieRepo.save(movie);

        Episode episode1 = new Episode(movie.getId(), 0, 0, "S01E01.mkv");
        Episode episode2 = new Episode(movie.getId(), 0, 0, "S01E02.mkv");
        Episode episode3 = new Episode(movie.getId(), 0, 0, "S01E03.mkv");
        Episode episode4 = new Episode(movie.getId(), 0, 0, "S02E01.mkv");
        Episode episode5 = new Episode(movie.getId(), 0, 0, "S02E02.mkv");
        episodeRepo.save(episode1);
        episodeRepo.save(episode2);
        episodeRepo.save(episode3);
        episodeRepo.save(episode4);
        episodeRepo.save(episode5);

        movieTranscoderService.transcode(movie);

        Assertions.assertAll(() -> {
            assertThat(movie.isTranscoded()).isTrue();
            assertThat(movie.getTrailerPath()).isEqualTo("sample");
            assertThat(movie.getFilmPath()).isNull();
            assertThat(movie.getSize()).isEqualTo(1255648L);

            assertThat(episode1.getEpisodePath()).isEqualTo("S01E01");
            assertThat(episode2.getEpisodePath()).isEqualTo("S01E02");
            assertThat(episode3.getEpisodePath()).isEqualTo("S01E03");
            assertThat(episode4.getEpisodePath()).isEqualTo("S02E01");
            assertThat(episode5.getEpisodePath()).isEqualTo("S02E02");
        });
    }

    @Test
    public void series_diffSeasonFolders_withTrailer_Test(){
        Movie movie = new Movie("seriesTrailerDiffSeasonFolders", Movie.MovieType.Series, "seriesTrailerDiffSeasonFolders", 10L);
        movie.setTrailerPath("sample.mkv");
        movieRepo.save(movie);

        Episode episode1 = new Episode(movie.getId(), 0, 0, "Season1\\S01E01.mkv");
        Episode episode2 = new Episode(movie.getId(), 0, 0, "Season1\\S01E02.mkv");
        Episode episode3 = new Episode(movie.getId(), 0, 0, "Season1\\S01E03.mkv");
        Episode episode4 = new Episode(movie.getId(), 0, 0, "Season2\\S02E01.mkv");
        Episode episode5 = new Episode(movie.getId(), 0, 0, "Season2\\S02E02.mkv");
        episodeRepo.save(episode1);
        episodeRepo.save(episode2);
        episodeRepo.save(episode3);
        episodeRepo.save(episode4);
        episodeRepo.save(episode5);

        movieTranscoderService.transcode(movie);

        Assertions.assertAll(() -> {
            assertThat(movie.isTranscoded()).isTrue();
            assertThat(movie.getTrailerPath()).isEqualTo("sample");
            assertThat(movie.getFilmPath()).isNull();
            assertThat(movie.getSize()).isEqualTo(1251552L);

            assertThat(episode1.getEpisodePath()).isEqualTo("Season1\\S01E01");
            assertThat(episode2.getEpisodePath()).isEqualTo("Season1\\S01E02");
            assertThat(episode3.getEpisodePath()).isEqualTo("Season1\\S01E03");
            assertThat(episode4.getEpisodePath()).isEqualTo("Season2\\S02E01");
            assertThat(episode5.getEpisodePath()).isEqualTo("Season2\\S02E02");
        });
    }

    @Test
    public void series_withSubtitles_Test(){
        Movie movie = new Movie("seriesSubtitles", Movie.MovieType.Series, "seriesSubtitles", 10L);
        movieRepo.save(movie);

        Episode episode1 = new Episode(movie.getId(), 0, 0, "S01E01.mkv");
        Episode episode2 = new Episode(movie.getId(), 0, 0, "S01E02.mkv");
        Episode episode3 = new Episode(movie.getId(), 0, 0, "S01E03.mkv");
        Episode episode4 = new Episode(movie.getId(), 0, 0, "S02E01.mkv");
        Episode episode5 = new Episode(movie.getId(), 0, 0, "S02E02.mkv");
        episodeRepo.save(episode1);
        episodeRepo.save(episode2);
        episodeRepo.save(episode3);
        episodeRepo.save(episode4);
        episodeRepo.save(episode5);

        Subtitle subtitle1 = new Subtitle(movie.getId(), Subtitle.SubtitleType.Episode, "S01E01", "S01E01.srt");
        Subtitle subtitle2 = new Subtitle(movie.getId(), Subtitle.SubtitleType.Episode, "S01E02", "S01E02.srt");
        Subtitle subtitle3 = new Subtitle(movie.getId(), Subtitle.SubtitleType.Episode, "S01E03", "S01E03.srt");
        Subtitle subtitle4 = new Subtitle(movie.getId(), Subtitle.SubtitleType.Episode, "S02E01", "S02E01.srt");
        Subtitle subtitle5 = new Subtitle(movie.getId(), Subtitle.SubtitleType.Episode, "S02E02", "S02E02.srt");
        subtitleRepo.save(subtitle1);
        subtitleRepo.save(subtitle2);
        subtitleRepo.save(subtitle3);
        subtitleRepo.save(subtitle4);
        subtitleRepo.save(subtitle5);

        movieTranscoderService.transcode(movie);

        Assertions.assertAll(() -> {
            assertThat(movie.isTranscoded()).isTrue();
            assertThat(movie.getTrailerPath()).isNull();
            assertThat(movie.getFilmPath()).isNull();
            assertThat(movie.getSize()).isEqualTo(1666556L);

            assertThat(episode1.getEpisodePath()).isEqualTo("S01E01");
            assertThat(episode2.getEpisodePath()).isEqualTo("S01E02");
            assertThat(episode3.getEpisodePath()).isEqualTo("S01E03");
            assertThat(episode4.getEpisodePath()).isEqualTo("S02E01");
            assertThat(episode5.getEpisodePath()).isEqualTo("S02E02");

            assertThat(subtitle1.getPath()).isEqualTo("S01E01.vtt");
            assertThat(subtitle2.getPath()).isEqualTo("S01E02.vtt");
            assertThat(subtitle3.getPath()).isEqualTo("S01E03.vtt");
            assertThat(subtitle4.getPath()).isEqualTo("S02E01.vtt");
            assertThat(subtitle5.getPath()).isEqualTo("S02E02.vtt");
        });
    }

    @Test
    public void series_withTrailerAndSubtitles_Test(){
        Movie movie = new Movie("seriesTrailerSubtitles", Movie.MovieType.Series, "seriesTrailerSubtitles", 10L);
        movie.setTrailerPath("sample.mkv");
        movieRepo.save(movie);

        Episode episode1 = new Episode(movie.getId(), 0, 0, "S01E01.mkv");
        Episode episode2 = new Episode(movie.getId(), 0, 0, "S01E02.mkv");
        Episode episode3 = new Episode(movie.getId(), 0, 0, "S01E03.mkv");
        Episode episode4 = new Episode(movie.getId(), 0, 0, "S02E01.mkv");
        Episode episode5 = new Episode(movie.getId(), 0, 0, "S02E02.mkv");
        episodeRepo.save(episode1);
        episodeRepo.save(episode2);
        episodeRepo.save(episode3);
        episodeRepo.save(episode4);
        episodeRepo.save(episode5);

        Subtitle subtitle1 = new Subtitle(movie.getId(), Subtitle.SubtitleType.Episode, "S01E01", "S01E01.srt");
        Subtitle subtitle2 = new Subtitle(movie.getId(), Subtitle.SubtitleType.Episode, "S01E02", "S01E02.srt");
        Subtitle subtitle3 = new Subtitle(movie.getId(), Subtitle.SubtitleType.Episode, "S01E03", "S01E03.srt");
        Subtitle subtitle4 = new Subtitle(movie.getId(), Subtitle.SubtitleType.Episode, "S02E01", "S02E01.srt");
        Subtitle subtitle5 = new Subtitle(movie.getId(), Subtitle.SubtitleType.Episode, "S02E02", "S02E02.srt");
        subtitleRepo.save(subtitle1);
        subtitleRepo.save(subtitle2);
        subtitleRepo.save(subtitle3);
        subtitleRepo.save(subtitle4);
        subtitleRepo.save(subtitle5);

        movieTranscoderService.transcode(movie);

        Assertions.assertAll(() -> {
            assertThat(movie.isTranscoded()).isTrue();
            assertThat(movie.getTrailerPath()).isEqualTo("sample");
            assertThat(movie.getFilmPath()).isNull();
            assertThat(movie.getSize()).isEqualTo(1875148L);

            assertThat(episode1.getEpisodePath()).isEqualTo("S01E01");
            assertThat(episode2.getEpisodePath()).isEqualTo("S01E02");
            assertThat(episode3.getEpisodePath()).isEqualTo("S01E03");
            assertThat(episode4.getEpisodePath()).isEqualTo("S02E01");
            assertThat(episode5.getEpisodePath()).isEqualTo("S02E02");

            assertThat(subtitle1.getPath()).isEqualTo("S01E01.vtt");
            assertThat(subtitle2.getPath()).isEqualTo("S01E02.vtt");
            assertThat(subtitle3.getPath()).isEqualTo("S01E03.vtt");
            assertThat(subtitle4.getPath()).isEqualTo("S02E01.vtt");
            assertThat(subtitle5.getPath()).isEqualTo("S02E02.vtt");
        });
    }

    @Test
    public void series_withTrailerAndSubtitles_Test2(){
        Movie movie = new Movie("seriesTrailerSubtitles2", Movie.MovieType.Series, "seriesTrailerSubtitles2", 10L);
        movie.setTrailerPath("sample.mkv");
        movieRepo.save(movie);

        Episode episode1 = new Episode(movie.getId(), 0, 0, "S01E01.mkv");
        Episode episode2 = new Episode(movie.getId(), 0, 0, "S01E02.mkv");
        Episode episode3 = new Episode(movie.getId(), 0, 0, "S01E03.mkv");
        Episode episode4 = new Episode(movie.getId(), 0, 0, "S02E01.mkv");
        Episode episode5 = new Episode(movie.getId(), 0, 0, "S02E02.mkv");
        episodeRepo.save(episode1);
        episodeRepo.save(episode2);
        episodeRepo.save(episode3);
        episodeRepo.save(episode4);
        episodeRepo.save(episode5);

        Subtitle subtitle1 = new Subtitle(movie.getId(), Subtitle.SubtitleType.Episode, "S01E01", "S01E01.srt");
        Subtitle subtitle2 = new Subtitle(movie.getId(), Subtitle.SubtitleType.Episode, "S01E02", "S01E02.srt");
        Subtitle subtitle3 = new Subtitle(movie.getId(), Subtitle.SubtitleType.Episode, "S01E03", "S01E03.srt");
        Subtitle subtitle4 = new Subtitle(movie.getId(), Subtitle.SubtitleType.Episode, "S02E01", "S02E01.srt");
        Subtitle subtitle5 = new Subtitle(movie.getId(), Subtitle.SubtitleType.Episode, "S02E02", "S02E02.srt");
        Subtitle trailerSubtitle = new Subtitle(movie.getId(), Subtitle.SubtitleType.Trailer, "sample", "sample.srt");
        subtitleRepo.save(subtitle1);
        subtitleRepo.save(subtitle2);
        subtitleRepo.save(subtitle3);
        subtitleRepo.save(subtitle4);
        subtitleRepo.save(subtitle5);
        subtitleRepo.save(trailerSubtitle);

        movieTranscoderService.transcode(movie);

        Assertions.assertAll(() -> {
            assertThat(movie.isTranscoded()).isTrue();
            assertThat(movie.getTrailerPath()).isEqualTo("sample");
            assertThat(movie.getFilmPath()).isNull();
            assertThat(movie.getSize()).isEqualTo(1999048L);

            assertThat(episode1.getEpisodePath()).isEqualTo("S01E01");
            assertThat(episode2.getEpisodePath()).isEqualTo("S01E02");
            assertThat(episode3.getEpisodePath()).isEqualTo("S01E03");
            assertThat(episode4.getEpisodePath()).isEqualTo("S02E01");
            assertThat(episode5.getEpisodePath()).isEqualTo("S02E02");

            assertThat(subtitle1.getPath()).isEqualTo("S01E01.vtt");
            assertThat(subtitle2.getPath()).isEqualTo("S01E02.vtt");
            assertThat(subtitle3.getPath()).isEqualTo("S01E03.vtt");
            assertThat(subtitle4.getPath()).isEqualTo("S02E01.vtt");
            assertThat(subtitle5.getPath()).isEqualTo("S02E02.vtt");
            assertThat(trailerSubtitle.getPath()).isEqualTo("sample.vtt");
        });
    }

    @Test
    public void film_withTrailerAndInternalSubtitles_Test(){
        Movie movie = new Movie("filmTrailerInternalSubtitles", Movie.MovieType.Film, "filmTrailerInternalSubtitles", 10L);
        movie.setFilmPath("output_video_eng_subs.mp4");
        movie.setTrailerPath("sample.mkv");
        movieRepo.save(movie);

        movieTranscoderService.transcode(movie);

        Subtitle extractedSubtitle = subtitles.stream().findFirst().orElseThrow();

        Assertions.assertAll(() -> {
            assertThat(movie.isTranscoded()).isTrue();
            assertThat(movie.getTrailerPath()).isEqualTo("sample");
            assertThat(movie.getFilmPath()).isEqualTo("output_video_eng_subs");
            assertThat(movie.getSize()).isEqualTo(421354L);

            assertThat(subtitles.size()).isEqualTo(1);

            assertThat(extractedSubtitle.getMovieId()).isEqualTo(movie.getId());
            assertThat(extractedSubtitle.getType()).isEqualTo(Subtitle.SubtitleType.Film);
            assertThat(extractedSubtitle.getPath()).isEqualTo("output_video_eng_subs\\extracted_subtitles_0.vtt");
            assertThat(extractedSubtitle.getName()).isEqualTo("Extracted (0)");
        });
    }

    @Test
    public void series_withTrailerAndInternalSubtitles_Test(){
        Movie movie = new Movie("seriesTrailerInternalSubtitles", Movie.MovieType.Series, "seriesTrailerInternalSubtitles", 10L);
        movie.setTrailerPath("sample.mkv");
        movieRepo.save(movie);

        Episode episode1 = new Episode(movie.getId(), 0, 0, "output_video_eng_subs.mp4");
        Episode episode2 = new Episode(movie.getId(), 0, 0, "output_video_2_eng_subs.mp4");
        episodeRepo.save(episode1);
        episodeRepo.save(episode2);

        movieTranscoderService.transcode(movie);

        Subtitle extractedSubtitle1 = subtitles.get(0);
        Subtitle extractedSubtitle2 = subtitles.get(1);
        Subtitle extractedSubtitle3 = subtitles.get(2);

        Assertions.assertAll(() -> {
            assertThat(movie.isTranscoded()).isTrue();
            assertThat(movie.getTrailerPath()).isEqualTo("sample");
            assertThat(movie.getSize()).isEqualTo(638264L);

            assertThat(episode1.getEpisodePath()).isEqualTo("output_video_eng_subs");
            assertThat(episode2.getEpisodePath()).isEqualTo("output_video_2_eng_subs");

            assertThat(subtitles.size()).isEqualTo(3);

            assertThat(extractedSubtitle1.getMovieId()).isEqualTo(movie.getId());
            assertThat(extractedSubtitle1.getEpisodeId()).isEqualTo(episode1.getId());
            assertThat(extractedSubtitle1.getType()).isEqualTo(Subtitle.SubtitleType.Episode);
            assertThat(extractedSubtitle1.getPath()).isEqualTo("output_video_eng_subs\\extracted_subtitles_0.vtt");
            assertThat(extractedSubtitle1.getName()).isEqualTo("Extracted (0)");

            assertThat(extractedSubtitle2.getMovieId()).isEqualTo(movie.getId());
            assertThat(extractedSubtitle2.getEpisodeId()).isEqualTo(episode2.getId());
            assertThat(extractedSubtitle2.getType()).isEqualTo(Subtitle.SubtitleType.Episode);
            assertThat(extractedSubtitle2.getPath()).isEqualTo("output_video_2_eng_subs\\extracted_subtitles_0.vtt");
            assertThat(extractedSubtitle2.getName()).isEqualTo("Extracted (0)");

            assertThat(extractedSubtitle3.getMovieId()).isEqualTo(movie.getId());
            assertThat(extractedSubtitle3.getEpisodeId()).isEqualTo(episode2.getId());
            assertThat(extractedSubtitle3.getType()).isEqualTo(Subtitle.SubtitleType.Episode);
            assertThat(extractedSubtitle3.getPath()).isEqualTo("output_video_2_eng_subs\\extracted_subtitles_1.vtt");
            assertThat(extractedSubtitle3.getName()).isEqualTo("Extracted (1)");
        });
    }

    @Test
    public void film_withTrailerAndInternalExternalSubtitles_Test(){
        Movie movie = new Movie("filmTrailerInternalExternalSubtitles", Movie.MovieType.Film, "filmTrailerInternalExternalSubtitles", 10L);
        movie.setFilmPath("output_video_2_eng_subs.mp4");
        movie.setTrailerPath("sample.mkv");
        movieRepo.save(movie);

        Subtitle nestedSubtitle1 = new Subtitle(movie.getId(), Subtitle.SubtitleType.Undetermined, "English", "Subs\\English.srt");
        Subtitle nestedSubtitle2 = new Subtitle(movie.getId(), Subtitle.SubtitleType.Undetermined, "French", "Subs\\French.srt");
        Subtitle nestedSubtitle3 = new Subtitle(movie.getId(), Subtitle.SubtitleType.Undetermined, "Chinese", "Subs\\Chinese.srt");
        Subtitle nestedSubtitle4 = new Subtitle(movie.getId(), Subtitle.SubtitleType.Undetermined, "hacker-din", "Subs2\\hacker-din.srt");
        Subtitle nestedSubtitle5 = new Subtitle(movie.getId(), Subtitle.SubtitleType.Undetermined, "hacker-din_eng", "Subs2\\hacker-din_eng.srt");
        Subtitle subtitle = new Subtitle(movie.getId(), Subtitle.SubtitleType.Film, "2024-01-28 14-35-19", "2024-01-28 14-35-19.srt");
        subtitleRepo.save(nestedSubtitle1);
        subtitleRepo.save(nestedSubtitle2);
        subtitleRepo.save(nestedSubtitle3);
        subtitleRepo.save(nestedSubtitle4);
        subtitleRepo.save(nestedSubtitle5);
        subtitleRepo.save(subtitle);

        movieTranscoderService.transcode(movie);

        Assertions.assertAll(() -> {
            assertThat(movie.isTranscoded()).isTrue();
            assertThat(movie.getTrailerPath()).isEqualTo("sample");
            assertThat(movie.getFilmPath()).isEqualTo("output_video_2_eng_subs");
            assertThat(movie.getSize()).isEqualTo(1150061L);

            assertThat(nestedSubtitle1.getPath()).isEqualTo("Subs\\English.vtt");
            assertThat(nestedSubtitle2.getPath()).isEqualTo("Subs\\French.vtt");
            assertThat(nestedSubtitle3.getPath()).isEqualTo("Subs\\Chinese.vtt");
            assertThat(nestedSubtitle4.getPath()).isEqualTo("Subs2\\hacker-din.vtt");
            assertThat(nestedSubtitle5.getPath()).isEqualTo("Subs2\\hacker-din_eng.vtt");
            assertThat(subtitle.getPath()).isEqualTo("2024-01-28 14-35-19.vtt");
        });
    }

    @Test
    public void series_withTrailerAndInternalExternalSubtitles_Test(){
        Movie movie = new Movie("seriesTrailerInternalExternalSubtitles", Movie.MovieType.Series, "seriesTrailerInternalExternalSubtitles", 10L);
        movie.setTrailerPath("sample.mkv");
        movieRepo.save(movie);

        Episode episode1 = new Episode(movie.getId(), 0, 0, "output_video_eng_subs.mp4");
        Episode episode2 = new Episode(movie.getId(), 0, 0, "output_video_2_eng_subs.mp4");
        episodeRepo.save(episode1);
        episodeRepo.save(episode2);

        Subtitle nestedSubtitle1 = new Subtitle(movie.getId(), Subtitle.SubtitleType.Undetermined, "English", "Subs\\English.srt");
        Subtitle nestedSubtitle2 = new Subtitle(movie.getId(), Subtitle.SubtitleType.Undetermined, "French", "Subs\\French.srt");
        Subtitle nestedSubtitle3 = new Subtitle(movie.getId(), Subtitle.SubtitleType.Undetermined, "Chinese", "Subs\\Chinese.srt");
        Subtitle nestedSubtitle4 = new Subtitle(movie.getId(), Subtitle.SubtitleType.Undetermined, "hacker-din", "Subs2\\hacker-din.srt");
        Subtitle nestedSubtitle5 = new Subtitle(movie.getId(), Subtitle.SubtitleType.Undetermined, "hacker-din_eng", "Subs2\\hacker-din_eng.srt");
        Subtitle subtitle = new Subtitle(movie.getId(), Subtitle.SubtitleType.Undetermined, "2024-01-28 14-35-19", "2024-01-28 14-35-19.srt");
        subtitleRepo.save(nestedSubtitle1);
        subtitleRepo.save(nestedSubtitle2);
        subtitleRepo.save(nestedSubtitle3);
        subtitleRepo.save(nestedSubtitle4);
        subtitleRepo.save(nestedSubtitle5);
        subtitleRepo.save(subtitle);

        movieTranscoderService.transcode(movie);

        Subtitle extractedSubtitle1 = subtitles.get(6);
        Subtitle extractedSubtitle2 = subtitles.get(7);
        Subtitle extractedSubtitle3 = subtitles.get(8);

        Assertions.assertAll(() -> {
            assertThat(movie.isTranscoded()).isTrue();
            assertThat(movie.getFilmPath()).isNull();
            assertThat(movie.getTrailerPath()).isEqualTo("sample");
            assertThat(movie.getSize()).isEqualTo(1362823L);

            assertThat(episode1.getEpisodePath()).isEqualTo("output_video_eng_subs");
            assertThat(episode2.getEpisodePath()).isEqualTo("output_video_2_eng_subs");

            assertThat(subtitles.size()).isEqualTo(9);

            assertThat(extractedSubtitle1.getMovieId()).isEqualTo(movie.getId());
            assertThat(extractedSubtitle1.getEpisodeId()).isEqualTo(episode1.getId());
            assertThat(extractedSubtitle1.getType()).isEqualTo(Subtitle.SubtitleType.Episode);
            assertThat(extractedSubtitle1.getPath()).isEqualTo("output_video_eng_subs\\extracted_subtitles_0.vtt");
            assertThat(extractedSubtitle1.getName()).isEqualTo("Extracted (0)");

            assertThat(extractedSubtitle2.getMovieId()).isEqualTo(movie.getId());
            assertThat(extractedSubtitle2.getEpisodeId()).isEqualTo(episode2.getId());
            assertThat(extractedSubtitle2.getType()).isEqualTo(Subtitle.SubtitleType.Episode);
            assertThat(extractedSubtitle2.getPath()).isEqualTo("output_video_2_eng_subs\\extracted_subtitles_0.vtt");
            assertThat(extractedSubtitle2.getName()).isEqualTo("Extracted (0)");

            assertThat(extractedSubtitle3.getMovieId()).isEqualTo(movie.getId());
            assertThat(extractedSubtitle3.getEpisodeId()).isEqualTo(episode2.getId());
            assertThat(extractedSubtitle3.getType()).isEqualTo(Subtitle.SubtitleType.Episode);
            assertThat(extractedSubtitle3.getPath()).isEqualTo("output_video_2_eng_subs\\extracted_subtitles_1.vtt");
            assertThat(extractedSubtitle3.getName()).isEqualTo("Extracted (1)");

            assertThat(nestedSubtitle1.getPath()).isEqualTo("Subs\\English.vtt");
            assertThat(nestedSubtitle2.getPath()).isEqualTo("Subs\\French.vtt");
            assertThat(nestedSubtitle3.getPath()).isEqualTo("Subs\\Chinese.vtt");
            assertThat(nestedSubtitle4.getPath()).isEqualTo("Subs2\\hacker-din.vtt");
            assertThat(nestedSubtitle5.getPath()).isEqualTo("Subs2\\hacker-din_eng.vtt");
            assertThat(subtitle.getPath()).isEqualTo("2024-01-28 14-35-19.vtt");
        });
    }

    @Test
    public void series_withTrailerAndInternalExternalSubtitles_Test2(){
        Movie movie = new Movie("seriesTrailerInternalExternalSubtitles2", Movie.MovieType.Series, "seriesTrailerInternalExternalSubtitles2", 10L);
        movie.setTrailerPath("sample.mkv");
        movieRepo.save(movie);

        Episode episode1 = new Episode(movie.getId(), 0, 0, "output_video_eng_subs.mp4");
        Episode episode2 = new Episode(movie.getId(), 0, 0, "output_video_2_eng_subs.mp4");
        episodeRepo.save(episode1);
        episodeRepo.save(episode2);

        Subtitle nestedSubtitle1 = new Subtitle(movie.getId(), Subtitle.SubtitleType.Undetermined, "English", "Subs\\English.srt");
        Subtitle nestedSubtitle2 = new Subtitle(movie.getId(), Subtitle.SubtitleType.Undetermined, "French", "Subs\\French.srt");
        Subtitle nestedSubtitle3 = new Subtitle(movie.getId(), Subtitle.SubtitleType.Undetermined, "Chinese", "Subs\\Chinese.srt");
        Subtitle nestedSubtitle4 = new Subtitle(movie.getId(), Subtitle.SubtitleType.Undetermined, "hacker-din", "Subs2\\hacker-din.srt");
        Subtitle nestedSubtitle5 = new Subtitle(movie.getId(), Subtitle.SubtitleType.Undetermined, "hacker-din_eng", "Subs2\\hacker-din_eng.srt");
        Subtitle subtitle = new Subtitle(movie.getId(), Subtitle.SubtitleType.Undetermined, "2024-01-28 14-35-19", "2024-01-28 14-35-19.srt");
        Subtitle trailerSubtitle = new Subtitle(movie.getId(), Subtitle.SubtitleType.Undetermined, "sample", "sample.srt");
        subtitleRepo.save(nestedSubtitle1);
        subtitleRepo.save(nestedSubtitle2);
        subtitleRepo.save(nestedSubtitle3);
        subtitleRepo.save(nestedSubtitle4);
        subtitleRepo.save(nestedSubtitle5);
        subtitleRepo.save(subtitle);
        subtitleRepo.save(trailerSubtitle);

        movieTranscoderService.transcode(movie);

        Subtitle extractedSubtitle1 = subtitles.get(7);
        Subtitle extractedSubtitle2 = subtitles.get(8);
        Subtitle extractedSubtitle3 = subtitles.get(9);

        Assertions.assertAll(() -> {
            assertThat(movie.isTranscoded()).isTrue();
            assertThat(movie.getFilmPath()).isNull();
            assertThat(movie.getTrailerPath()).isEqualTo("sample");
            assertThat(movie.getSize()).isEqualTo(1486723L);

            assertThat(episode1.getEpisodePath()).isEqualTo("output_video_eng_subs");
            assertThat(episode2.getEpisodePath()).isEqualTo("output_video_2_eng_subs");

            assertThat(subtitles.size()).isEqualTo(10);

            assertThat(extractedSubtitle1.getMovieId()).isEqualTo(movie.getId());
            assertThat(extractedSubtitle1.getEpisodeId()).isEqualTo(episode1.getId());
            assertThat(extractedSubtitle1.getType()).isEqualTo(Subtitle.SubtitleType.Episode);
            assertThat(extractedSubtitle1.getPath()).isEqualTo("output_video_eng_subs\\extracted_subtitles_0.vtt");
            assertThat(extractedSubtitle1.getName()).isEqualTo("Extracted (0)");

            assertThat(extractedSubtitle2.getMovieId()).isEqualTo(movie.getId());
            assertThat(extractedSubtitle2.getEpisodeId()).isEqualTo(episode2.getId());
            assertThat(extractedSubtitle2.getType()).isEqualTo(Subtitle.SubtitleType.Episode);
            assertThat(extractedSubtitle2.getPath()).isEqualTo("output_video_2_eng_subs\\extracted_subtitles_0.vtt");
            assertThat(extractedSubtitle2.getName()).isEqualTo("Extracted (0)");

            assertThat(extractedSubtitle3.getMovieId()).isEqualTo(movie.getId());
            assertThat(extractedSubtitle3.getEpisodeId()).isEqualTo(episode2.getId());
            assertThat(extractedSubtitle3.getType()).isEqualTo(Subtitle.SubtitleType.Episode);
            assertThat(extractedSubtitle3.getPath()).isEqualTo("output_video_2_eng_subs\\extracted_subtitles_1.vtt");
            assertThat(extractedSubtitle3.getName()).isEqualTo("Extracted (1)");

            assertThat(nestedSubtitle1.getPath()).isEqualTo("Subs\\English.vtt");
            assertThat(nestedSubtitle2.getPath()).isEqualTo("Subs\\French.vtt");
            assertThat(nestedSubtitle3.getPath()).isEqualTo("Subs\\Chinese.vtt");
            assertThat(nestedSubtitle4.getPath()).isEqualTo("Subs2\\hacker-din.vtt");
            assertThat(nestedSubtitle5.getPath()).isEqualTo("Subs2\\hacker-din_eng.vtt");
            assertThat(subtitle.getPath()).isEqualTo("2024-01-28 14-35-19.vtt");
            assertThat(trailerSubtitle.getPath()).isEqualTo("sample.vtt");
        });
    }

    /**
     * Counts the occurences of a word. Every word must be space-separated!
     * @param str the string to count word occurrences from
     * @param word the word which occurrence we want to count
     * @return the amount of occurrences of the given word in the given string
     */
    private int countWordOccurrences(String str, String word) {
        String[] words = str.split(" ");

        int count = 0;
        for (String s : words) {
            if (word.equals(s))
                count++;
        }

        return count;
    }
}
