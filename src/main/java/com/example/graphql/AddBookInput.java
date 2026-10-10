package com.example.graphql;

/** Matches "input AddBookInput" in the schema. */
public record AddBookInput(String title, int year, String authorId) {
}
