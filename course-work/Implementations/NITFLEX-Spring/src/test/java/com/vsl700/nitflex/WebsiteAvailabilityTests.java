package com.vsl700.nitflex;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

public class WebsiteAvailabilityTests {
    /**
     * If this test fails you might have to connect the server to a VPN as Zamunda is banned in some countries (Bulgaria is not one of them)!
     */
    @Test
    public void connectsToZamunda(){
        WebClient client = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create().followRedirect(false)))
                .baseUrl("https://zamunda.net")
                .build();

        client.get()
                .retrieve()
                .onStatus(HttpStatusCode::is3xxRedirection, ClientResponse::createException)
                .toBodilessEntity()
                .block();
    }
}
