/*
 * サブプロジェクト用共通設定プラグイン
 */
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.testing.jacoco.tasks.JacocoReport

plugins {
    `java-library`
    id("io.freefair.lombok")
    `jacoco`
    `maven-publish`
    id("rdkdler-root-conventions")
}

val libs = the<VersionCatalogsExtension>().named("libs")

// Mockito Agent設定
val mockitoAgent by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
    isTransitive = false
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    withSourcesJar()
    withJavadocJar()
}

// エンコーディングの一括設定
tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

dependencies {
    // 全プロジェクト共通のライブラリ
    implementation(libs.findLibrary("slf4j").get())
    testImplementation(libs.findLibrary("assertj-core").get())
    testImplementation(libs.findLibrary("mockito-junit-jupiter").get())
    testRuntimeOnly(libs.findLibrary("logback-classic").get())

    val mockitoVersion = libs.findVersion("mockito").get().requiredVersion
    add(mockitoAgent.name, "org.mockito:mockito-core:$mockitoVersion")
}

val junitVersion = libs.findVersion("junit").get().requiredVersion

testing {
    suites {
        // 標準のユニットテスト
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter(junitVersion)
            targets.all {
                testTask.configure {
                    // test タスクの後に jacocoTestReport を実行する
                    finalizedBy(tasks.named("jacocoTestReport"))
                }
            }
        }

        // 統合テストスイート
        val integrationTest by registering(JvmTestSuite::class) {
            useJUnitJupiter(junitVersion)
            dependencies {
                implementation(project())
                implementation(libs.findLibrary("assertj-core").get())
                implementation(libs.findLibrary("mockito-junit-jupiter").get())
                runtimeOnly(libs.findLibrary("logback-classic").get())
            }
            targets.all {
                testTask.configure {
                    description = "Runs integration and system tests."
                    group = "verification"
                    shouldRunAfter(tasks.named("test"))
                    // integrationTest タスクの後に jacocoItReport を実行する
                    finalizedBy(tasks.named("jacocoItReport"))
                }
            }
        }
    }
}

// ライフサイクル管理
tasks.named("check") {
    dependsOn(tasks.named("integrationTest"))
}

// JaCoCo共通設定
tasks.withType<JacocoReport>().configureEach {
    reports {
        html.required.set(true)
    }
}

val integrationTestExec = layout.buildDirectory.file("jacoco/integrationTest.exec")

// 統合テスト用JaCoCoタスクの改善
tasks.register<JacocoReport>("jacocoItReport") {
    val integrationTestTask = tasks.named<Test>("integrationTest")
    dependsOn(integrationTestTask)

    // Taskを渡さず、execファイルを直接指定（CC安全）
    executionData.from(integrationTestExec)

    classDirectories.setFrom(
        sourceSets.main
            .get()
            .output.classesDirs,
    )
    sourceDirectories.setFrom(
        sourceSets.main
            .get()
            .allSource.srcDirs,
    )
}

// テスト共通実行引数（Agentの遅延解決）
tasks.withType<Test>().configureEach {
    // 定義したクラスに「mockitoAgent」を渡す
    jvmArgumentProviders.add(MockitoAgentArgumentProvider(mockitoAgent))

    testLogging {
        events("skipped", "failed")
        showExceptions = true
        showCauses = true
        showStackTraces = true
        exceptionFormat = TestExceptionFormat.FULL
        // デバッグ時以外は標準出力を抑制する
        showStandardStreams = false
    }
}

spotless {
    java {
        target("src/*/java/**/*.java")
        targetExclude("**/build/**", "**/.gradle/**")
        toggleOffOn() // spotless:off と spotless:on のコメントで括ることでフォーマットを抑止
        googleJavaFormat()
            .aosp() // 4スペース
            .reflowLongStrings()
            .skipJavadocFormatting()
        importOrder()
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}

/**
 * Configuration Cacheに対応したMockito Agentプロバイダー。
 * クラスとして独立させることで、スクリプトオブジェクトのキャプチャを防ぎます。
 */
class MockitoAgentArgumentProvider(
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val agentFiles: FileCollection,
) : CommandLineArgumentProvider {
    override fun asArguments(): Iterable<String> = listOf("-javaagent:${agentFiles.singleFile.absolutePath}")
}
