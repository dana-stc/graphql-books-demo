package com.example.graphql;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

/** The GraphQL equivalent of a @RestController. Each method answers one field of the schema. */
@Controller
public class BookController {

    private final BookRepository repository;

    public BookController(BookRepository repository) {
        this.repository = repository;
    }

    // --- Query fields: method name == field name in "type Query" ---

    @QueryMapping
    public List<Book> books() {
        return repository.findAllBooks();
    }

    @QueryMapping
    public Book bookById(@Argument String id) {
        return repository.findBook(id).orElse(null); // null is allowed: the schema says Book (nullable)
    }

    @QueryMapping
    public List<Author> authors() {
        return repository.findAllAuthors();
    }

    // --- Mutation ---

    @MutationMapping
    public Book addBook(@Argument AddBookInput input) {
        if (repository.findAuthor(input.authorId()).isEmpty()) {
            throw new IllegalArgumentException("No author with id " + input.authorId());
        }
        return repository.add(input.title(), input.year(), input.authorId());
    }

    // --- Nested fields: only run if the client actually asks for them! ---

    /** Resolves Book.author */
    @SchemaMapping(typeName = "Book", field = "author")
    public Author author(Book book) {
        return repository.findAuthor(book.authorId()).orElseThrow();
    }

    /** Resolves Author.books */
    @SchemaMapping(typeName = "Author", field = "books")
    public List<Book> books(Author author) {
        return repository.findBooksByAuthor(author.id());
    }
}
