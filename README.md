# Library API

Simple CRUD Library API for borrowing, adding books, and adding user

## Tech stack
- Java 17, Spring Boot 3.3
- Spring Data JPA
- PostgreSQL (prod)
- Maven
- springdoc-openapi (Swagger UI)

## Why PostgreSQL

Since this is a simple system with mostly transactional and relational data, i choose an regular sql database.
the main entity are Books, Borrowers, and Loans. A book can only be borrowed by a single borrower. A borrower can borrow multiple books.
and a book cannot be borrowed by multiple borrowers. basically no more than one active loan per book at a time.
Based on that, we can definitely use relational database like MSSQL, ORACLE, or Postgres that all have features like foreign keys, transactions, and row-level locking.

Why PostgreSQL specifically?
- It's free and open source
- I Am mostly familiar with PostgreSQL features because i have been using it daily for 3+ years

## Running locally

1. Install PostgreSQL and create a database:
```sql
   CREATE DATABASE librarydb;
```
2. Configure connection details in `src/main/resources/application.properties`:
```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/librarydb
   spring.datasource.username=postgres
   spring.datasource.password=postgres
   spring.datasource.driver-class-name=org.postgresql.Driver
 
   spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
   spring.jpa.hibernate.ddl-auto=update
   spring.jpa.open-in-view=false
```
(Adjust username/password/port to match your local Postgres setup.)
3. Run the app:
```bash
   ./mvnw spring-boot:run
```
The API is available at `http://localhost:8081` (or whichever port is configured).

## Configuration for multiple environments

The app uses Spring profiles to separate environment-specific configuration:
- `application.properties` — shared/base config, activates a profile via `spring.profiles.active`
- `application-dev.properties` — local development defaults
- `application-prod.properties` — production values, sourced from environment variables
  (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`) with no secrets hardcoded

Switch environments by setting `SPRING_PROFILES_ACTIVE` (e.g. via Docker or a deployment
platform) — no source code changes or rebuild required.

## API Reference

### Borrowers

**Register a borrower**
`POST /api/borrowers`
```json
{ "name": "Jane Doe", "email": "jane@example.com" }
```
→ `200 OK` with the created borrower record. `400` if name/email is blank or email is invalid.
Duplicate email is rejected.

**List all borrowers**
`GET /api/borrowers`
→ `200 OK`, array of borrowers.

**Get a borrower by id**
`GET /api/borrowers/{id}`
→ `200 OK`, or `404 Not Found` if the id doesn't exist.

### Books

**Register a book (copy)**
`POST /api/books`
```json
{ "isbn": "978-0-13-468599-1", "title": "Effective Java", "author": "Joshua Bloch" }
```
→ `200 OK` with the created book record. Registering the same ISBN again with the **same**
title/author creates a new copy with a new id. Registering the same ISBN with a **different**
title or author returns `409 Conflict`.

**List all books**
`GET /api/books`
→ `200 OK`, array of books, each including an `available` boolean (true if that specific copy
has no active loan).

**Get a book by id**
`GET /api/books/{id}`
→ `200 OK`, or `404 Not Found` if the id doesn't exist.

### Loans

**Borrow a book on behalf of a borrower**
`POST /api/loan/borrow?bookId={bookId}&borrowerId={borrowerId}`
→ `200 OK` with the loan record.
→ `404 Not Found` if the book or borrower id doesn't exist.
→ `409 Conflict` if that specific book id is already on loan to someone.

**Return a book on behalf of a borrower**
`POST /api/loan/return?bookId={bookId}&borrowerId={borrowerId}`
→ `200 OK` with the updated loan record.
→ `409 Conflict` if there's no active loan for that book+borrower pair.

**List all loans**
`GET /api/loan`
→ `200 OK`, array of all loan records (past and active).
## Error format

All errors return a consistent shape:
```json
{
  "timestamp": "2026-08-27T12:34:42.608Z",
  "status": 409,
  "error": "Conflict",
  "messages": ["Book with id 1 is currently borrowed"]
}
```
Swagger documentation is also available at http://localhost:8081/swagger-ui/index.html

## Concurrency handling

The rule "no more than one member borrowing the same book id at a time" is
enforced with a database row lock, not just an application-level check. `LoanService.borrow()`
and `LoanService.returnBook()` both call `findByIdForUpdate()`, which issues a
`SELECT ... FOR UPDATE` on the book's row. This means if two borrow requests for the same book
id arrive at nearly the same time, the second one is blocked at the database level until the
first transaction commits — so it always sees the up-to-date loan state before deciding whether
the book is available. Without this lock, both requests could read "available" before either
commits, resulting in two active loans for the same book.

## Assumptions

Since the task left some behavior unspecified, these are the calls made and why:

1. **Borrower email is unique.** You cannot register with an email that is already registered. 
   This is to prevent creating multiple borrower with the same email. The same email will be rejected.
2. **"Book id" means one physical copy, not the ISBN.** Two copies of the same ISBN are
   independently borrowable. In a real library, you can have multiple copy of the same book. There will
   always be a unique identifier, in this case, the book id.
3. **A borrower can hold multiple different books at once.** There is no limit that the borrower needs to follow.
   they can borrow as much books as they want.
4. **Returning requires the same borrower/book pair used to borrow.** Returning a book that
   someone else borrowed is rejected. 
5. **No due dates or late fees.** We only tracked when is the book is borrowed and returned. there is no
   penalty when books are borrowed for a long time.
6. **No authentication/authorization layer.** The task doesn't mention users/roles beyond "API
   user," so all endpoints are open. Would add proper auth before this went to production.
7. **ISBN format isn't strictly validated** (no ISBN-10/13 checksum check) — just required as a
   non-blank string, since the task doesn't specify the exact format expected.

## Unit Test
i have also added some simple unit test using Springtest. Just run the ./mvnw test and the test will run.

## Possible next steps

- Pagination on `GET /api/books`
- Flyway/Liquibase migrations instead of `ddl-auto`
- CI pipeline running build + tests on push
- Kubernetes manifests for deployment
- Validation for late fee or due date
- Authentication layer for hitting api
- Add logging for clearer debugging
- cleaner formating and more specific data validation for creating new books or new user / borrower.
## 12-Factor Conformance

This project follows several of the [12-factor app](https://12factor.net/) principles:

- **Config**: environment-specific settings (DB connection, credentials) live in Spring profiles
  and environment variables, never hardcoded into application logic.
- **Backing services**: PostgreSQL is treated as an attached resource — swappable between local
  and Docker environments through config alone.
- **Dependencies**: explicitly declared in `pom.xml`, isolated via Maven.
- **Processes**: the app is stateless; all state lives in the database, so it could run as
  multiple instances without issue.
- **Disposability**: fast startup and graceful shutdown (verified in logs) support quick
  restarts.
- **Dev/prod parity**: the same database engine (Postgres) is used in both dev and prod
  configurations, avoiding the drift that comes from using a different DB locally.
- **Logs**: output goes to stdout by default, which Docker captures as a log stream rather than
  the app managing log files itself.

Not fully addressed: a formal build/release/run pipeline with immutable, versioned release
artifacts (this would come from a CI/CD pipeline, which wasn't set up here), horizontal
concurrency testing across multiple running instances, and admin/maintenance process tooling
(no one-off scripts were required for this project's current feature set).
