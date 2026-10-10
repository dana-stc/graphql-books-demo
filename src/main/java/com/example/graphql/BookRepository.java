package com.example.graphql;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/** A fake "database": data lives in memory and vanishes when the app stops. */
@Component
public class BookRepository {

    private final Map<String, Author> authors = new ConcurrentHashMap<>();
    private final Map<String, Book> books = new ConcurrentHashMap<>();
    private final AtomicInteger nextBookId = new AtomicInteger(1);

    public BookRepository() {
        authors.put("a1", new Author("a1", "Frank Herbert"));
        authors.put("a2", new Author("a2", "George Orwell"));
        authors.put("a3", new Author("a3", "J.R.R. Tolkien"));
        add("Dune", 1965, "a1");
        add("Children of Dune", 1976, "a1");
        add("1984", 1949, "a2");
        add("The Hobbit", 1937, "a3");
    }

    public List<Book> findAllBooks() {
        return books.values().stream().sorted((x, y) -> Integer.compare(
                Integer.parseInt(x.id()), Integer.parseInt(y.id()))).toList();
    }

    public Optional<Book> findBook(String id) {
        return Optional.ofNullable(books.get(id));
    }

    public List<Book> findBooksByAuthor(String authorId) {
        return findAllBooks().stream().filter(b -> b.authorId().equals(authorId)).toList();
    }

    public List<Author> findAllAuthors() {
        return authors.values().stream().sorted((x, y) -> x.id().compareTo(y.id())).toList();
    }

    public Optional<Author> findAuthor(String id) {
        return Optional.ofNullable(authors.get(id));
    }

    public Book add(String title, int year, String authorId) {
        Book book = new Book(String.valueOf(nextBookId.getAndIncrement()), title, year, authorId);
        books.put(book.id(), book);
        return book;
    }
}
