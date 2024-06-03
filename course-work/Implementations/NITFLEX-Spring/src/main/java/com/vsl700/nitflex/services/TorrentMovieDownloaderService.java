package com.vsl700.nitflex.services;

import java.nio.file.Path;
import java.util.function.Consumer;

public interface TorrentMovieDownloaderService {
    /**
     * Downloads a movie from the given torrent file at the specified path asynchronously, and then invokes the
     * given Consumer method.
     * @param torrentFilePath the file path of the torrent file to download with
     * @param onDownloadFinished the method to be invoked after movie download completes. Arguments accepted:<br>
     *                           <li> movieFolder - the just-downloaded movie's folder (absolute path)
     */
    default void downloadFromTorrentFilePathAsync(Path torrentFilePath, Consumer<Path> onDownloadFinished){
        new Thread(() -> {
            Path movieFolderPath = downloadFromTorrentFilePath(torrentFilePath);
            onDownloadFinished.accept(movieFolderPath);
        }).start();
    }

    /**
     * Downloads a movie from the given torrent file at the specified path
     * @param torrentFilePath the file path of the torrent file to download with
     * @return the full path to the newly downloaded movie
     */
    Path downloadFromTorrentFilePath(Path torrentFilePath);
}
