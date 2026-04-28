# AGENTS

## Repo Shape
- Single Gradle/Kotlin JVM application. Root project name is `hue-setup`; there are no subprojects.
- Main entrypoint is `pl.zarajczyk.huesetup.MainKt`.
- Runtime flow is `MainKt` -> `ConfigurationLoader.load(...)` -> `HueSetup.run()` -> manager list in `src/main/kotlin/pl/zarajczyk/huesetup/HueSetup.kt`.

## Commands
- Build and test: `./gradlew build`
- Run unit tests only: `./gradlew test`
- Run the app: `./gradlew run --args="--ip <bridge-ip> --token <bridge-token> --definitions <path-to-yaml>"`
- Run one test class: `./gradlew test --tests "pl.zarajczyk.huesetup.hue.httpclient.HueJsonV2Test"`

## Verified Constraints
- The app is destructive. `README.md` explicitly warns it will destroy the current Hue configuration. Do not run it against a real bridge unless the user clearly wants that.
- The CLI requires all three flags: `--ip`, `--token`, and `--definitions`.
- Definitions are loaded from YAML with Jackson. Unknown properties are ignored, but bean validation errors are printed and then fail the run.
- The detailed accepted YAML language is documented in `docs/yaml-spec.md`.
- Hue API traffic uses both bridge APIs: v1 requests go to `/api/<token>...`; v2 requests go to `/clip/v2...` with header `hue-application-key: <token>`.

## Tests
- `./gradlew test` currently exercises JSON/unit tests only.
- Integration-style tests exist under `src/test/kotlin/pl/zarajczyk/huesetup/hue/...`, but several are fully commented out right now. Do not assume they provide coverage.
- The integration test scaffold expects `HUE_IP` and `HUE_TOKEN` environment variables if those tests are ever re-enabled.

## Toolchain Notes
- Java/Kotlin toolchain target is 17. `settings.gradle.kts` uses the Foojay toolchain resolver, so Gradle may provision JDK 17 automatically.
- CI only runs `./gradlew build` on pushes to `master` and uploads `build/distributions/hue-setup-1.0-SNAPSHOT.zip`.
