# CCApi Integration Tests

Comprehensive integration test suite using REST Assured to test all API endpoints against a running server instance.

## Overview

These tests verify the API's behavior by making real HTTP requests to a running CCApi server. They test:
- Authentication and authorization
- Request/response validation
- Business logic
- Error handling
- Security controls

## Running the Tests

### Prerequisites

**A running CCApi server instance is required.** Start the server before running tests:

```bash
# Terminal 1: Start the server
./gradlew bootRun

# Terminal 2: Run integration tests
./gradlew integrationTest
```

### Run All Integration Tests

```bash
./gradlew integrationTest
```

### Run Specific Test Class

```bash
./gradlew integrationTest --tests "*ChallengeIntegrationTest"
./gradlew integrationTest --tests "*EventIntegrationTest"
./gradlew integrationTest --tests "*AuthIntegrationTest"
```

### Run Specific Test Method

```bash
./gradlew integrationTest --tests "*ChallengeIntegrationTest.shouldCreateChallenge"
```

## Configuration

Configure the test environment using environment variables or system properties:

### Environment Variables

```bash
export TEST_SERVER_URL=http://localhost:8080
export TEST_API_KEY=dev-default-api-key-extended

./gradlew integrationTest
```

### System Properties

```bash
./gradlew integrationTest \
  -Dtest.server.url=http://localhost:8080 \
  -Dtest.api.key=dev-default-api-key-extended
```

### Default Values

If not configured, tests use these defaults:
- **Server URL**: `http://localhost:8080`
- **API Key**: `dev-default-api-key-extended`

## Test Structure

### Base Class: `IntegrationTestBase`

All integration tests extend this base class which provides:
- REST Assured configuration
- JWT token management
- Common request specifications
- Helper methods for authentication

### Test Organization

Tests are organized by API domain:

```
integration/
├── README.md
├── IntegrationTestBase.kt
├── auth/
│   └── AuthIntegrationTest.kt
├── challenge/
│   └── ChallengeIntegrationTest.kt
└── event/
    └── EventIntegrationTest.kt
```

### Test Naming Convention

- Test classes: `{Domain}IntegrationTest`
- Test methods: `should{ExpectedBehavior}`
- Nested classes: Group related tests by endpoint

## Test Coverage

### Authentication (`AuthIntegrationTest`)
- ✅ Generate JWT token with valid API key
- ✅ Reject missing API key
- ✅ Reject invalid API key
- ✅ Reject malformed request
- ✅ Validate JWT token format

### Challenge (`ChallengeIntegrationTest`)
- ✅ Create challenge with valid data
- ✅ Retrieve challenge by ID
- ✅ Retrieve challenges by user ID
- ✅ Delete challenge
- ✅ Validation: name length, cheats range
- ✅ Security: authentication required
- ✅ Edge cases: non-existent resources, negative IDs

### Event (`EventIntegrationTest`)
- ✅ Create event with valid data
- ✅ Retrieve events by key
- ✅ Validation: key/value length, user ID
- ✅ Security: authentication required, guild isolation
- ✅ Edge cases: non-existent keys, empty results

## Writing New Tests

### Example Test

```kotlin
@DisplayName("My API Integration Tests")
class MyIntegrationTest : IntegrationTestBase() {

    @Test
    @DisplayName("Should do something successfully")
    fun shouldDoSomething() {
        // Given
        val requestBody = """{"field": "value"}"""

        // When & Then
        given()
            .spec(givenAuthenticated())  // Adds JWT auth
            .body(requestBody)
        .`when`()
            .post("$baseUrl/api/v1/myendpoint")
        .then()
            .statusCode(200)
            .body("field", equalTo("value"))
    }
}
```

### Best Practices

1. **Use `givenAuthenticated()`** for endpoints requiring JWT
2. **Use `given()`** for unauthenticated requests (auth tests)
3. **Make tests independent** - don't rely on test execution order
4. **Create test data per test** - use helper methods like `createTestChallenge()`
5. **Use unique identifiers** - timestamp-based keys prevent test interference
6. **Test both success and failure** - happy path AND validation/error cases
7. **Use descriptive names** - `@DisplayName` should explain the test intent

## REST Assured Patterns

### Basic Request

```kotlin
given()
    .spec(givenAuthenticated())
    .body("""{"key": "value"}""")
.`when`()
    .post("$baseUrl/api/v1/endpoint")
.then()
    .statusCode(200)
```

### Path Parameters

```kotlin
given()
    .spec(givenAuthenticated())
.`when`()
    .get("$baseUrl/api/v1/challenges/{id}", 123)
.then()
    .statusCode(200)
```

### Extracting Response Data

```kotlin
val challengeId = given()
    .spec(givenAuthenticated())
    .body(requestBody)
    .post("$baseUrl/api/v1/challenges")
    .then()
    .statusCode(200)
    .extract()
    .path<Int>("id")
```

### Multiple Assertions

```kotlin
given()
    .spec(givenAuthenticated())
.`when`()
    .get("$baseUrl/api/v1/challenges/1")
.then()
    .statusCode(200)
    .body("id", equalTo(1))
    .body("name", notNullValue())
    .body("cheats", greaterThan(-1))
```

## Troubleshooting

### Server Not Running

```
Connection refused: connect
```

**Solution**: Start the server with `./gradlew bootRun` before running tests.

### Authentication Failures

```
HTTP 403 Forbidden
```

**Solution**: Check that `TEST_API_KEY` matches the server's configured API key.

### Test Failures Due to Data Conflicts

**Solution**: Integration tests use unique identifiers (timestamps) to avoid conflicts. If issues persist, restart the server to clear the H2 database.

### Verbose Logging

Enable detailed REST Assured logging in `IntegrationTestBase`:

```kotlin
RestAssured.enableLoggingOfRequestAndResponseIfValidationFails()
```

Or for all requests:

```kotlin
.log().all()  // Log request
.then()
.log().all()  // Log response
```

## CI/CD Integration

### GitHub Actions Example

```yaml
name: Integration Tests

on: [push, pull_request]

jobs:
  integration-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '23'
          distribution: 'temurin'
      
      - name: Start server
        run: |
          ./gradlew bootRun &
          sleep 30  # Wait for server to start
      
      - name: Run integration tests
        run: ./gradlew integrationTest
      
      - name: Stop server
        if: always()
        run: pkill -f "spring-boot"
```

## Testing Against Different Environments

### Local Development

```bash
./gradlew integrationTest
```

### Staging Environment

```bash
TEST_SERVER_URL=https://staging.example.com \
TEST_API_KEY=$STAGING_API_KEY \
./gradlew integrationTest
```

### Production (Read-only tests)

```bash
TEST_SERVER_URL=https://api.example.com \
TEST_API_KEY=$PROD_API_KEY \
./gradlew integrationTest --tests "*Get*"  # Only run read operations
```

## Differences from Unit Tests

| Aspect | Unit Tests | Integration Tests |
|--------|-----------|-------------------|
| Tag | None | `@Tag("integration")` |
| Server | Mocked | Real running instance |
| Database | In-memory/mocked | Real H2/PostgreSQL |
| Speed | Fast (milliseconds) | Slower (seconds) |
| Scope | Single class/method | Full request/response cycle |
| Run | `./gradlew test` | `./gradlew integrationTest` |

## Additional Resources

- [REST Assured Documentation](https://rest-assured.io/)
- [Hamcrest Matchers](http://hamcrest.org/JavaHamcrest/javadoc/2.2/)
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
