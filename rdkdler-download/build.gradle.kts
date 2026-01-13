import org.gradle.api.plugins.jvm.JvmTestSuite
import org.gradle.jvm.tasks.Jar

plugins {
    id("rdkdler-common-conventions")
}

dependencies {
    api(project(":rdkdler-common"))
    implementation(libs.jackson.databind)
    implementation(libs.jaffree)
}

// ライブラリとしての属性定義
tasks.named<Jar>("jar") {
    manifest {
        // モジュール名を固定
        attributes(mapOf("Automatic-Module-Name" to "net.iwazou.rdkdler.download"))
    }
}

testing {
    suites.named<JvmTestSuite>("test") {
        dependencies {
            // ユニットテストでフィクスチャ（rdkdler-commonのsrc/testFixtures）を使用する
            implementation(testFixtures(project(":rdkdler-common")))
        }
    }
    suites.named<JvmTestSuite>("integrationTest") {
        dependencies {
            implementation(project(":rdkdler-common"))
            implementation(project(":rdkdler-program"))
            implementation(testFixtures(project(":rdkdler-common")))
        }
    }
}
