# Burn for JetBrains IDEs

Language support for [Burn](https://github.com/burnlang/burn) in IntelliJ IDEA, CLion, RustRover, PyCharm and the
other JetBrains IDEs from 2024.2 on. Highlighting comes from the TextMate grammar of
[vscode-burn](https://github.com/burnlang/vscode-burn), copied to `src/main/resources/textmate`. Everything else
comes from the Burn language server through [LSP4IJ](https://github.com/redhat-developer/lsp4ij): diagnostics,
completion, hover, parameter info, inlay hints, go to declaration (also into the standard library and built-ins),
find usages, implementations, rename, formatting and quick fixes. **Tools | Burn** runs, natively runs and builds the current file and opens the
library sources. **Reload Burn Project** runs `ash sync` and restarts the language server, like a Gradle sync;
after `burn.toml` changes, its editor shows a **Load Burn Changes** banner.

```sh
gradle buildPlugin
```

This needs JDK 21 and Gradle 9 or newer. Install `build/distributions/burn-intellij-26.3.0.zip` with **Settings |
Plugins | Install Plugin from Disk...**. The plugin starts `burn lsp` from your `PATH`, `$BURN_HOME/bin` or
`~/.burn/bin`.

Every push builds the plugin in CI; download `burn-intellij-plugin` from the run's artifacts to try a change.

## Updating the grammar

The TextMate files in `src/main/resources/textmate` are copies from vscode-burn. After a grammar change there, copy
`syntaxes/burn.tmLanguage.json`, `snippets/burn.json` and `language-configuration.json` over.

## License

GPL-3.0-only
