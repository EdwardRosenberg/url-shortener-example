# URL Shortener Example

A Spring Boot application demonstrating project skeleton and guardrails setup with Gradle, multiple configuration profiles, and CI/CD pipeline.

## Prerequisites

- Java 17 or higher
- Gradle 8.5+ (included via wrapper)

## Getting Started

### Build the Project

```bash
./gradlew build
```

### Run the Application

Run with default (local) profile:
```bash
./gradlew bootRun
```

Run with a specific profile:
```bash
# Development profile (port 8081)
./gradlew bootRun --args='--spring.profiles.active=dev'

# Production profile (port 8082)
./gradlew bootRun --args='--spring.profiles.active=prod'
```

### Health Check

Once the application is running, you can access the health endpoint:

```bash
curl http://localhost:8080/actuator/health
```

Expected response:
```json
{"status":"UP"}
```

## Configuration Profiles

The application supports three profiles with different configurations:

- **local** (default): Port 8080, DEBUG logging
- **dev**: Port 8081, INFO logging with additional actuator endpoints
- **prod**: Port 8082, WARN logging, restricted actuator endpoints

### Environment Variables

You can externalize secrets and configuration using environment variables:

1. Copy `.env.example` to `.env`
2. Update the values in `.env`
3. **Important:** The `.env` file must be in Java properties format (not standard dotenv format). Each line should be `key=value`, with no spaces around the equals sign, and values should be properly escaped if they contain special characters. For example:
## Code Formatting

This project uses [Spotless](https://github.com/diffplug/spotless) with Google Java Format for consistent code style.

### Check Formatting

```bash
./gradlew spotlessCheck
```

### Apply Formatting

```bash
./gradlew spotlessApply
```

## Testing

Run all tests:
```bash
./gradlew test
```

## CI Pipeline

The project includes a GitHub Actions CI pipeline that:

1. Checks code formatting with Spotless
2. Builds the project
3. Runs all unit tests
4. Uploads test results

The pipeline runs on:
- Push to `main`/`master` branch
- Pull requests to `main`/`master` branch

**PRs will fail if:**
- Code formatting violations are detected
- Tests fail
- Build fails

## Project Structure

```
.
├── .github/
│   └── workflows/
│       └── ci.yml                 # CI pipeline configuration
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/urlshortener/
│   │   │       └── UrlShortenerApplication.java
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── application-local.properties
│   │       ├── application-dev.properties
│   │       └── application-prod.properties
│   └── test/
│       └── java/
│           └── com/example/urlshortener/
│               └── UrlShortenerApplicationTests.java
├── build.gradle                   # Gradle build configuration
├── settings.gradle                # Gradle settings
├── gradle.properties              # Gradle properties
└── .env.example                   # Example environment variables
```

## Acceptance Criteria

✅ `./gradlew bootRun` starts the application
✅ Health endpoint available at `/actuator/health`
✅ Switching profiles changes ports and environment variables
✅ CI pipeline fails on formatting violations or test failures