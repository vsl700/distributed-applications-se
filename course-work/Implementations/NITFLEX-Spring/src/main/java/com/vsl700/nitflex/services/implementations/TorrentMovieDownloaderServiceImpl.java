package com.vsl700.nitflex.services.implementations;

import com.turn.ttorrent.client.Client;
import com.turn.ttorrent.client.SharedTorrent;
import com.vsl700.nitflex.components.SharedProperties;
import com.vsl700.nitflex.exceptions.MovieTorrentingException;
import com.vsl700.nitflex.services.TorrentMovieDownloaderService;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@AllArgsConstructor
public class TorrentMovieDownloaderServiceImpl implements TorrentMovieDownloaderService {
    private SharedProperties sharedProperties;

    @Override
    public Path downloadFromTorrentFilePath(Path torrentFilePath) {
        var client = createClient(torrentFilePath);
        client.download();

        client.waitForCompletion();
        client.stop();

        Path resultMovieFolder = Path.of(sharedProperties.getMoviesFolder(), client.getTorrent().getName());

        // If the torrent was a 'single-file' and the downloaded video file is in the parent folder, we should move it
        // from the parent folder and put it in its own folder (following the MovieLoaderService's policy)
        if(!client.getTorrent().isMultifile()){
            String fileName = client.getTorrent().getFilenames()
                    .stream().findFirst().orElseThrow();

            if(Path.of(fileName).getNameCount() > 1)
                return resultMovieFolder;

            // If the video file is not in its own folder
            String movieFolderName = fileName.substring(0, fileName.lastIndexOf('.'));

            File parentDir = new File(sharedProperties.getMoviesFolder());

            File movieFolder = new File(parentDir, movieFolderName);
            if (!movieFolder.mkdir())
                throw new MovieTorrentingException("Folder %s could not be created!".formatted(movieFolder.getAbsolutePath()));

            File targetFile = new File(parentDir, fileName);
            try {
                Files.move(Path.of(targetFile.getAbsolutePath()), Path.of(movieFolder.getAbsolutePath(), fileName));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            resultMovieFolder = Path.of(movieFolder.getAbsolutePath());
        }

        return resultMovieFolder;
    }

    @SneakyThrows
    private Client createClient(Path torrentFilePath){
        File parentDir = new File(sharedProperties.getMoviesFolder());
        if(!parentDir.exists() && !parentDir.mkdir()) {
            throw new MovieTorrentingException("Access to %s denied!".formatted(parentDir.getAbsolutePath()));
        }

        File torrentFile = torrentFilePath.toFile();

        return new Client(InetAddress.getLocalHost(), SharedTorrent.fromFile(
                torrentFile,
                parentDir
        ));
    }
}
