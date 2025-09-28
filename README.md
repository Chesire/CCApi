# CCApi
A backend service to handle the CCBot interactions

## CI/CD

This project uses GitHub Actions for continuous integration and deployment:

### Pull Request Workflow
- **Triggers**: On pull requests to `master` branch
- **Jobs**: 
  - Cache setup for Gradle dependencies
  - Static analysis using multiple tools:
    - **Ktlint**: Kotlin code style and formatting
    - **SpotBugs**: Bug pattern detection for JVM bytecode
  - Unit tests with JUnit
  - Application compilation
  - Security scanning with OWASP Dependency Check
- **Artifacts**: Test results, static analysis reports, security reports, and build artifacts are stored for 7 days

### Main Branch Workflow  
- **Triggers**: On pushes to `master` branch
- **Jobs**: Full build, test, and cache update
- **Artifacts**: Test results and build artifacts are stored for 30 days

### Static Analysis Tools
- **Ktlint**: Enforces Kotlin coding conventions and style
- **SpotBugs**: Detects potential bugs in Java/Kotlin bytecode
- **OWASP Dependency Check**: Scans dependencies for known security vulnerabilities

All workflows use Java 21 with Temurin distribution and implement Gradle dependency caching for faster builds.
