# YAKD (Yet Another Kubernetes Dashboard)  - AI Agents Instructions

Always reference these instructions first and fallback to search or bash commands only when you encounter unexpected information that does not match the info here.

This file provides guidance to AI coding agents (GitHub Copilot, Claude Code, etc.) when working with code in this repository.

## Project Overview

This repository contains YAKD (Yet Another Kubernetes Dashboard), a Kubernetes dashboard built with Quarkus (Java 17) for the backend and React for the frontend.
The project provides a web-based UI for managing Kubernetes and OpenShift clusters.

## Project Structure

- `src/main/java/com/marcnuri/yakd/` - Backend Java source code
  - `ApiResource.java` - Root JAX-RS resource delegating to sub-resources
  - `<resource>/` - Package per Kubernetes resource type (pods, deployments, services, etc.)
- `src/main/frontend/` - React frontend application
  - `src/` - Frontend source code with per-resource directories
  - `build/` - Production build output (bundled into JAR)
- `src/test/java/` - Backend tests
- `src/main/resources/` - Quarkus configuration
- `docs/` - Kubernetes manifests for deployment

## Build & Development Commands

### Building

```bash
# Build everything (frontend + backend)
mvn clean package -Pbuild-frontend

# Build backend only (requires frontend to be pre-built)
mvn clean package

# Build native image
mvn clean package -Dnative

# Deploy to Minikube
eval $(minikube docker-env)
mvn clean install -Pbuild-frontend,k8s
```

### Development

```bash
# Start Quarkus in dev mode (hot reload on port 8080)
mvn quarkus:dev

# Frontend development (in src/main/frontend/)
npm install
npm start  # Vite dev server on port 3000, proxies /api to localhost:8080
```

### Linting & Formatting

```bash
cd src/main/frontend
npm run eslint
npm run prettier
```

## Architecture

**Code is organized by feature, not by function.** Instead of grouping by type (e.g., `services/`, `resources/`), each feature has its own package/directory containing all related code. This allows splitting the codebase at any time without problems.

### Backend (Quarkus)

- **Entry point**: `KubernetesDashboardApplication.java`
- **API routing**: `ApiResource.java` - delegates to sub-resources via `@Path` annotations
- **Feature packages**: Each Kubernetes resource type has its own package (e.g., `pod/`, `deployment/`):
  - `PodResource.java` - JAX-RS endpoints
  - `PodService.java` - Business logic using Fabric8 Kubernetes client
- **WebSocket support**: `PodExecEndpoint.java` for terminal exec, `WatchResource.java` for resource watching
- **Kubernetes client**: Quarkus Kubernetes Client extension (based on Fabric8)

### Frontend (React)

- **Feature directories**: Each Kubernetes resource has its own directory (e.g., `pods/`, `deployments/`) containing:
  - `index.jsx` - Main component/exports
  - List, Detail, Edit components
  - `api.js` - API calls specific to that resource
- **State management**: Redux with slices per resource type in `src/redux/`
- **Routing**: React Router in `src/router.jsx`
- **API layer**: `src/fetch.js` for HTTP, per-resource `api.js` files for WebSocket

### Key Dependencies

- **Backend**: Quarkus 3.x, Fabric8 Kubernetes Client, RESTEasy Reactive, Jackson
- **Frontend**: React 18, Redux, Vite, TailwindCSS, xterm.js, ace-builds, DOMPurify

## Code Style

### Java

- Java 17 target
- Apache License 2.0 headers required on all source files
- Standard Quarkus/JAX-RS patterns

### JavaScript/React

- Prettier: single quotes, no trailing commas, no bracket spacing
- ESLint with React hooks plugin
- Test files in `__test__/` directories following `*.test.js` or `*.test.jsx` pattern

## Testing

**MOCKS ARE STRICTLY FORBIDDEN.** This project uses black-box testing to ensure refactorings don't break production code. If you believe mocks are required to test a functionality, notify the user first to find an alternative approach. See: https://blog.marcnuri.com/blackbox-whitebox-testing-comparison

### Key Testing Principles

1. **Avoid mocking whenever possible** - Use real implementations
2. **Use provided test infrastructure** - Don't reinvent testing utilities
3. **Test actual behavior** - Verify observable outcomes, not implementation details
4. **Keep tests simple and readable** - Tests should be easy to understand
5. **Don't tie tests to implementation** - Tests should survive refactoring

### Testing Philosophy

- **Test the public API only** - Tests should be black-box and not access internal/private functions
- **No mocks** - Use real implementations and integration testing where possible
- **Behavior over implementation** - Test what the code does, not how it does it
- Focus on observable behavior and outcomes rather than internal state

### Test Structure Guidelines

- Group scenarios or environment conditions with `beforeXxx` or setup blocks
- Use nested tests for grouping related cases
- Each test should assert only one aspect of the spec's behavior (e.g., a test for a component render should have separate tests for the classes it contains, its text content, if it has an icon, etc.)
- Group the Arrange/Given, Act/When blocks if needed, then add separate cases for the Assert/Then blocks
- Tests should have descriptive names that describe the spec behavior (e.g., "renders a slashed wifi icon")
- Add tests for edge cases

### Backend Tests (Java)

```bash
# Run all tests (unit + integration)
mvn verify

# Run only unit tests
mvn test

# Run a specific test class
mvn test -Dtest=PodTest

# Run integration tests only
mvn failsafe:integration-test
```

Tests use:
- **Quarkus Test Framework** with `@QuarkusTest` annotation
- **Kubernetes Test Server** with `@WithKubernetesTestServer` for mocking Kubernetes API
- **REST Assured** for API testing
- **AssertJ** for assertions
- **Awaitility** for async testing

### Frontend Tests (React/Vitest)

```bash
cd src/main/frontend

# Run tests once
npm test

# Run tests in watch mode
npm run test:watch
```

Frontend tests use Vitest with:
- Component tests using `renderToString` from react-dom/server
- WebSocket tests with custom `ws-test-server.js` utility

## Important Notes

- Frontend must be built before packaging the backend JAR (use `-Pbuild-frontend` profile)
- The frontend dev server proxies `/api` requests to `localhost:8080` where Quarkus runs
- License headers are checked in CI - all source files require Apache 2.0 headers
