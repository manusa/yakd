# YAKD — AI Agents Instructions

YAKD (Yet Another Kubernetes Dashboard) is a Quarkus (Java 21) + React web UI for Kubernetes and OpenShift clusters. Frontend lives under `src/main/frontend/`, gets built into the backend JAR under `frontend/` (see `pom.xml:120-123`). This file documents what an old hand would tell a new contributor on day one — read this first; fall back to grep only when something here doesn't match what you see.

## Canonical commands

Use the `Makefile`. CI runs these exact targets (`.github/workflows/build-and-test.yaml`), so what works for you matches what gates a PR.

| Intent                                | Command                  |
|---------------------------------------|--------------------------|
| Fast "still wired together" check     | `make quick-build`       |
| Full build (frontend + backend)       | `make build`             |
| Test the Quarkus app (CI's `app-tests` job) | `make test-app`    |
| Full local CI mirror — am I ready to push? | `make check`         |
| Backend dev loop (hot reload, :8080)  | `make dev-backend`       |
| Frontend dev loop (Vite, :3000)       | `make dev-frontend`      |
| Deploy to local Minikube              | `make deploy-minikube`   |

`make help` lists every target. Run `dev-backend` and `dev-frontend` side-by-side — the Vite proxy assumes the backend is up.

## Architecture

Feature-per-folder, not layer-per-folder. Each Kubernetes resource type owns:
- a backend package: `src/main/java/com/marcnuri/yakd/<resource>/` (`<Resource>Resource.java` for JAX-RS, `<Resource>Service.java` for Fabric8 calls)
- a frontend directory: `src/main/frontend/src/<resource>/` (List, Detail, Edit components + `api.js` + `selectors.js`)

`ApiResource.java` is the JAX-RS root; it delegates each `/api/v1/<resource>` to the relevant `*Resource.java`. WebSocket entry points: `PodExecEndpoint.java` (terminal exec) and `WatchResource.java` (resource watches).

Redux state lives in `src/main/frontend/src/redux/` with per-resource slices. **This project uses plain Redux** (manual `reducer.js`/`actions.js`, action types like `CRUD_ADD_OR_REPLACE`), **not Redux Toolkit** — don't reach for `createSlice`/`createAsyncThunk`.

**To add a new resource type**, touch all of: the backend package (`<Resource>Resource.java` + `<Resource>Service.java`); the `@Path` delegation in `ApiResource.java`; a frontend feature directory with `index.jsx` + `api.js` + `selectors.js` + List/Detail/Edit components; a slice in `src/main/frontend/src/redux/`; and the route in `src/main/frontend/src/router.jsx`.

### Barrel exports (enforced)

Every frontend feature directory has an `index.js`/`index.jsx` that re-exports its public surface. **Production code** MUST import through the barrel; tests (`__test__/*.test.{js,jsx}`) import sibling files directly.

```javascript
// Correct (production)
import {PodsList, api, selectors} from '../pods';
import {Card, Icon} from '../components';

// Wrong (production) — bypasses the barrel
import {PodsList} from '../pods/List';
import {Card} from '../components/Card';
```

Barrel conventions:
- `export * as api from './api'` (namespaced)
- `export * as selectors from './selectors'` (namespaced)
- `export {PodsDetailPage} from './PodsDetailPage'` (named)
- Rename generic component names semantically: `export {List as PodsList} from './List'`

## Code style

**Java.** Java 21. Apache 2.0 header required on every source file (the license-check script gates this; see Gotchas).

**JavaScript/React.**
- Prettier config (in `package.json:70-77`): `singleQuote`, `jsxSingleQuote`, `arrowParens: avoid`, `bracketSpacing: false`, `trailingComma: none`. `make fmt` runs it (mutating — see Gotchas).
- ESLint with `eslint-plugin-react-hooks`; `make lint` runs ESLint + the license-header check.
- Tests live in `__test__/` directories named `*.test.{js,jsx}`.
- **Type checking via JSDoc + `// @ts-check`** — see Gotchas for how this actually works (`checkJs: false` in jsconfig.json means the directive is the opt-in, not a no-op).

## Testing

**MOCKS ARE STRICTLY FORBIDDEN.** Black-box testing only — verifies behavior, survives refactors. If you think you need a mock, ask first. Background: <https://blog.marcnuri.com/blackbox-whitebox-testing-comparison>.

Test the public API. One assertion per test. Group related cases with nested `describe` / `beforeXxx`. Descriptive names ("renders a slashed wifi icon", not "test1").

### Preferred: Selenium IT against Fabric8 MockServer

Full-stack `*IT.java` tests in `src/test/java/` validate the rendered UI against a mocked Kubernetes API. No real cluster. Highest confidence per minute of test runtime — **this is the preferred test type for full features**.

Infrastructure in `src/test/java/com/marcnuri/yakd/selenium/`: `IntegrationTestProfile` bundles `@WithSelenium` + `@WithKubernetesTestServer`; `SeleniumTestResource` owns the ChromeDriver lifecycle and injects `WebDriver` into test fields.

Pattern: (1) seed mock resources via `kubernetes.getClient()`; (2) `kubernetes.expect().get().withPath(...).andReturn(...)` for anything outside CRUD; (3) navigate with `driver.get(url + "...")`; (4) wait on `data-testid` selectors with `FluentWait`; (5) assert with AssertJ.

```java
@QuarkusTest
@TestProfile(IntegrationTestProfile.class)
public class PodLogsIT {
  @KubernetesTestServer KubernetesServer kubernetes;
  @TestHTTPResource URL url;
  WebDriver driver;
  Wait<WebDriver> wait;

  @BeforeEach
  void setUp() {
    wait = new FluentWait<>(driver)
      .withTimeout(Duration.ofSeconds(10))
      .pollingEvery(Duration.ofMillis(100))
      .ignoring(NoSuchElementException.class);
    kubernetes.getClient().pods().inNamespace("default").resource(new PodBuilder()
      .withNewMetadata().withName("test-pod").withUid("test-uid").endMetadata()
      .withNewSpec().withContainers(new ContainerBuilder()
        .withName("container-1").withImage("busybox").build()).endSpec()
      .withNewStatus().withPhase("Running").endStatus()
      .build()).createOr(NonDeletingOperation::update);
    kubernetes.expect().get()
      .withPath("/api/v1/namespaces/default/pods/test-pod/log?...")
      .andReturn(200, "Hello from logs!").always();
    driver.navigate().to(url.toString() + "pods");
    wait.until(d -> d.findElement(By.cssSelector("[data-testid='pod-list__logs-link']")).isDisplayed());
  }

  @Test
  void displaysLogs() {
    assertThat(driver.findElement(By.cssSelector("[data-testid='pod-logs__content']")).getText())
      .contains("Hello from logs!");
  }
}
```

UI elements that tests need to find are marked with `data-testid`. Most newer IDs follow `<feature>__<element>` (e.g. `pod-list__logs-link`, `pod-logs__content`); older ones use single tokens (e.g. `container-dropdown`). Prefer `data-testid` over class names or text content when adding new selectors.

### Frontend tests (Vitest)

Component tests render via `renderToString` from `react-dom/server` and parse with `parseHtml`. Stores come from `createTestStore` (both in `src/test-utils/`). WebSocket tests use the custom `ws-test-server.js` harness. Nested `describe` groups per concern, one assertion per test:

```javascript
describe('FilterBar component tests', () => {
  describe('namespace dropdown', () => {
    test('should show selected namespace name when one is selected', () => {
      const doc = renderFilterBar({ui: {selectedNamespace: 'kube-system'}});
      const button = doc.querySelector('button');
      expect(button.textContent).toContain('kube-system');
    });
  });
});
```

## Operational gotchas

The day-one knowledge. Each tied to a file path so you can verify it didn't drift.

- **`src/main/frontend/jsconfig.json:3`** — `checkJs: false`. This is **opt-in type-checking**: only files starting with `// @ts-check` (e.g. `components/FilterBar.jsx`, `components/ResourceListV2.jsx`, `clusterrolebindings/List.jsx`) get checked by `tsc`. New files need the directive on the line after the license header to participate. `make typecheck` runs `tsc -p jsconfig.json`.
- **`src/main/frontend/package.json:51,53`** — `prestart` and `prebuild` hooks both run eslint and typecheck, but they treat prettier differently: `prestart` (used by `make dev-frontend` → `npm start`) calls `npm run prettier` which runs `prettier --write` and mutates files. `prebuild` (used by `make build` and `make verify` via `mvn -Pbuild-frontend verify` → `npm run build`) calls `npm run prettier:check` which fails on unformatted files without mutating. **The CI gate is idempotent on a clean tree; `make dev-frontend` is not.** Run `make fmt` to reformat, `make fmt-check` to verify without writing.
- **`src/main/frontend/postcss.config.cjs` and `tailwind.config.cjs`** — both MUST stay `.cjs`: `package.json:5` sets `"type": "module"`, and `vite.config.js:45` explicitly points at `postcss.config.cjs`. Renaming to `.js` silently breaks Tailwind class generation.
- **`src/main/frontend/src/test-utils/vitest.setup.js:22-24`** — stubs `HTMLCanvasElement.prototype.getContext = () => null` because jsdom has no 2D canvas. Tests touching xterm.js or anything canvas-backed can't assert on canvas output; mock at a higher layer.
- **`src/main/resources/application.properties:2-5`** — CORS is pinned to `http://localhost:3000`; that's why `dev-frontend` (Vite on :3000) can call the backend on :8080 in dev without flags.
- **`src/main/resources/application.properties:11-12`** — `%test` profile remaps `yakd.frontend.root` to `/frontend-test`. If you hardcode `/frontend` anywhere it will break only under `mvn test`.
- **`src/main/resources/application.properties:1`** — `quarkus.package.output-name=yakd` renames the legacy/fast-jar thin JAR. The runnable artifact for a JVM build lives in `target/quarkus-app/` (Quarkus 3.x fast-jar layout); the Docker image (`Dockerfile.build`) ships `target/yakd-runner` (native binary), not the JAR.
- **`src/main/frontend/vite.config.js:37-42`** — Vite dev server proxies `/api/*` to `http://localhost:8080`. `make dev-backend` must be running alongside `make dev-frontend` or API calls return ECONNREFUSED.
- **`src/main/frontend/vitest.config.js:26`** — global setup at `src/test-utils/vitest.setup.js` runs before every test. Anything jsdom-mutating (matchMedia stubs, etc.) lives there.
- **`pom.xml:62-67`** — `kubernetes-model-gatewayapi` is excluded from `quarkus-kubernetes-client` (binary size / native-image trim). Don't re-add it without justification.
- **`pom.xml:124-127`** — `src/main/frontend/build/` is mapped into the JAR at `targetPath=frontend`. A missing or stale `build/` ships zero (or stale) frontend assets silently — `make clean-frontend` before `make build` if you suspect this.
- **`pom.xml:131-149`** — inside `<pluginManagement>`, both Surefire (131-140) and Failsafe (141-149) force `java.util.logging.manager=org.jboss.logmanager.LogManager` (declarations at lines 137 and 146; inherited by the failsafe execution at 165-175). Removing the system prop breaks every test (Quarkus needs JBoss LogManager).
- **`pom.xml:27-30,135`** — custom property `<surefire.skip>false</surefire.skip>` is wired into Surefire's `<skip>` parameter (line 135). This is what makes `-Dsurefire.skip=true` actually skip Surefire **without** also killing Failsafe — Surefire's built-in `skip` is bound to `maven.test.skip`, which would skip both. Don't strip the property or the binding during a pom cleanup; `make test-it` depends on it.
- **`src/test/java/com/marcnuri/yakd/selenium/IntegrationTestProfile.java:30-34`** — sets `quarkus.kubernetes-client.devservices.enabled=false` in addition to the Selenium + Kubernetes test resources. Without it, ITs try to spin up a dev-services container and break in CI.
- **`pom.xml:252-268`** — the `native` profile auto-activates on `-Dnative` and hardcodes JDK truststore path + `changeit` password into the GraalVM args. JDK relocations can break the native build.
- **`pom.xml:7,22-23`** — version flows through `${revision}` (default `0.0.0-SNAPSHOT`) and propagates into `container.image.tag`. Release pipeline passes `-Drevision=${VERSION#v}`.
- **`src/test/java/com/marcnuri/yakd/selenium/SeleniumTestResource.java:42-55`** — uses `ChromeDriverService` directly with the `chrome` binary; **Chrome must be on PATH**. Headless mode (`--headless=new`) is on by default; flip with `@WithSelenium(headless = false)` when you need to see what's rendering.
- **`.github/scripts/check-license-headers.sh:36`** — uses `git ls-files`, so brand-new files you forgot to `git add` are **silently skipped**. Always `git add` before `make license-check`. Globs cover `.java`, `.js`, `.jsx`, `.ts`, `.css` only.
- **`.github/workflows/publish-snapshot.yml:33` and `publish-release.yml:36`** — release pipelines still call raw `mvn -Pbuild-frontend clean package`, **not** `make`. If you change the build, update the workflows too.
- **`src/main/docker/Dockerfile.build:11,21`** — multi-stage native build; output binary is hardcoded as `yakd-runner` (no version suffix). `--build-arg VERSION=...` only affects `-Drevision=` during Maven.
- **`src/main/jkube/cluster-admin-crb.yml`** — sole file in `src/main/jkube/`; **hand-written**, not generated. Uses Maven property `${k8s.namespace}` (default `default`, set in `pom.xml:26`).

## CI reproduction

`.github/workflows/build-and-test.yaml` runs three jobs in parallel (`license-check`, `app-tests`, `frontend-tests`). Reproduce locally:

```bash
make test-app        # Java unit + Selenium UI against the packaged app — CI's `app-tests` job
make test-frontend   # Vitest — CI's `frontend-tests` job
make license-check   # Apache header check — CI's `license-check` job
```

Or one-shot via `make check` — it chains the same three targets through Make composition (`check: test license-check`, `test: test-app test-frontend`).

Release/snapshot pipelines (`publish-snapshot.yml`, `publish-release.yml`) build multi-arch images (`amd64` + `arm64`) and stitch them with `docker manifest create`. Required GitHub secrets: `DOCKER_USERNAME`, `DOCKER_PASSWORD` (Docker Hub); `GITHUB_TOKEN` is provided automatically (GHCR). Release tag `v1.2.3` → image tag `1.2.3` (strip via `${VERSION#v}`).

## Troubleshooting

**`*IT.java` fails with ChromeDriver errors.** Chrome isn't on PATH (see Selenium gotcha above). Tests run headless; flip with `@WithSelenium(headless = false)` to see the actual browser when debugging locally.

**"Connection refused" on port 8080 during tests.** Another Quarkus is already on :8080. `@QuarkusTest` boots its own server. Kill the dev instance.

**Frontend looks stale after a build.** `src/main/frontend/build/` cached an older bundle (see `pom.xml:120-123` gotcha). Run `make clean-frontend && make build`.
