/*
 * プロジェクトルートも含めた共通設定プラグイン
 */
plugins {
    id("com.diffplug.spotless")
}

spotless {
    format("misc") {
        target(".gitattributes", ".gitignore")
        trimTrailingWhitespace()
        leadingTabsToSpaces()
        endWithNewline()
    }

    kotlinGradle {
        target("**/*.gradle.kts")
        targetExclude("**/build/**", "**/.gradle/**")
        ktlint()
        leadingTabsToSpaces()
        trimTrailingWhitespace()
        endWithNewline()
    }
}
