package com.vsl700.nitflex.services;

import com.vsl700.nitflex.components.SharedProperties;
import com.vsl700.nitflex.services.implementations.TorrentMovieDownloaderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;

import java.io.File;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@Disabled
@ExtendWith(MockitoExtension.class)
public class TorrentMovieDownloaderServiceImplTests {
    @Mock
    private SharedProperties sharedProperties;
    private TorrentMovieDownloaderService torrentMovieDownloaderService;

    @BeforeEach
    public void setUp(){
        torrentMovieDownloaderService = mock(TorrentMovieDownloaderServiceImpl.class,
                Mockito.withSettings().useConstructor(sharedProperties)
                        .defaultAnswer(CALLS_REAL_METHODS)
                        .strictness(Strictness.LENIENT));

        when(sharedProperties.getMoviesFolder()).then(invocation -> "D:\\Videos\\Torrent Test");
    }

    @ParameterizedTest
    @ValueSource(strings = {"D:\\Videos\\Torrent Test\\Oppenheimer.2023.1080p.BluRay.x265.DTS.HD.MA.5.1-DiN.torrent",
            "D:\\Videos\\Torrent Test\\The.Creator.2023.1080p.AMZN.WEB-DL.DDP5.1.H.264-LessConfusingThanTenet.mkv.torrent",
            "D:\\Videos\\Torrent Test\\The.Killer.2023.WEB.H264-RAW.torrent"})
    public void downloadFromTorrentFilePath_Test(String path) {
        torrentMovieDownloaderService.downloadFromTorrentFilePath(Path.of(path));
    }

    @Test
    public void downloadFromTorrentFilePath_singleFile_inParentFolder_Test() {
        torrentMovieDownloaderService.downloadFromTorrentFilePath(Path.of("D:\\Videos\\Torrent Test\\The.Creator.2023.1080p.AMZN.WEB-DL.DDP5.1.H.264-LessConfusingThanTenet.mkv.torrent"));
    }

    @Test
    public void downloadFromTorrentFilePath_singleFile_inParentFolder_Test2() {
        torrentMovieDownloaderService.downloadFromTorrentFilePath(Path.of("D:\\Videos\\Torrent Test\\The.Gods.Must.Be.Crazy.1980.WEBRip.BG.Audio-Stasoiakara.avi.torrent"));
    }

    @Test
    public void downloadFromTorrentFilePath_singleFile_inItsOwnFolder_Test() {
        torrentMovieDownloaderService.downloadFromTorrentFilePath(Path.of("D:\\Videos\\Torrent Test\\Baby.Driver.2017.1080p.Bluray.x265.torrent"));
    }

    // Download the Movie with the least size to speed up testing
    @Test
    public void downloadFromTorrentFilePath_ShortDownload_Test() {
        torrentMovieDownloaderService.downloadFromTorrentFilePath(Path.of("D:\\Videos\\Torrent Test\\The.Killer.2023.WEB.H264-RAW.torrent"));
    }

    @Test
    public void downloadFromTorrentFilePath_ReturnString_Test() {
        String actual = torrentMovieDownloaderService.downloadFromTorrentFilePath(Path.of("D:\\Videos\\Torrent Test\\The.Killer.2023.WEB.H264-RAW.torrent")).toString();
        assertThat(actual).isEqualTo("D:\\Videos\\Torrent Test\\The.Killer.2023.WEB.H264-RAW");
    }

    @Test
    public void downloadFromTorrentFilePath_ReturnString_Test2() {
        String actual = torrentMovieDownloaderService.downloadFromTorrentFilePath(Path.of("D:\\Videos\\Torrent Test\\The.Gods.Must.Be.Crazy.1980.WEBRip.BG.Audio-Stasoiakara.avi.torrent")).toString();
        assertThat(actual).isEqualTo("D:\\Videos\\Torrent Test\\The.Gods.Must.Be.Crazy.1980.WEBRip.BG.Audio-Stasoiakara");
    }

    @Test
    public void downloadFromTorrentFilePath_ReturnString_Test3() {
        String actual = torrentMovieDownloaderService.downloadFromTorrentFilePath(Path.of("D:\\Videos\\Torrent Test\\Baby.Driver.2017.1080p.Bluray.x265.torrent")).toString();
        assertThat(actual).isEqualTo("D:\\Videos\\Torrent Test\\Baby.Driver.2017.1080p.Bluray.x265");
    }
}
