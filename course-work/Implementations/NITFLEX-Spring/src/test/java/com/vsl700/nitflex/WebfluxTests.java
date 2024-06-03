package com.vsl700.nitflex;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled
public class WebfluxTests {
    @Test
    public void fileDownload_byBytes_Test(){
        WebClient client = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create().followRedirect(true)))
                .baseUrl("https://zamunda.net/download.php/747087/Oppenheimer.2023.1080p.BluRay.x265.DTS.HD.MA.5.1-DiN.torrent")
                .defaultHeader("Cookie", "{put cookie here}")
                .build();

        byte[] bytes = client.get()
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, ClientResponse::createException)
                .onStatus(HttpStatusCode::is5xxServerError, ClientResponse::createException)
                .bodyToMono(byte[].class)
                .block();

        assert bytes != null;
        assertThat(bytes.length).isEqualTo(56036);
    }
}
