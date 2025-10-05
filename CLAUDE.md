# Claude Development Guidelines

This file contains instructions and context for AI assistants working on this project.

## Project Overview

**Project Name:** CCApi  
**Description:** A backend service to handle the CCBot interactions  
**Tech Stack:** Kotlin, Spring Boot, Gradle, PostgreSQL, H2 (dev)  
**Database:** Neon (PostgreSQL for production)

## Rules

- To ensure you have correctly loaded this file, start the first reply in a conversation with "Claude guidelines loaded"
- When making ANY changes to code, configuration, or project files:
    - **Always walk through each change step-by-step**
    - **Explain the WHAT**: Describe exactly what is being changed
    - **Explain the WHY**: Provide detailed reasoning for why this change is necessary or beneficial
    - **Explain the HOW**: Show how the change improves the code, fixes issues, or adds functionality
    - **Educational focus**: Frame explanations as learning opportunities, not just fixes
    - **Context matters**: Explain how changes fit into the broader project architecture and goals
- Treat every change as a teaching moment to help the user understand and improve their development skills

## Development Standards

### Code Style

- Follow Kotlin coding conventions
- Use ktlint for code formatting (configured in build.gradle.kts)
- Maintain existing code structure and patterns
- Add meaningful validation messages and error handling

### Dependencies

- Use Spring Boot 3.x ecosystem
- Prefer Spring Boot starters over individual dependencies
- Keep dependencies up to date for security
- Only add new dependencies when absolutely necessary

### Database

- Use JPA/Hibernate for data access
- Development: H2 in-memory database
- Production: PostgreSQL via Neon
- Use environment variables for database credentials

### Testing

- Write unit tests for new functionality
- Use JUnit 5 and Spring Boot Test
- Test validation rules and error cases
- Maintain existing test structure

### Security Priorities

Based on the TODO list in README.md, focus on:

1. Input sanitization and validation
2. Authentication and authorization (JWT or OAuth)
3. Rate limiting and throttling
4. Logging and monitoring
5. Error handling and reporting

### API Design

- Follow REST conventions
- Use appropriate HTTP status codes
- Validate all input data
- Return consistent error responses
- Use `/api/v1/` prefix for versioning

### File Structure

```
src/
├── main/kotlin/com/chesire/capi/
│   ├── challenge/
│   │   ├── dto/
│   │   ├── data/
│   │   └── service/
│   ├── events/
│   ├── models/
│   └── ping/
└── test/kotlin/com/chesire/capi/
```

### Branch Strategy

- Create feature branches for new functionality
- Use descriptive branch names (e.g., `feature/add-input-validation`)
- Keep changes focused and atomic

### Configuration

- Environment-specific configs in application-{profile}.yaml
- Never commit secrets or credentials
- Use environment variables for sensitive data
- H2 console should only be enabled in development

## Current Status

### Implemented Features

- Basic CRUD operations for challenges
- JPA entities and repositories
- REST controllers with basic error handling
- Development and production configurations

### Known Issues & TODOs

- No authentication/authorization (critical)
- No input validation (high priority)
- No rate limiting
- Generic error handling
- H2 console enabled in dev (security concern)
- Missing audit logging

### Immediate Priorities

1. Add input validation with Jakarta Validation
2. Implement proper error handling
3. Add authentication framework
4. Security headers and CORS configuration

## Guidelines for Changes

### Making Changes

- Make minimal, focused changes
- Don't break existing functionality
- Add validation to all user inputs
- Follow existing patterns and conventions
- Update tests when adding new features
- **Always provide educational explanations:**
    - Walk through each change methodically
    - Explain the reasoning behind design decisions
    - Highlight best practices and anti-patterns
    - Connect changes to broader software engineering principles
    - Help the user understand trade-offs and alternatives considered

### Security Focus

- Validate all inputs at controller level
- Use proper HTTP status codes
- Never expose stack traces to clients
- Log security-relevant events
- Follow OWASP guidelines

### Testing Changes

- Run `./gradlew clean build` before committing
- Ensure all tests pass
- Add tests for new validation rules
- Test error conditions and edge cases

### Testing Guidelines

#### Test Development Approach

- **Add tests incrementally, one at a time** - Never add multiple tests simultaneously
- **Explain each test thoroughly** before writing it:
  - **WHAT** the test is verifying (the specific behavior or scenario)
  - **WHY** this test is important (what could break without it)
  - **HOW** the test works (the setup, execution, and assertions)
- **Educational focus**: Treat each test as a learning opportunity
- **Build understanding gradually**: Start with simple happy path tests, then add edge cases
- **Verify each test passes** before moving to the next one

#### Test Structure and Format

- **Use Given-When-Then format** for all tests to ensure clarity and consistency
- **Use spacing (not comments)** to separate Given, When, Then sections - NO inline comments explaining Given/When/Then
- **Group related tests** using `@Nested` inner classes for better organization
- **Use descriptive test names** that explain the expected behavior
- **Test boundary conditions** and edge cases explicitly

**Example Format:**
```kotlin
@Test
@DisplayName("Should reject empty name")
fun shouldRejectEmptyName() {
    val dto = createValidDto().copy(name = "")
    
    val violations = validate(dto)
    
    assertThat(violations).hasSize(1)
    assertThat(violations.first().message).contains("name is required")
}
```

#### Test Categories

- **Unit Tests**: Fast, isolated tests for individual components (DTOs, services)
- **Integration Tests**: Test component interactions with Spring context
- **End-to-End Tests**: Test complete request/response cycles

<tool_calling>
You have the capability to call multiple tools in a single response. For maximum efficiency, whenever you need to perform multiple independent operations, ALWAYS call tools simultaneously whenever the actions can be done in parallel rather than sequentially.
Especially when exploring repository, searching, reading files, viewing directories, validating changes, reporting progress or replying to comments. For Example you can read 3 different files parallelly, or report progress and edit different files in parallel. Always report progress in parallel with other tool calls that follow it as it does not depend on the result of those calls.
However, if some tool calls depend on previous calls to inform dependent values like the parameters, do NOT call these tools in parallel and instead call them sequentially.
</tool_calling>
