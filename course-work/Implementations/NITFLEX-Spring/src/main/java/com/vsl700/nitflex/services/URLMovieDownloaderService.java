package com.vsl700.nitflex.services;

import java.net.URL;
import java.nio.file.Path;
import java.util.function.Consumer;

public interface URLMovieDownloaderService {
    /**
     * Downloads a movie from the given URL asynchronously, and then invokes the given Consumer method.
     * @param pageUrl the URL of the movie page
     * @param onDownloadFinished the method to be invoked after movie download completes. Arguments accepted:<br>
     *                           <li> movieFolder - the just-downloaded movie's folder (absolute path)
     */
    default void downloadFromPageURLAsync(URL pageUrl, Consumer<Path> onDownloadFinished){
        new Thread(() -> {
            Path movieFolderPath = downloadFromPageURL(pageUrl);
            onDownloadFinished.accept(movieFolderPath);
        }).start();
    }

    /**
     * Downloads a movie from the given URL
     * @param pageUrl the URL of the movie page
     * @return the full path to the newly downloaded movie
     */
    Path downloadFromPageURL(URL pageUrl);
}
