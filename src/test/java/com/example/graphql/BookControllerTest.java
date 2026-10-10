package com.example.graphql;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BookControllerTest {

    @LocalServerPort
    int port;

    private String graphql(String query) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new RestTemplate().postForObject("http://localhost:" + port + "/graphql",
                new HttpEntity<>(Map.of("query", query), headers), String.class);
    }

    @Test
    void bookByIdWithNestedAuthor() {
        String json = graphql("{ bookById(id: 1) { title author { name } } }");
        assertTrue(json.contains("Dune") && json.contains("Frank Herbert"), json);
    }

    @Test
    void unknownBookIsNull() {
        String json = graphql("{ bookById(id: 99) { title } }");
        assertTrue(json.contains("\"bookById\":null"), json);
    }

    @Test
    void addBookMutation() {
        String json = graphql("mutation { addBook(input: {title: \"Emma\", year: 1815, authorId: \"a2\"}) { id title } }");
        assertTrue(json.contains("Emma"), json);
    }

    @Test
    void addBookWithUnknownAuthorReturnsError() {
        String json = graphql("mutation { addBook(input: {title: \"X\", year: 1, authorId: \"zzz\"}) { id } }");
        assertTrue(json.contains("errors"), json);
    }

    @Test
    void authorWithBooks() {
        String json = graphql("{ authors { name books { title } } }");
        assertTrue(json.contains("Children of Dune"), json);
        assertFalse(json.contains("errors"), json);
    }
}
