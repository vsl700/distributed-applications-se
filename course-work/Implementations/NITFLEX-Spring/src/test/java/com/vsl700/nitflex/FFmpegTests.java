package com.vsl700.nitflex;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ws.schild.jave.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled
public class FFmpegTests {
    @Test
    public void video_dashingTest(){
        /* Step 1. Declaring source file and Target file */
        File source = new File("D:\\Videos\\video-1622183701.mp4");
        File target = new File("D:\\Videos\\Conversion_Tests\\video-1622183701\\output_manifest.mpd");

        /* Step 2. Set Audio Attrributes for conversion*/
        AudioAttributes audio = new AudioAttributes();
        audio.setCodec("aac");
        // here 64kbit/s is 64000
        /*audio.setBitRate(64000);*/
        /*audio.setChannels(2);*/
        /*audio.setSamplingRate(44100);*/

        /* Step 3. Set Video Attributes for conversion*/
        VideoAttributes video = new VideoAttributes();
        video.setCodec("h264");
        video.setX264Profile(VideoAttributes.X264_PROFILE.BASELINE);
        // Here 160 kbps video is 160000
        /*video.setBitRate(160000);*/
        // More the frames more quality and size, but keep it low based on devices like mobile
        /*video.setFrameRate(15);*/
        /*video.setSize(new VideoSize(400, 300));*/

        /* Step 4. Set Encoding Attributes*/
        EncodingAttributes attrs = new EncodingAttributes();
        attrs.setFormat("dash");
        attrs.setAudioAttributes(audio);
        attrs.setVideoAttributes(video);

        /* Step 5. Do the Encoding*/
        try {
            Encoder encoder = new Encoder();
            encoder.encode(new MultimediaObject(source), target, attrs);
        } catch (Exception e) {
            /*Handle here the video failure*/
            throw new RuntimeException(e);
        }
    }

    @Test
    public void video_dashingTest2() throws IOException, InterruptedException {
        Path path = Path.of(new DefaultFFMPEGLocator().getFFMPEGExecutablePath());
        String inputPath = "D:\\Videos\\video-1622183701.mp4";
        String outputPath = "D:\\Videos\\Conversion_Tests\\video-1622183701";
        String ffmpegCommand = "%s -i %s -c:a aac -c:v libx264 -profile:v:1 baseline -profile:v:0 main -bf 1 -keyint_min 120 -g 120 -sc_threshold 0 -b_strategy 0 -adaptation_sets \"id=0,streams=v id=1,streams=a\" -f dash -init_seg_name %s\\init-$RepresentationID$.m4s -media_seg_name %s\\chunk-$RepresentationID$-$Number%s$.m4s %s\\output_manifest.mpd".formatted(path.toString(), inputPath, outputPath, outputPath, "%05d", outputPath);

        ProcessBuilder pb = new ProcessBuilder(ffmpegCommand.split(" "));
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

        assertThat(process.waitFor()).isEqualTo(0);
    }

    @Test
    public void video_dashingTest3() throws IOException, InterruptedException {
        Path path = Path.of(new DefaultFFMPEGLocator().getFFMPEGExecutablePath());
        String inputPath = "\"D:\\Videos\\EPIC Moonguard match.mkv\"";
        String outputPath = "\"D:\\Videos\\Conversion_Tests\\EPIC Moonguard match\"";
        String ffmpegCommand = "%s -i %s -c:a aac -c:v libx264 -profile:v:1 baseline -profile:v:0 main -bf 1 -keyint_min 120 -g 120 -sc_threshold 0 -b_strategy 0 -adaptation_sets \"id=0,streams=v id=1,streams=a\" -f dash -init_seg_name %s\\init-$RepresentationID$.m4s -media_seg_name %s\\chunk-$RepresentationID$-$Number%s$.m4s %s\\output_manifest.mpd".formatted(path.toString(), inputPath, outputPath, outputPath, "%05d", outputPath);

        ProcessBuilder pb = new ProcessBuilder(ffmpegCommand.split(" "));
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

        assertThat(process.waitFor()).isEqualTo(0);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "D:\\Videos\\The.Commuter.2018.BRRip.XviD.AC3.DUAL-SlzD\\The.Commuter.2018.BRRip.XviD.AC3.DUAL-SlzD.avi\nD:\\Videos\\Conversion_Tests\\The.Commuter.2018.BRRip.XviD.AC3.DUAL-SlzD",
            "D:\\Videos\\Retribution.2023.1080p.WEB-DL.DDP5.1.Atmos.H.264-IWiLLFiNDyouANDiWiLLKiLLYOU\\Retribution.2023.1080p.WEB-DL.DDP5.1.Atmos.H.264-IWiLLFiNDyouANDiWiLLKiLLYOU.mkv\nD:\\Videos\\Conversion_Tests\\Retribution.2023.1080p.WEB-DL.DDP5.1.Atmos.H.264-IWiLLFiNDyouANDiWiLLKiLLYOU",
            "D:\\Videos\\Upgrade.2018.1080p.BluRay.x264.BGAUDIO-SlzD\\Upgrade.2018.1080p.BluRay.x264.BGAUDIO-SlzD.mkv\nD:\\Videos\\Conversion_Tests\\Upgrade.2018.1080p.BluRay.x264.BGAUDIO-SlzD",
            "D:\\Videos\\Tetris.2023.1080p.WEBRip.x264-LAMA\\Tetris.2023.1080p.WEBRip.x264-LAMA.mp4\nD:\\Videos\\Conversion_Tests\\Tetris.2023.1080p.WEBRip.x264-LAMA",
            "D:\\Videos\\Blacklight.2022.BRRip.XviD.AC3-EVO\\Blacklight.2022.BRRip.XviD.AC3-EVO.avi\nD:\\Videos\\Conversion_Tests\\Blacklight.2022.BRRip.XviD.AC3-EVO",
            "D:\\Videos\\The Gods Must be Crazy\\The.Gods.Must.Be.Crazy.1980.WEBRip.BG.Audio-Stasoiakara.avi\nD:\\Videos\\Conversion_Tests\\The Gods Must be Crazy",
            "D:\\Videos\\The Blue Lagoon BG Audio\\The Blue Lagoon (1980) 576p.BD-Rip.x264 + EN n BG audio - REFLUX.mkv\nD:\\Videos\\Conversion_Tests\\The Blue Lagoon (1980) 576p.BD-Rip.x264 + EN n BG audio - REFLUX",
            "D:\\Videos\\The.Man.from.Toronto.2022.1080p.BluRay.AV1-DiN\\The.Man.from.Toronto.2022.1080p.BluRay.AV1-DiN.mkv\nD:\\Videos\\Conversion_Tests\\The.Man.from.Toronto.2022.1080p.BluRay.AV1-DiN",
            "D:\\Videos\\L Homme Orchestre (1970)\\L Homme Orchestre.avi\nD:\\Videos\\Conversion_Tests\\L Homme Orchestre (1970)"
    })
    public void videoPiece_dashingTest(String videoPaths) throws IOException, InterruptedException {
        // Get the path of the FFmpeg executable, provided by the Jave2 library and appropriate for the OS
        Path executablePath = Path.of(new DefaultFFMPEGLocator().getFFMPEGExecutablePath());

        // Extract input and output paths from the test's argument
        String[] videoPathsArr = videoPaths.split("\n");
        String inputPath = videoPathsArr[0];
        String outputPath = videoPathsArr[1]; // You have to create the directory first, error otherwise

        int exitCode = execCommand("%s -ss 00:06:42.000 -to 00:06:52.170 -i \"%s\" -c:a aac -c:v libx264 -pix_fmt yuv420p -vf format=yuv420p -ac 2 -map 0:v:0 -map 0:a -adaptation_sets \"id=0,streams=v id=1,streams=a\" -f dash -init_seg_name \"%s\\init-$RepresentationID$.m4s\" -media_seg_name \"%s\\chunk-$RepresentationID$-$Number%s$.m4s\" \"%s\\output_manifest.mpd\"".formatted(executablePath.toString(), inputPath, outputPath, outputPath, "%05d", outputPath));
        assertThat(exitCode).isEqualTo(0);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "D:\\Videos\\Retribution.2023.1080p.WEB-DL.DDP5.1.Atmos.H.264-IWiLLFiNDyouANDiWiLLKiLLYOU\\Retribution.2023.1080p.WEB-DL.DDP5.1.Atmos.H.264-IWiLLFiNDyouANDiWiLLKiLLYOU.mkv\nD:\\Videos\\Conversion_Tests\\Retribution.2023.1080p.WEB-DL.DDP5.1.Atmos.H.264-IWiLLFiNDyouANDiWiLLKiLLYOU",
            "D:\\Videos\\The.Man.from.Toronto.2022.1080p.BluRay.AV1-DiN\\The.Man.from.Toronto.2022.1080p.BluRay.AV1-DiN.mkv\nD:\\Videos\\Conversion_Tests\\The.Man.from.Toronto.2022.1080p.BluRay.AV1-DiN",
            "D:\\Videos\\Shanghai.Knights.2003.720p.BluRay.DTS.x264-EbP\\Shanghai.Knights.2003.720p.BluRay.DTS.x264-EbP (1)-001.mkv\nD:\\Videos\\Conversion_Tests\\subtitles_test_shanghai_knights"
    })
    public void videoPiece_SubtitlesOnly_dashingTest(String videoPaths) throws IOException, InterruptedException { // This test will fail if the video has no subtitles at all!
        // Get the path of the FFmpeg executable, provided by the Jave2 library and appropriate for the OS
        Path executablePath = Path.of(new DefaultFFMPEGLocator().getFFMPEGExecutablePath());

        // Extract input and output paths from the test's argument
        String[] videoPathsArr = videoPaths.split("\n");
        String inputPath = videoPathsArr[0];
        String outputPath = videoPathsArr[1]; // You have to create the directory first, error otherwise

        boolean flag = false;
        for(int subtitleStreamNumber = 0; execCommand("%s -ss 00:06:42.000 -to 00:06:52.170 -i \"%s\" -vn -an -c:s webvtt -map 0:s:%d -f webvtt \"%s\\extracted_subtitles_%d.vtt\"".formatted(executablePath.toString(), inputPath, subtitleStreamNumber, outputPath, subtitleStreamNumber)) == 0; subtitleStreamNumber++, flag = true);
        assertThat(flag).isTrue();
    }

    private int execCommand(String command) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(command.split(" "));
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
}
