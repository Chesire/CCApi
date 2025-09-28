# CCApi
A backend service to handle the CCBot interactions

## CI/CD

This project uses GitHub Actions for continuous integration and deployment:

### Pull Request Workflow
- **Triggers**: On pull requests to `master` or `main` branch
- **Jobs**: 
  - Cache setup for Gradle dependencies
  - Static analysis using `gradle check`
  - Unit tests with JUnit
  - Application compilation
- **Artifacts**: Test results and build artifacts are stored for 7 days

### Main Branch Workflow  
- **Triggers**: On pushes to `master` or `main` branch
- **Jobs**: Full build, test, and cache update
- **Artifacts**: Test results and build artifacts are stored for 30 days

Both workflows use Java 21 with Temurin distribution and implement Gradle dependency caching for faster builds.
