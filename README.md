# Upcaser

![Build](https://github.com/Melendez1209/Upcaser/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/27707-upcaser.svg)](https://plugins.jetbrains.com/plugin/27707-upcaser)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/27707-upcaser.svg)](https://plugins.jetbrains.com/plugin/27707-upcaser)

## Template ToDo list

- [x] Create a new [IntelliJ Platform Plugin Template][template] project.
- [x] Get familiar with the [template documentation][template].
- [x] Adjust the [pluginGroup](./gradle.properties) and [pluginName](./gradle.properties), as well as
  the [id](./src/main/resources/META-INF/plugin.xml) and [sources package](./src/main/kotlin).
- [x] Adjust the plugin description in `README` (see [Tips][docs:plugin-description])
- [x] Review
  the [Legal Agreements](https://plugins.jetbrains.com/docs/marketplace/legal-agreements.html?from=IJPluginTemplate).
- [x] [Publish a plugin manually](https://plugins.jetbrains.com/docs/intellij/publishing-plugin.html?from=IJPluginTemplate)
  for the first time.
- [x] Set the `MARKETPLACE_ID` in the above README badges. You can obtain it once the plugin is published to JetBrains
  Marketplace.
- [x] Set the [Plugin Signing](https://plugins.jetbrains.com/docs/intellij/plugin-signing.html?from=IJPluginTemplate)
  related [secrets](https://github.com/JetBrains/intellij-platform-plugin-template#environment-variables).
- [ ] Set
  the [Deployment Token](https://plugins.jetbrains.com/docs/marketplace/plugin-upload.html?from=IJPluginTemplate).
- [ ] Click the <kbd>Watch</kbd> button on the top of the [IntelliJ Platform Plugin Template][template] to be notified
  about releases containing new features and fixes.

<!-- Plugin description -->
Upcaser is an IntelliJ Platform plugin that automatically capitalises the first letter of sentences in any text file.

When you type a lowercase letter after sentence-ending punctuation (period, exclamation mark, or question mark) followed
by whitespace, the plugin automatically converts it to uppercase.

Features:

- Automatic capitalisation after periods (.), exclamation marks (!), question marks (?), and ellipsis (...)
- Works with any text file format, not limited to Markdown
- Handles multiple spaces between punctuation and the next word
- Capitalises the first letter at the beginning of the file
- Status bar widget showing current activation status
- Enhanced settings management with configurable shortcuts
- Toggle functionality with visual feedback
- Does not interfere with already capitalised letters or non-letter characters

This specific section is a source for the [plugin.xml](/src/main/resources/META-INF/plugin.xml) file which will be
extracted by the [Gradle](/build.gradle.kts) during the build process.

To keep everything working, do not remove `<!-- ... -->` sections.
<!-- Plugin description end -->

## Installation

- Using the IDE built-in plugin system:

  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "Upcaser"</kbd> >
  <kbd>Install</kbd>

- Using JetBrains Marketplace:

  Go to [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID) and install it by clicking
  the <kbd>Install to ...</kbd> button in case your IDE is running.

  You can also download the [latest release](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID/versions) from
  JetBrains Marketplace and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

- Manually:

  Download the [latest release](https://github.com/Melendez1209/Upcaser/releases/latest) and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>
