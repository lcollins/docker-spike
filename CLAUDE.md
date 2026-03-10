# CLAUDE.md

This file provides guidance for AI assistants working with this repository.

## Project Overview

**docker-spike** is a minimal Gradle-based Docker image build and publish pipeline. It demonstrates how to automatically version and push Docker images to GitHub Container Registry (GHCR) using git metadata.

The actual "application" is a single shell script that echoes `Hello, World from Docker!` — the real purpose of this repo is the build/publish infrastructure around it.

## Repository Structure

```
docker-spike/
├── .github/
│   ├── workflows/
│   │   └── docker-build.yml     # CI: builds and pushes on push to main
│   └── dependabot.yml           # Auto-updates Docker, Gradle, and Actions deps
├── containers/
│   └── app/
│       ├── build.gradle.kts     # Docker build/push task definitions
│       ├── Dockerfile           # Alpine-based container definition
│       └── entrypoint.sh        # The application (hello world shell script)
├── gradle/wrapper/              # Gradle wrapper files (do not modify manually)
├── build.gradle.kts             # Root build config (mavenCentral repo, no subproject plugins)
├── settings.gradle.kts          # Declares root name and includes containers:app subproject
├── gradlew / gradlew.bat        # Gradle wrapper scripts
└── README.md                    # Minimal project readme
```

## Technology Stack

| Component | Technology |
|-----------|-----------|
| Build system | Gradle 9.3.1 (Kotlin DSL) |
| Container base | Alpine Linux 3.23 |
| Docker plugin | `com.bmuschko.docker-remote-api` v10.0.0 |
| Versioning | `com.palantir.git-version` v4.3.0 |
| CI/CD | GitHub Actions |
| Registry | GitHub Container Registry (GHCR) |
| Java runtime | Java 21 (Temurin) — used only to run Gradle |

## Build System

This project uses **Gradle with Kotlin DSL** (`.gradle.kts` files). There is no Java/Kotlin application source code — Gradle is used solely to drive Docker operations.

### Key Gradle tasks

| Task | Description |
|------|-------------|
| `./gradlew dockerTagImage` | Builds the Docker image and tags it with three tags |
| `./gradlew dockerPushImage` | Pushes all tags to GHCR (depends on `dockerTagImage`) |

### Docker image tagging strategy

Each build produces three tags for `ghcr.io/lcollins/hello-world-app`:

1. `{gitHash}` — short git commit hash (immutable, commit-specific)
2. `{branchName}-latest` — mutable pointer to latest build on a branch
3. `stable` — manually promoted stable tag

Tag logic is in `containers/app/build.gradle.kts` using `com.palantir.git-version` to extract `gitHash`, `branchName`, etc.

## Development Workflows

### Building and pushing locally

```bash
# Ensure Docker daemon is running and you're logged into GHCR
echo $GITHUB_TOKEN | docker login ghcr.io -u USERNAME --password-stdin

# Build and push
./gradlew dockerPushImage
```

### Modifying the container application

Edit `containers/app/entrypoint.sh`. The script is copied into the Alpine image at `/app/entrypoint.sh` and set as the `ENTRYPOINT`.

### Modifying the Dockerfile

Edit `containers/app/Dockerfile`. The base image is `alpine:3.23` — keep it minimal.

### Adding or changing build logic

Edit `containers/app/build.gradle.kts`. The two custom tasks (`dockerTagImage`, `dockerPushImage`) define all Docker operations.

### Updating Gradle wrapper

```bash
./gradlew wrapper --gradle-version=<new-version>
```

Do **not** manually edit `gradle/wrapper/gradle-wrapper.properties` for version bumps; use Dependabot or the command above.

## CI/CD Pipeline

**Trigger**: Push to `main` branch

**Workflow** (`.github/workflows/docker-build.yml`):
1. Checkout repository
2. Set up Java 21 (Temurin) — needed to run Gradle
3. Log in to GHCR using `${{ secrets.GITHUB_TOKEN }}`
4. Run `./gradlew dockerPushImage`

**Required permissions**: `contents: read`, `packages: write`

## Dependency Management

Dependabot is configured to check **daily** for updates to:
- Docker base images (`containers/app/Dockerfile`)
- Gradle plugins and wrapper (`/`)
- GitHub Actions versions (`/`)

Dependabot PRs follow the pattern `dependabot/{ecosystem}/{package}-{version}`.

## Conventions

- **Gradle task names**: camelCase (`dockerTagImage`, `dockerPushImage`)
- **File names**: kebab-case (`docker-build.yml`, `docker-spike`)
- **Image names**: follow GHCR convention — `ghcr.io/{owner}/{repo-name}`
- **Commit messages**: plain English, imperative mood; Dependabot bumps use its default format
- **Branch names**: `main` is the default branch for CI triggers; feature branches use the standard GitHub flow

## No Tests

This project has no automated tests. There are no test directories, frameworks, or CI test steps. The only validation is that `dockerPushImage` succeeds (i.e., the image builds and pushes without error).

## Common Pitfalls

- **Docker daemon must be running** for any `docker*` Gradle task to work locally.
- **GHCR authentication required** before pushing — the CI workflow handles this automatically via `GITHUB_TOKEN`.
- **Gradle requires Java** — even though there's no Java application, you need a JDK installed to run `./gradlew`.
- **`noCache` is `false`** in `dockerTagImage` — Docker layer caching is enabled; set to `true` if you need a clean build.
- The `stable` tag is always pushed on every build — it is not gated on any promotion logic. If you want true environment promotion, this tag strategy needs updating.
