import org.jetbrains.changelog.Changelog
import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.changelog")
    id("org.jetbrains.intellij.platform")
}

// Read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html
dependencies {
    testImplementation(libs.junit)

    // IntelliJ Platform Gradle Plugin Dependencies Extension - read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-dependencies-extension.html
    intellijPlatform {
        intellijIdea("2026.1.4")
        testFramework(TestFrameworkType.Platform)
        testFramework(TestFrameworkType.Plugin.Java)

        // Java + Kotlin (UAST) power the optional cross-language reference features, wired via
        // jvm-support.xml with an optional dependency so the plugin still works without them.
        bundledPlugin("com.intellij.java")
        bundledPlugin("org.jetbrains.kotlin")
    }
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            // Support the 2026.x line (build 261 through 263).
            sinceBuild = "261"
            untilBuild = "263.*"
        }

        // "What's New" on the Marketplace listing, rendered from CHANGELOG.md by the changelog plugin.
        changeNotes = provider {
            with(changelog) {
                renderItem(
                    (getOrNull(project.version.toString()) ?: getUnreleased())
                        .withHeader(false)
                        .withEmptySections(false),
                    Changelog.OutputType.HTML,
                )
            }
        }
    }

    pluginVerification {
        ides {
            recommended()
        }
    }

    publishing {
        token = providers.environmentVariable("PUBLISH_TOKEN")
    }
}

changelog {
    groups.empty()
    repositoryUrl = "https://github.com/dsudomoin/hocon-intellij-plugin"
}

tasks.test {
    // The unified IntelliJ distribution bundles the Vue plugin, whose LSP server loader throws from its
    // static initializer in the headless test sandbox. Any test that makes the platform enumerate
    // LspServerSupportProvider then fails via TestLoggerFactory's error escalation — unrelated to this
    // plugin, and which test gets hit depends on run order. Nothing here needs Vue, so suppress it.
    systemProperty("idea.suppressed.plugins.id", "org.jetbrains.plugins.vue")
}
