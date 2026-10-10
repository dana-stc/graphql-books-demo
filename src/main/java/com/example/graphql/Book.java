package com.example.graphql;

/**
 * Matches the "Book" type, except GraphQL's "author" field is NOT stored here:
 * we only keep authorId and resolve the full Author on demand (see BookController).
 */
public record Book(String id, String title, int year, String authorId) {
}
