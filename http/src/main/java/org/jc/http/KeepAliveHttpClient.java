package org.jc.http;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class KeepAliveHttpClient
{
    public static void main(String[] args) {

        String url = args[0];
        try {
            // Create an HttpClient with keep-alive
            HttpClient client = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_1_1)
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            // Create the first request
            HttpRequest request1 = HttpRequest.newBuilder()
                    .uri(new URI(url))

                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            // Send the first request and get the response
            HttpResponse<String> response1 = client.send(request1, HttpResponse.BodyHandlers.ofString());
            System.out.println("Response 1: " + response1.body() + " " + response1.headers());

            // Create the second request
            HttpRequest request2 = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            // Send the second request and get the response
            HttpResponse<String> response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());
            System.out.println("Response 2: " + response2.body() + " " + response2.headers());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
