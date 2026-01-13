import org.gradle.jvm.tasks.Jar

plugins {
    id("rdkdler-common-conventions")
    id("rdkdler-test-fixtures-conventions")
}

dependencies {
    testImplementation(libs.mockwebserver3.junit5)
}

// ライブラリとしての属性定義
tasks.named<Jar>("jar") {
    manifest {
        // モジュール名を固定
        attributes(mapOf("Automatic-Module-Name" to "net.iwazou.rdkdler.common"))
    }
}
