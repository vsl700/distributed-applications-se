package com.vsl700.nitflex;

import com.turn.ttorrent.client.SharedTorrent;
import org.junit.jupiter.api.*;
import com.turn.ttorrent.client.Client;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

@Disabled
public class TTorrentLibraryTests {

    private Client client;

    @BeforeEach
    public void setUp() throws IOException, NoSuchAlgorithmException {
        // First, instantiate the Client object.
        File parentDir = new File("D:\\Videos\\Torrent Test"); // TESTING! Use 'D:\Videos' instead!
        if(!parentDir.exists() && !parentDir.mkdir()) {
            fail();
            return;
        }

        client = new Client(InetAddress.getLocalHost(), SharedTorrent.fromFile(
                new File("D:\\Downloads\\The.Retirement.Plan.2023.1080p.WEB.H265-RAW.torrent"),
                parentDir
        ));
    }

    @AfterEach
    public void end(){
        client.stop();
    }

    @Test
    public void logTest(){
        //client.download();
        client.run();
        client.info();
    }

    @Test
    public void test() throws InterruptedException {
        client.download();

        while(!client.getState().equals(Client.ClientState.DONE) && !client.getState().equals(Client.ClientState.ERROR)){
            //System.out.println(client.getState());
            //if(client.getState().equals(Client.ClientState.SHARING))
                client.info();
            Thread.sleep(1000);
        }

        assertThat(client.getState()).isEqualTo(Client.ClientState.DONE);
    }

}
