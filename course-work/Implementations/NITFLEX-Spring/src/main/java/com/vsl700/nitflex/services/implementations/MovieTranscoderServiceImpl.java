package com.vsl700.nitflex.services.implementations;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;
import com.vsl700.nitflex.components.SharedProperties;
import com.vsl700.nitflex.models.Episode;
import com.vsl700.nitflex.models.Movie;
import com.vsl700.nitflex.models.Subtitle;
import com.vsl700.nitflex.repo.EpisodeRepository;
import com.vsl700.nitflex.repo.MovieRepository;
import com.vsl700.nitflex.repo.SubtitleRepository;
import com.vsl700.nitflex.services.MovieTranscoderService;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import ws.schild.jave.DefaultFFMPEGLocator;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class MovieTranscoderServiceImpl implements MovieTranscoderService {
    private static final String extractedSubtitlesNameStandard = "extracted_subtitles_%d.vtt";
    private static final String videoFileTranscodeCommandTemplate = "-i \"%s\" -c:a aac -c:v libx264 -pix_fmt yuv420p -vf format=yuv420p -ac 2 -map 0:v:0 -map 0:a -adaptation_sets \"id=0,streams=v id=1,streams=a\" -f dash -init_seg_name \"%s^init-$RepresentationID$.m4s\" -media_seg_name \"%s^chunk-$RepresentationID$-$Number%s$.m4s\" \"%s^manifest.mpd\""
            .replace("^", File.separator);
    private static final String subtitleStreamTranscodeCommandTemplate = "-i \"%s\" -vn -an -c:s webvtt -map 0:s:%s -f webvtt \"%s^%s\""
            .formatted("%s", "%d", "%s", extractedSubtitlesNameStandard)
            .replace("^", File.separator);
    private static final String subtitleFileTranscodeCommandTemplate = "-sub_charenc %s -i \"%s\" -c:s webvtt -f webvtt \"%s\"";

    private MovieRepository movieRepo;
    private EpisodeRepository episodeRepo;
    private SubtitleRepository subtitleRepo;
    private SharedProperties sharedProperties;

    @SneakyThrows
    @Override
    public void transcode(Movie movie) {
        if(movie.isTranscoded() || !sharedProperties.isTranscodingEnabled())
            return;

        // Save all original video and subtitle file paths
        List<String> videoFiles = new ArrayList<>();
        List<String> subtitleFiles = subtitleRepo.findAllByMovieId(movie.getId()).stream()
                .map(s -> Path.of(sharedProperties.getMoviesFolder(), movie.getPath(), s.getPath()).toString())
                .toList();
        switch (movie.getType()){
            case Film -> videoFiles.add(Path.of(sharedProperties.getMoviesFolder(), movie.getPath(), movie.getFilmPath()).toString());
            case Series -> videoFiles.addAll(episodeRepo.findAllBySeriesId(movie.getId()).stream()
                    .map(e -> Path.of(sharedProperties.getMoviesFolder(), movie.getPath(), e.getEpisodePath()).toString())
                    .toList());
        }

        if(movie.getTrailerPath() != null)
            videoFiles.add(Path.of(sharedProperties.getMoviesFolder(), movie.getPath(), movie.getTrailerPath()).toString());

        // Transcode subtitle files
        transcodeSubtitles(movie);

        // Transcoding the video files
        switch (movie.getType()){
            case Film -> transcodeFilm(movie);
            case Series -> transcodeSeries(movie);
        }

        // Assign Movie as transcoded
        movie.setTranscoded(true);
        movieRepo.save(movie);

        // Remove original files
        subtitleFiles.forEach(p -> {
            try {
                Files.delete(Path.of(p));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        videoFiles.forEach(p -> {
            try {
                Files.delete(Path.of(p));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        // Recalculate Movie size
        long size;
        try(var stream = Files.walk(Path.of(sharedProperties.getMoviesFolder(), movie.getPath()))) {
                size = stream/*.filter(f -> !Files.isDirectory(f))*/
                    .mapToLong(f -> {
                        try {
                            return Files.size(f);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .sum();
        }

        movie.setSize(size);
        movieRepo.save(movie);
    }

    private void transcodeFilm(Movie movie){
        String filmFilePath = Path.of(sharedProperties.getMoviesFolder(), movie.getPath(), movie.getFilmPath()).toString();
        // Transcode subtitles from film file
        extractAndSaveSubtitlesFromVideoFile(Subtitle.SubtitleType.Film, filmFilePath, movie);

        // Transcoding the film file
        if(!isPathOfTranscodedVideoFile(filmFilePath))
            movie.setFilmPath(Path.of(sharedProperties.getMoviesFolder(), movie.getPath()).relativize(Path.of(transcodeVideoFile(filmFilePath))).toString());

        // If there's no trailer, return
        if(movie.getTrailerPath() == null)
            return;

        String trailerFilePath = Path.of(sharedProperties.getMoviesFolder(), movie.getPath(), movie.getTrailerPath()).toString();
        // Transcode subtitles from trailer file
        extractAndSaveSubtitlesFromVideoFile(Subtitle.SubtitleType.Trailer, trailerFilePath, movie);

        // Transcoding the trailer file
        if(!isPathOfTranscodedVideoFile(trailerFilePath))
            movie.setTrailerPath(Path.of(sharedProperties.getMoviesFolder(), movie.getPath()).relativize(Path.of(transcodeVideoFile(trailerFilePath))).toString());
    }

    private void transcodeSeries(Movie movie){
        // Transcoding each episode
        episodeRepo.findAllBySeriesId(movie.getId()).forEach(e -> {
            // Transcode subtitles from episode file
            extractAndSaveSubtitlesFromEpisode(e, movie);

            // Transcode episode file
            String episodeFilePath = Path.of(sharedProperties.getMoviesFolder(), movie.getPath(), e.getEpisodePath()).toString();
            if(!isPathOfTranscodedVideoFile(episodeFilePath)) {
                e.setEpisodePath(Path.of(sharedProperties.getMoviesFolder(), movie.getPath()).relativize(Path.of(transcodeVideoFile(episodeFilePath))).toString());
                episodeRepo.save(e);
            }
        });

        // If there's no trailer, return
        if(movie.getTrailerPath() == null)
            return;

        String trailerFilePath = Path.of(sharedProperties.getMoviesFolder(), movie.getPath(), movie.getTrailerPath()).toString();
        // Transcode subtitles from trailer file
        extractAndSaveSubtitlesFromVideoFile(Subtitle.SubtitleType.Trailer, trailerFilePath, movie);

        // Transcoding the trailer file
        if(!isPathOfTranscodedVideoFile(trailerFilePath))
            movie.setTrailerPath(Path.of(sharedProperties.getMoviesFolder(), movie.getPath()).relativize(Path.of(transcodeVideoFile(trailerFilePath))).toString());
    }

    private void transcodeSubtitles(Movie movie){
        subtitleRepo.findAllByMovieId(movie.getId()).stream()
                .filter(s -> !isPathOfTranscodedSubtitleFile(s.getPath()))
                .forEach(s -> {
                    s.setPath(Path.of(sharedProperties.getMoviesFolder(), movie.getPath()).relativize(Path.of(transcodeSubtitleFile(Path.of(sharedProperties.getMoviesFolder(), movie.getPath(), s.getPath()).toString()))).toString());
                    subtitleRepo.save(s);
                });
    }

    private void extractAndSaveSubtitlesFromEpisode(Episode episode, Movie movie){
        String path = Path.of(sharedProperties.getMoviesFolder(), movie.getPath(), episode.getEpisodePath()).toString();
        String outputVideoFilePath = generateVideoFileOutputPath(path);
        String outputSubtitleFilePath;
        for(int i = 0; subtitleRepo.findByPath(outputSubtitleFilePath = Path.of(outputVideoFilePath, extractedSubtitlesNameStandard.formatted(i)).toString()).isEmpty() && extractAndTranscodeSubtitlesFromVideoFile(path, i); i++){
            Subtitle extractedSubtitle = new Subtitle(movie.getId(), Subtitle.SubtitleType.Episode, "Extracted (%d)".formatted(i), Path.of(sharedProperties.getMoviesFolder(), movie.getPath()).relativize(Path.of(outputSubtitleFilePath)).toString());
            extractedSubtitle.setEpisodeId(episode.getId());
            subtitleRepo.save(extractedSubtitle);
        }
    }

    private void extractAndSaveSubtitlesFromVideoFile(Subtitle.SubtitleType type, String path, Movie movie){
        String outputVideoFilePath = generateVideoFileOutputPath(path);
        String outputSubtitleFilePath;
        for(int i = 0; subtitleRepo.findByPath(outputSubtitleFilePath = Path.of(outputVideoFilePath, extractedSubtitlesNameStandard.formatted(i)).toString()).isEmpty() && extractAndTranscodeSubtitlesFromVideoFile(path, i); i++){
            Subtitle extractedSubtitle = new Subtitle(movie.getId(), type, "Extracted (%d)".formatted(i), Path.of(sharedProperties.getMoviesFolder(), movie.getPath()).relativize(Path.of(outputSubtitleFilePath)).toString());
            subtitleRepo.save(extractedSubtitle);
        }
    }

    private boolean isPathOfTranscodedVideoFile(String path){ // TODO Improve the method's logic
        return Files.isDirectory(Path.of(path));
    }

    private boolean isPathOfTranscodedSubtitleFile(String path){
        return path.endsWith(".vtt");
    }

    private String generateVideoFileOutputPath(String path){
        return createDirAndReturn(path.substring(0, path.lastIndexOf('.')));
    }

    private String transcodeVideoFile(String path){
        String outputPath = generateVideoFileOutputPath(path);

        int exitCode = execFFmpegCommand(videoFileTranscodeCommandTemplate.formatted(path, outputPath, outputPath, "%05d", outputPath));
        if(exitCode != 0)
            throw new RuntimeException("Transcoding of video file \"%s\" failed!".formatted(path)); // TODO Add custom exception

        correctMPDValues(outputPath);

        return outputPath;
    }

    /**
     * Correct the 'initialization' and 'media' values of the video and audio streams in the MPD file
     */
    @SneakyThrows
    private void correctMPDValues(String path){
        Path manifestFilePath = Path.of(path, "manifest.mpd");
        StringBuilder manifestContent = new StringBuilder(Files.readString(manifestFilePath));

        String initStart = "initialization=\"";
        String initEnd = "init-";
        String mediaStart = "media=\"";
        String mediaEnd = "chunk-";
        int initStartIndex = manifestContent.indexOf(initStart) + initStart.length();
        int initEndIndex = manifestContent.indexOf(initEnd);
        int mediaStartIndex = manifestContent.indexOf(mediaStart) + mediaStart.length();
        int mediaEndIndex = manifestContent.indexOf(mediaEnd);

        while(initStartIndex > initStart.length()){
            manifestContent.delete(initStartIndex, initEndIndex);
            int offset = initEndIndex - initStartIndex; // After the first delete the desired character's indexes will shift
            manifestContent.delete(mediaStartIndex - offset, mediaEndIndex - offset);

            // Update indexes
            initStartIndex = manifestContent.indexOf(initStart, initStartIndex + 1) + initStart.length();
            initEndIndex = manifestContent.indexOf(initEnd, initEndIndex + 1);
            mediaStartIndex = manifestContent.indexOf(mediaStart, mediaStartIndex + 1) + mediaStart.length();
            mediaEndIndex = manifestContent.indexOf(mediaEnd, mediaEndIndex + 1);
        }

        Files.write(manifestFilePath, manifestContent.toString().getBytes());
    }

    private String transcodeSubtitleFile(String path){
        // Detect charset of input file
        String charset = getCharsetOfFile(path);

        String outputPath = path.substring(0, path.lastIndexOf('.')) + ".vtt";

        int exitCode = execFFmpegCommand(subtitleFileTranscodeCommandTemplate.formatted(charset, path, outputPath));
        if(exitCode != 0)
            throw new RuntimeException("Transcoding of subtitle file \"%s\" failed!".formatted(path)); // TODO Add custom exception

        return outputPath;
    }

    private String getCharsetOfFile(String path) {
        String charset;
        try (FileInputStream fis = new FileInputStream(path)) {
            byte[] data = fis.readAllBytes();

            CharsetDetector detector = new CharsetDetector();
            detector.setText(data);

            CharsetMatch match = detector.detect();
            if(match == null)
                throw new RuntimeException("Could not recognize charset of %s".formatted(path));

            charset = match.getName();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return charset;
    }

    private boolean extractAndTranscodeSubtitlesFromVideoFile(String path, int subtitleStreamNumber){
        String outputPathStr = generateVideoFileOutputPath(path);

        // Remove output file if already exists (to prevent FFmpeg executable from crashing)
        Path outputPath = Path.of(outputPathStr, extractedSubtitlesNameStandard.formatted(subtitleStreamNumber));
        if(Files.exists(outputPath)) {
            try {
                Files.delete(outputPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return execFFmpegCommand(subtitleStreamTranscodeCommandTemplate.formatted(path, subtitleStreamNumber, outputPathStr, subtitleStreamNumber)) == 0;
    }

    @SneakyThrows
    private int execFFmpegCommand(String options) {
        // Get the path of the FFmpeg executable, provided by the Jave2 library and appropriate for the OS
        Path executablePath = Path.of(new DefaultFFMPEGLocator().getFFMPEGExecutablePath());

        // Create the FFmpeg process by invoking the executable with the provided options
        List<String> commandPieces = new ArrayList<>();
        commandPieces.add(executablePath.toString());
        commandPieces.addAll(List.of(options.split(" ")));

        ProcessBuilder pb = new ProcessBuilder(commandPieces);
        Process process = pb.start();

        // Terminate the child process when the Java application is being terminated
        Runtime.getRuntime().addShutdownHook(new Thread(process::destroy));

        String line;
        BufferedReader reader2 = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        while ((line = reader2.readLine()) != null) {
            System.out.println(line);
        }
        reader2.close();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
        reader.close();

        return process.waitFor();
    }

    private String createDirAndReturn(String path){
        File file = new File(path);
        if(!file.exists() && !file.mkdir())
            throw new RuntimeException("Directory at %s could not be created!".formatted(path)); // TODO Change that or add custom exception
        return path;
    }
}
