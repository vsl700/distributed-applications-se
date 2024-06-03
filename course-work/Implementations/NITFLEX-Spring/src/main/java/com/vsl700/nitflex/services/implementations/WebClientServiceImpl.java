package com.vsl700.nitflex.services.implementations;

import com.vsl700.nitflex.components.WebsiteCredentials;
import com.vsl700.nitflex.services.WebClientService;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class WebClientServiceImpl implements WebClientService {
    @Override
    public String loginAndGetCookie(String url, String usernameAttr, String passwordAttr, WebsiteCredentials websiteCredentials) {
        MultiValueMap<String, String> creds = new LinkedMultiValueMap<>();
        creds.put(usernameAttr, List.of(websiteCredentials.getUsername()));
        creds.put(passwordAttr, List.of(websiteCredentials.getPassword()));

        WebClient client = WebClient.builder()
                .baseUrl(url)
                .build();

        AtomicReference<String> result = new AtomicReference<>();
        client.post()
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(creds))
                .exchangeToMono(response -> {
                    List<String> cookieList = response.cookies().values().stream()
                            .map(l -> String.join("; ", l.stream().map(c -> "%s=%s".formatted(c.getName(), c.getValue())).toList()))
                            .toList();
                    String res = String.join("; ", cookieList);

                    result.set(res);

                    return response.bodyToMono(Void.class);
                }).block();

        return result.get();
    }

    @Override
    public String getWebsiteContents(String url, String cookie) {
        return new String(getContentsAsByteArray(url, cookie));
    }

    @Override
    public byte[] getContentsAsByteArray(String url, String cookie){
        WebClient client = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create().followRedirect(true)))
                .baseUrl(url)
                .defaultHeader("Cookie", cookie)
                .build();

        return client.get()
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, ClientResponse::createException)
                .onStatus(HttpStatusCode::is5xxServerError, ClientResponse::createException)
                .bodyToMono(byte[].class)
                .block();
    }
}
