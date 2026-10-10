# GraphQL Books Demo (Spring Boot) – a beginner's guide

A tiny GraphQL API: **list books and authors, look up a book by id, and add a new book**.
Data is kept in memory (no database), so the code stays focused on GraphQL.

## 1. What is GraphQL?

GraphQL is a way for a client to **ask for exactly the data it needs** from an API, in one request.
Instead of many URLs, there is **one endpoint** (`/graphql`) and the client sends a *query* describing
the shape of the answer it wants.

| | REST | GraphQL |
|---|---|---|
| URLs | many (`/books`, `/books/1`, `/authors/1`) | **one** (`/graphql`) |
| Response shape | fixed by the server | **chosen by the client** |
| Related data | extra requests or special endpoints | nested in the same query |
| Contract | OpenAPI (optional) | the **schema** (required, built in) |
| Over/under-fetching | common | avoided |
| Errors | HTTP status codes | HTTP 200 with an `errors` list in the JSON |

### Example

Request (POST `/graphql`):

```graphql
{
  bookById(id: 1) {
    title
    author { name }
  }
}
```

Response contains *only* what was asked:

```json
{ "data": { "bookById": { "title": "Dune", "author": { "name": "Frank Herbert" } } } }
```

Ask for `year` too and it appears; leave out `author` and the server never even looks it up.

### The key words

- **Schema** – the contract: the types and the operations that exist (`schema.graphqls`).
- **Query** – read data (like GET).
- **Mutation** – change data (like POST/PUT/DELETE).
- **Type / field** – `Book` has fields `id`, `title`, `year`, `author`. `!` means "never null".
- **Resolver** – a Java method that produces the value of one field.

## 2. How this project works (schema-first)

We write the schema first, then Java methods whose names match it.

```
schema.graphqls  ──(startup)──>  Spring checks that every Query/Mutation field has a Java method

Client ──POST /graphql { query }──> Spring GraphQL ──> BookController method ──> BookRepository
```

## 3. The files, one by one

| File | What it does |
|---|---|
| `src/main/resources/graphql/schema.graphqls` | **The contract.** Defines `Query`, `Mutation`, `Book`, `Author`, and `AddBookInput`. |
| `BookController.java` | **Your logic.** `@QueryMapping` / `@MutationMapping` = "this method answers the schema field with the same name". `@SchemaMapping` answers a *nested* field (`Book.author`, `Author.books`). Think `@RestController`, but per field. |
| `Book.java`, `Author.java`, `AddBookInput.java` | Plain Java records matching the schema types. |
| `BookRepository.java` | In-memory storage with 3 authors and 4 books. |
| `application.properties` | Enables **GraphiQL**, a browser playground, and schema printing. |
| `requests/*.graphql` | Ready-made sample queries. |
| `BookControllerTest.java` | Starts the app and checks queries, nesting, the mutation and an error. |

A detail worth noticing: `Book` the record stores only `authorId`, but the GraphQL `Book` type has an
`author` field. The `author(Book)` method in `BookController` fills it in, and **only when a client asks
for it**.

## 4. Run it

Requires Java 17+. Maven is included via the wrapper (`mvnw`).

```bash
./mvnw spring-boot:run
```

### Play in the browser

Open <http://localhost:8080/graphiql>. Paste a query on the left, press ▶. The **Docs** panel shows the whole schema
and auto-completes as you type (Ctrl+Space).

### Or use curl

```bash
curl -s -H "Content-Type: application/json" \
  -d '{"query":"{ bookById(id: 1) { title year author { name } } }"}' \
  http://localhost:8080/graphql
```

Things to try in GraphiQL:

```graphql
# all books, only titles
{ books { title } }

# authors with their books (nested the other way round)
{ authors { name books { title year } } }

# add a book
mutation {
  addBook(input: { title: "Messiah", year: 1969, authorId: "a1" }) {
    id
    title
    author { name }
  }
}

# ask for several things in ONE request, with aliases
{ dune: bookById(id: 1) { title } hobbit: bookById(id: 4) { title } }
```

Unknown id (`bookById(id: 99)`) returns `"bookById": null`, because the schema declares the field nullable.
Adding a book with an unknown `authorId` returns an `errors` entry (note: still HTTP 200!).

### Run the tests

```bash
./mvnw test
```

(One test deliberately triggers an error, so an `ERROR` log line during tests is expected.)

## 5. Try changing it

1. Add a `genre: String` field to `Book` in the schema and the record, then query it.
2. Add `deleteBook(id: ID!): Boolean!` to `Mutation` and a `@MutationMapping` method.
3. Add a `booksByYear(from: Int!, to: Int!)` query that takes two `@Argument`s.
4. Restart and look in GraphiQL's Docs panel: your changes are documented automatically.

## 6. Common gotchas

- **Names must match.** Method `bookById` ↔ schema field `bookById`. If they don't, the app fails at startup
  with a clear "unmapped field" message.
- **Always POST** JSON `{"query": "..."}` with `Content-Type: application/json`.
- **Errors are HTTP 200.** Always check for an `errors` key in the response.
- **N+1 problem.** Resolving `author` for 1000 books runs 1000 lookups. Real apps fix this with
  `@BatchMapping`; it's skipped here to keep things simple.
