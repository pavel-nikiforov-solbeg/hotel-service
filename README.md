# Hotel Service

A Spring Boot REST API for managing hotel listings with search and filtering capabilities.

## Tech Stack

- Java 21
- Spring Boot 3.5.11 (Web, Data JPA, Validation, Actuator)
- H2 in-memory database
- Liquibase (schema migrations + seed data)
- MapStruct 1.6.3
- Lombok
- springdoc-openapi 2.8.6 (Swagger UI)

## Prerequisites

- JDK 21 (system Java may differ — see [Running](#running))
- Maven wrapper included (`./mvnw`)

## Getting Started

### 1. Configure environment

Create a `.env` file in the project root (or copy the example below):

```
DB_URL=jdbc:h2:mem:hoteldb;DB_CLOSE_DELAY=-1;INIT=CREATE SCHEMA IF NOT EXISTS PUBLIC
DB_DRIVER=org.h2.Driver
DB_USERNAME=
DB_PASSWORD=
```

The application reads this file via a custom `EnvironmentPostProcessor`. CLI/environment variables take precedence over `.env`.

### 2. Run the application

If Java 21 is not your system default, set `JAVA_HOME` explicitly:

```bash
JAVA_HOME="C:/Program Files/Java/jdk-21" ./mvnw spring-boot:run
```

The server starts on **port 8092**.

### 3. Run tests

```bash
JAVA_HOME="C:/Program Files/Java/jdk-21" ./mvnw test
```

Tests use a separate `src/test/resources/application.yaml` with a hardcoded H2 URL, so no `.env` is needed for testing.

## API

Base path: `/property-view`

Interactive docs available at: `http://localhost:8092/swagger-ui.html`
OpenAPI spec: `http://localhost:8092/v3/api-docs`

### Endpoints

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/hotels` | List all hotels (brief view) |
| `GET` | `/hotels/{id}` | Get full hotel details |
| `GET` | `/search` | Search hotels by filters |
| `POST` | `/hotels` | Create a new hotel (returns 201) |
| `POST` | `/hotels/{id}/amenities` | Add amenities to a hotel |
| `GET` | `/histogram/{param}` | Count hotels grouped by field |

### Search parameters (`GET /search`)

All parameters are optional and combinable:

| Param | Type | Description |
|-------|------|-------------|
| `name` | string | Partial match on hotel name |
| `brand` | string | Partial match on brand |
| `city` | string | Partial match on city |
| `country` | string | Partial match on country |
| `amenities` | string[] | Hotels that have all listed amenities |

### Histogram parameters (`GET /histogram/{param}`)

Valid values: `brand`, `city`, `country`, `amenities`

Returns a `Map<String, Long>` of value → count.

### Request body: create hotel (`POST /hotels`)

```json
{
  "name": "Grand Hotel",
  "description": "Optional description",
  "brand": "Marriott",
  "address": {
    "houseNumber": 10,
    "street": "Main Street",
    "city": "Minsk",
    "country": "Belarus",
    "postCode": "220000"
  },
  "contacts": {
    "phone": "+375291234567",
    "email": "info@grandhotel.com"
  },
  "arrivalTime": {
    "checkIn": "14:00",
    "checkOut": "12:00"
  }
}
```

`name` and `brand` are required; all other fields are optional.

## Project Structure

```
src/main/java/com/example/hotelservice/
├── config/          # OpenApiConfig, DotenvConfig (EnvironmentPostProcessor)
├── controller/      # HotelController
├── dto/             # Java records: HotelBriefDto, HotelFullDto, HotelCreateDto, ...
├── entity/          # Hotel, Address (embeddable), Contacts (embeddable), ArrivalTime (embeddable)
├── exception/       # HotelNotFoundException, InvalidHistogramParameterException, GlobalExceptionHandler
├── mapper/          # HotelMapper (MapStruct)
├── repository/      # HotelRepository (JPA + Specification), HotelSpecification
└── service/         # HotelService interface, HotelServiceImpl

src/main/resources/
├── application.yaml
└── db/changelog/
    ├── db.changelog-master.yaml
    ├── 001-create-tables.yaml   # DDL
    └── 002-load-data.yaml       # 10 seed hotels + amenities
```

## Development Notes

- **OSIV is disabled** (`spring.jpa.open-in-view: false`). Lazy associations must be loaded within a transaction.
- **`HotelServiceImpl`** is `@Transactional(readOnly = true)` at class level; write methods override with `@Transactional`.
- **DTOs are Java records** — use canonical constructors, not builders.
- **`HotelSpecification`** is a `final` utility class with static factory methods; LIKE wildcards are escaped.
- **H2 console** is available at `http://localhost:8092/h2-console` (JDBC URL: `jdbc:h2:mem:hoteldb`).
