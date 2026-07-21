# Secure Banking API — TDD Test Suite

A **test-first** solution to the *Spring Boot TDD Challenge – Secure Banking API*.

Per the challenge, the production code does **not** exist yet. The deliverable is a
comprehensive automated **test suite** that acts as the *executable specification*
the development team will later implement against.

## Technology stack

- Java 21, Spring Boot 3.3.x
- JUnit 5, Mockito, MockMvc, Spring Boot Test, AssertJ
- Spring Data JPA + H2 (in-memory)
- Spring Security + JWT (JJWT 0.12.x)
- Bean Validation, Jackson

## How to run

```bash
mvn test
```

## What is implemented vs. specified

To let the test suite **compile** and the Spring context **load**, `src/main`
contains the type contracts (entities, DTOs, repository & service interfaces,
controllers). Two categories exist:

| Layer | State | Rationale |
|-------|-------|-----------|
| Entities, DTOs, Repositories | Complete | Data + Spring Data-generated queries |
| Exception handling (`GlobalExceptionHandler`) | Complete | The error-code mapping is itself part of the spec |
| Security (JWT filter, config) | Complete | Infrastructure required to exercise `SecurityTest` |
| **Service implementations** | **Skeleton** (`throw UnsupportedOperationException`) | This is the business logic developers must build |
| **Controller handlers** | **Skeleton** | Same — wired & validated, but logic pending |

Skeleton handlers surface as **`501 Not Implemented`** via the exception handler.

### Expected test result (this is intentional)

Running `mvn test` yields a deliberate red/green split — the essence of TDD:

- **GREEN** — infrastructure already satisfied: `CustomerRepositoryTest`,
  `AccountRepositoryTest`, `SecurityTest`, `GlobalExceptionHandlerTest`.
- **RED** — the executable specification for behaviour not yet built:
  the service, controller and integration tests.

Each red test turns green as the corresponding production logic is implemented —
exactly the workflow the challenge describes.

## The 12 test classes

| Test class | Kind | Verifies |
|------------|------|----------|
| `CustomerServiceTest` | Mockito unit | Unique/mandatory fields, password encryption, duplicate → conflict, not-found |
| `AccountServiceTest` | Mockito unit | Account rules + deposit / withdraw / transfer (incl. insufficient funds, atomicity, 2 ledger records) |
| `TransactionServiceTest` | Mockito unit | Transaction queries + not-found |
| `CustomerControllerTest` | `@WebMvcTest` | CRUD status codes, validation (400), conflict (409), not-found (404) |
| `AccountControllerTest` | `@WebMvcTest` | Account CRUD + banking ops, negative balance / insufficient funds |
| `TransactionControllerTest` | `@WebMvcTest` | Transaction query endpoints |
| `CustomerRepositoryTest` | `@DataJpaTest` | `findByEmail`, `existsByEmail`, unique-email constraint |
| `AccountRepositoryTest` | `@DataJpaTest` | `findByAccountNumber`, `findByCustomerId`, unique constraint |
| `SecurityTest` | `@SpringBootTest` | Public vs protected endpoints; missing / invalid / expired / valid JWT |
| `GlobalExceptionHandlerTest` | Unit | Exception → HTTP status mapping (404/409/400/401/403) |
| `CustomerIntegrationTest` | `@SpringBootTest` | End-to-end register → CRUD lifecycle with JWT |
| `TransferIntegrationTest` | `@SpringBootTest` | End-to-end atomic transfer, balances, two transaction records |

## Test design conventions

- **Arrange–Act–Assert** structure in every test.
- Descriptive method names and `@DisplayName` / `@Nested` grouping.
- Positive, negative and edge cases (e.g. withdrawing the exact balance).
- Shared `TestFixtures` factory removes duplicated setup.
- Successful **and** failure scenarios for each business rule.

## Package layout

```
src/main/java/com/abcbank
  ├── controller   REST endpoints (skeleton handlers)
  ├── service      interfaces + impl/ (skeleton business logic)
  ├── repository   Spring Data JPA interfaces
  ├── entity       Customer, Account, Transaction, enums
  ├── dto          request/response records
  ├── security     JwtService, JwtAuthenticationFilter, SecurityConfig
  └── exception    domain exceptions + GlobalExceptionHandler
src/test/java/com/abcbank
  ├── service | controller | repository | security | exception | integration
  └── testsupport/TestFixtures
```
