/*
 * テスト用の補助コード（src/testFixtures/java）を扱うためのプラグイン
 */
import org.gradle.api.component.AdhocComponentWithVariants

plugins {
    `java-test-fixtures`
}

// testFixturesApiElements と testFixturesRuntimeElements は Maven Publish の公開対象から外す
val javaComponent = components["java"] as AdhocComponentWithVariants
javaComponent.withVariantsFromConfiguration(configurations["testFixturesApiElements"]) { skip() }
javaComponent.withVariantsFromConfiguration(configurations["testFixturesRuntimeElements"]) { skip() }
