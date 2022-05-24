package com.example.telemillonario.service;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import javax.swing.table.DefaultTableModel;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class DniAPI {
    private static HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();

    public static UsuarioAPI FormRestAPI(String dni) throws IOException, InterruptedException {
        final HttpRequest requestPosts = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("https://consulta.api-peru.com/api/dni/"+dni))
                .build();

        ObjectMapper mapper = new ObjectMapper();

        final HttpResponse<String> response = httpClient.send(requestPosts, HttpResponse.BodyHandlers.ofString());

        UsuarioAPI usuario = new UsuarioAPI();

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        usuario = mapper.readValue(response.body(), UsuarioAPI.class);

        return usuario;

    }
}
