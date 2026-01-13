import org.gradle.api.plugins.jvm.JvmTestSuite
import org.gradle.jvm.tasks.Jar

plugins {
    id("rdkdler-common-conventions")
}

dependencies {
    api(project(":rdkdler-common"))
    implementation(libs.jackson.databind)
    implementation(libs.jackson.dataformat.xml)
    implementation(libs.jackson.datatype.jsr310)
    implementation(libs.jsoup)
}

// ライブラリとしての属性定義
tasks.named<Jar>("jar") {
    manifest {
        // モジュール名を固定
        attributes(mapOf("Automatic-Module-Name" to "net.iwazou.rdkdler.program"))
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
            implementation(testFixtures(project(":rdkdler-common")))
        }
    }
}
