package com.vsl700.nitflex;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;

public class CookieCombiningTests {
    @Test
    public void test(){
        WebClient client = WebClient.builder()
                .baseUrl("https://zamunda.net")
                .build();

        StringBuilder sb = new StringBuilder();
        client.get()
                .exchangeToMono(response -> {
                    System.out.println(response.cookies().size());
                    var cookieList = response.cookies().values().stream()
                            .map(l -> String.join("; ", l.stream().map(c -> "%s=%s".formatted(c.getName(), c.getValue())).toList()))
                            .toList();
                    sb.append(String.join("; ", cookieList));

                    return response.bodyToMono(Void.class);
                }).block();

        System.out.println(sb);
    }

    @Test
    public void cookieManager_test(){
        WebClient client = WebClient.builder()
                .baseUrl("https://zamunda.net")
                .build();

        StringBuilder sb = new StringBuilder();
        client.get()
                .exchangeToMono(response -> {

                    System.out.println(response.cookies().size());
                    var cookieList = response.cookies().values().stream()
                            .map(l -> String.join("; ", l.stream().map(c -> "%s=%s".formatted(c.getName(), c.getValue())).toList()))
                            .toList();
                    sb.append(String.join("; ", cookieList));

                    return response.bodyToMono(Void.class);
                }).block();

        System.out.println(sb);
    }
}
