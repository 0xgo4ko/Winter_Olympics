# Winter Olympics

Spring Boot web application for managing Winter Olympics competitions, athlete registration, competition results, and public rankings.

This project was built for the CSCB869 Java Web Services course.

## Overview

The application supports two winter competition types:

- Ski Slalom
- Biathlon

Athletes can register, manage their profile, and join available competitions. Admins manage competitions and manually enter athlete performance results. Guests can browse competitions and public results without logging in.

## Tech Stack

- Java 21
- Spring Boot 4
- Spring MVC
- Spring Security
- Spring Data JPA / Hibernate
- Thymeleaf
- MySQL
- Bean Validation
- Gradle
- JUnit / Mockito

## Main Features

### Authentication and Users

- Athlete registration
- Login and logout with Spring Security
- Password hashing with BCrypt
- User profile page
- Profile update
- Profile deletion
- Role-based authorization

Roles:

- `ADMIN`
- `ATHLETE`
- Guest/public user

### Competition Management

Admins can:

- Create competitions
- Edit competitions
- Delete competitions
- Start competitions after the registration deadline
- Manage participant results
- End competitions

Athletes can:

- View competitions
- Join one competition
- Leave a competition before it starts

Guests can:

- View competitions
- View competition details
- View public final results

## Competition Types

### Ski Slalom

Ski slalom has two runs.

Admin workflow:

1. Start the competition.
2. Enter first-run times in seconds.
3. Mark DNF athletes when needed.
4. Start the second run.
5. Only the fastest configured number of athletes qualify.
6. Enter second-run times in seconds.
7. Mark second-run DNF athletes when needed.
8. End the competition.

Final time:

```text
first run time + second run time
```

DNF athletes are excluded from medal ranking and appear at the bottom.

### Biathlon

Biathlon has one result-entry phase.

Competition setup includes:

- Number of laps
- Number of targets
- Penalty seconds per missed target

Admin workflow:

1. Start the competition.
2. Enter athlete time in seconds.
3. Enter missed targets.
4. Mark DNF athletes when needed.
5. End the competition.

Final time:

```text
base time + missed targets * penalty seconds
```

DNF athletes are excluded from medal ranking and appear at the bottom.

## Public Results

The Results page contains public queries for ended competitions:

- Country medal count
- Average participant age
- Youngest and oldest medalist

Competition details pages also show final rankings per competition, including highlighted gold, silver, and bronze medalists.

## Seeded Data

The application seeds default data on startup:

- Roles: `ADMIN`, `ATHLETE`
- Admin user:
  - username: `admin`
  - password: `admin123`
- Ski slalom competition:
  - `Men Ski Slalom`
  - 10 seeded athletes
  - top 5 qualify for the second run
- Biathlon competition:
  - `Men Biathlon`
  - 5 seeded athletes
  - 2 laps
  - 3 targets
  - 60 seconds penalty per missed target

Seeded athlete password:

```text
athlete123
```

The seed logic is idempotent, so restarting the app does not duplicate the same seed records.

## Running the App

The app expects a local MySQL server.

Default configuration:

```properties
server.port=8084
spring.datasource.url=jdbc:mysql://localhost:3306/winter_olympics?createDatabaseIfNotExist=true&useSSL=true
spring.datasource.username=my_user
spring.datasource.password=my_user_for_test_not_important
```

Run:

```powershell
.\gradlew.bat bootRun
```

Open:

```text
http://localhost:8084
```

## Tests

Run all tests:

```powershell
.\gradlew.bat test
```

The project includes service-level unit tests for business logic, validation rules, and competition result workflows.

## Project Structure

Important packages:

- `config` - security and seed data
- `core.service` - service interfaces
- `core.service.impl` - business logic
- `data.entity` - JPA entities
- `data.repo` - Spring Data repositories
- `dto` - view and form DTOs
- `exception` - domain exceptions
- `web.controller` - MVC controllers
- `web.exception` - global exception handling
- `templates` - Thymeleaf views
- `static/css` - page styling

## Notes

- Times are stored and displayed in seconds with decimal precision.
- Public data is read-only.
- Admin-only actions are protected by Spring Security.
- Validation errors are shown through friendly page messages/modals where possible.
