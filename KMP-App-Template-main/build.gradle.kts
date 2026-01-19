plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinxSerialization) apply false
    alias(libs.plugins.kotlinCocoapods) apply false

}
// build.gradle
// build.gradle.kts
//plugins {
//    java // 必须应用 java 插件才能使用 toolchain
//}

//java {
//    toolchain {
//        languageVersion.set(JavaLanguageVersion.of(21))
//    }
//}

tasks.register("showJavaVersion") {
    doLast {
        // 1. Gradle 运行时 JVM
        println("🔧 Gradle Runtime JVM:")
        println("  Version: ${System.getProperty("java.version")}")
        println("  Vendor:  ${System.getProperty("java.vendor")}")
        println("  Home:    ${System.getProperty("java.home")}")

        // 2. 编译用的 JDK（来自 toolchain）
//        println("\n🛠️  Compile JDK (from toolchain):")
//        val launcher = project.pjavaToolchains.launcherFor(java.toolchain).get()
//        println("  Version: ${launcher.metadata.languageVersion}")
//        println("  Vendor:  ${launcher.metadata.vendor}")
//        println("  Home:    ${launcher.metadata.installationPath}")
    }
}

println("Gradle is using Java: ${System.getProperty("java.version")} (${System.getProperty("java.vendor")})")
println("Java Home: ${System.getProperty("java.home")}")