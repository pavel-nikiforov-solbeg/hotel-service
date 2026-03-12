# Hotel Service API

A Spring Boot REST API designed for managing hotel listings with search, filtering, and data aggregation capabilities.

## Tech Stack

* **Core:** Java 21 & Spring Boot 3.5.11
* **Database:** H2 (In-memory) with Liquibase for versioned migrations.
* **Mapping:** MapStruct 1.6.3 & Lombok.
* **Documentation:** Springdoc-OpenAPI 2.8.6 (Swagger UI).
* **Utilities:** Java Records for DTOs, Jakarta Validation.

## Getting Started

### Prerequisites
* **JDK 21** (Ensure your `JAVA_HOME` is set correctly).
* **Maven Wrapper** (Included as `./mvnw`).

### 1. Environment Configuration
Create a `.env` file in the project root to configure database settings:

```properties
DB_URL=jdbc:h2:mem:hoteldb;DB_CLOSE_DELAY=-1;INIT=CREATE SCHEMA IF NOT EXISTS PUBLIC
DB_DRIVER=org.h2.Driver
DB_USERNAME=sa
DB_PASSWORD=

```

### 2. Execution Commands
**Run App**     ./mvnw spring-boot:run
**Run Tests**   ./mvnw test 

The server starts on **port 8092**.

## API Reference

**Base Path:** `/property-view`

### Core Endpoints

| Method | Path | Description |
| --- | --- | --- |
| `GET` | `/hotels` | Retrieve all hotels (Brief View) with pagination. |
| `GET` | `/hotels/{id}` | Get full details for a specific hotel. |
| `GET` | `/search` | Advanced search using dynamic filters. |
| `POST` | `/hotels` | Create a new hotel listing. |
| `POST` | `/hotels/{id}/amenities` | Bulk add amenities to an existing hotel. |
| `GET` | `/histogram/{param}` | Aggregate statistics (count) by a specific field. |

### Search & Statistics

**Pagination:** Defaults to 20 items per page, sorted by ID (ASC).

#### Search Filters (`GET /search`)

* **`name`, `brand`, `city`, `country**`: Case-insensitive partial matching.
* **`amenities`**: Returns hotels containing all requested amenity strings.

#### Histogram Grouping (`GET /histogram/{param}`)

Supported parameters: `brand`, `city`, `country`, `amenities`. Returns a Map of value to count.

## Data Handling

### Request Body: Create Hotel (`POST /hotels`)

```json
{
  "name": "Grand Palace",
  "brand": "Luxury Group",
  "address": {
    "houseNumber": "12B",
    "street": "High St",
    "city": "London",
    "country": "UK",
    "postCode": "220000"
  },
  "contacts": {
    "phone": "+447700900000",
    "email": "stay@grandpalace.com"
  },
  "arrivalTime": {
    "checkIn": "14:00",
    "checkOut": "12:00"
  }
}

```

### Validation Rules

* **postCode**: Must be exactly 6 digits.
* **phone**: Must start with `+` and contain between 10 and 20 characters.
* **arrivalTime**: Standard `HH:mm` format.

## Project Architecture

* **config/**: OpenAPI/Swagger and Dotenv EnvironmentPostProcessor.
* **controller/**: REST layer handling requests and Jakarta validation.
* **dto/**: Immutable Java Records for data transfer.
* **entity/**: JPA entities (Address, Contacts, and ArrivalTime are @Embeddable).
* **repository/**: JPA repositories using Specification for dynamic queries.
* **service/**: Transactional business logic (Service/ServiceImpl pattern).

## Development Notes

* **OSIV Disabled**: `spring.jpa.open-in-view` is set to false for predictable database sessions.
* **Transactional Integrity**: `HotelServiceImpl` is @Transactional(readOnly = true) by default, with write access enabled for modification methods.
* **H2 Console**: Available at `http://localhost:8092/h2-console`.