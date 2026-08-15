# AGENTS.md

Communicate with the user in Simplified Chinese, but, as a matter of open-source etiquette, write comments, commit
messages, Markdown files and similar content in British English.

## Git operations (mandatory rule)

- **Every Git operation that changes state - staging, commit, push, pull, merge, rebase, branch, stash, reset, tag,
  cherry-pick, etc. - requires explicit user confirmation first.** Always ask before running any mutating git command;
  never commit or push on your own initiative.
- Reviewing past records is always allowed without confirmation: read-only commands such as `git log`, `git status`,
  `git diff`, `git show`, `git blame`.
- Active development branch for Android work is `dev_android`, not `main` (check `git status` - it may be ahead of
  `origin/dev_android`).
- Pushes to `main` containing changes under `Web/` trigger the GitHub Pages deploy workflow
  (`.github/workflows/static.yml`). Treat pushes to `main` with extra care.

## Project Overview

- IntelliJ Platform plugin (Kotlin + Gradle Kotlin DSL, IntelliJ Platform Gradle Plugin 2.6.0, Kotlin 2.2.21, JVM 21),
  Windows / PowerShell 5.1.
- Manifest and registration: `src/main/resources/META-INF/plugin.xml`. Core code:
    - `AutoCapitalizeHandler.kt` — typedHandler, core automatic capitalisation logic
    - `UpcaserSettings.kt` — application-level `PersistentStateComponent` (stored in upcaser.xml), not project-level
    - `UpcaserConfigurable.kt` — settings page (`applicationConfigurable`, id=`upcaser.settings`)
    - `UpcaserStatusBarWidget.kt` / `UpcaserStatusBarWidgetFactory.kt` — status bar widget (ID=`UpcaserStatus`, must
      match the factory id in plugin.xml)
    - `ToggleUpcastAction.kt`, `ShortcutManager.kt`, `ShortcutTextField.kt`

## Build and Test

- Build (fast path): `.\gradlew.bat build -x verifyPlugin` (the full `build` also runs verifyPlugin which downloads an
  IDE for validation and takes a long time)
- Unit tests: `.\gradlew.bat test --tests "com.github.melendez1209.upcaser.AutoCapitalizeHandlerTest"`
- **Proxy pitfall**: `~/.gradle/gradle.properties` is configured with a localhost:7890 proxy; when the proxy is not
  running, dependency resolution fails with `Connection refused: localhost:7890`. Workaround (do not modify that file):
  `.\gradlew.bat build "-Dhttp.proxyHost=" "-Dhttps.proxyHost="` — the quotes must be kept; PowerShell splits an
  unquoted `-Dxxx=` into a task name and fails with `Task '.xxx=' not found`.
- **Known failing tests (unrelated to this repository's changes, they also fail on a clean checkout; do not treat as
  regressions)**:
    - `testCapitalizeAtBeginningOfFile` — NPE, the test itself is written incorrectly (calls `myFixture.type` directly
      without first `configureByText`)
    - `testEllipsisCapitalizationAndSpacing` — ComparisonFailure

## Version Release

- The version number lives in 3 places; a version change must keep them in sync: `pluginVersion` in `gradle.properties`,
  `<version>` in `plugin.xml`, and `CHANGELOG.md` (entry + bottom link).
- `patchPluginXml` generates the marketplace description from the `<!-- Plugin description -->` section of `README.md`;
  keep that marker when editing the README.
- Releases go through `publishPlugin` (depends on patchChangelog).

## Localisation

- Base bundle `MyBundle.properties` (US English). Variants `MyBundle_en` / `MyBundle_en_GB` / `MyBundle_zh_CN` /
  `MyBundle_zh_TW` must have exactly the same key set as the base bundle (currently 42 keys); a missing key silently
  falls back to the base bundle and is easy to miss.
- Non-ASCII characters must be escaped as `\uXXXX` (uppercase hexadecimal), e.g. `\u5927`.
- Key names are mixed-case and case-sensitive (e.g. `Settings.Title` vs `action.enable`); code references them via
  `MyBundle.message("Key")`.
