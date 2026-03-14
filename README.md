# Upcaser

![Build](https://github.com/Melendez1209/Upcaser/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/27707-upcaser.svg)](https://plugins.jetbrains.com/plugin/27707-upcaser)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/27707-upcaser.svg)](https://plugins.jetbrains.com/plugin/27707-upcaser)


<!-- Plugin description -->
Upcaser is an IntelliJ Platform plugin that automatically capitalises the first letter of sentences in any text file.

When you type a lowercase letter after sentence-ending punctuation (period, exclamation mark, or question mark) followed
by whitespace, the plugin automatically converts it to uppercase.

Features:

- Automatic capitalisation after periods (.), exclamation marks (!), question marks (?), ellipsis (...), and Markdown headers (#)
- Works with any text file format, with special support for Markdown
- Handles multiple spaces between punctuation and the next word
- Capitalises the first letter at the beginning of the file
- Status bar widget showing current activation status
- Enhanced settings management with configurable shortcuts
- Toggle functionality with visual feedback
- Does not interfere with already capitalised letters or non-letter characters

This specific section is a source for the [plugin.xml](/src/main/resources/META-INF/plugin.xml) file which will be
extracted by the [Gradle](/build.gradle.kts) during the build process.

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
