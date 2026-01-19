@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.compose.resources.ResourcesExtension.ResourceClassGeneration
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.native.tasks.PodBuildSettingsProperties.Companion.CONFIGURATION_BUILD_DIR

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.kotlinCocoapods)
    // This is the one you need for compose-resources:
}
compose {
    resources {
        generateResClass = ResourceClassGeneration.Always
    }
}
kotlin {
    applyDefaultHierarchyTemplate()

    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class) compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    cocoapods {
        summary = "ComposeApp shared module"
        homepage = "http://localhost/"
        ios.deploymentTarget = "15.3"
        version = "1.6.2"

        //podfile must be existed, else podinstall task will be skipped
        //podfile cann't be empty, some header must be there
        podfile = project.file("../iosApp/Podfile")

        // ⚠️ 请用您提供的完整路径替换这里的字符串
        //val podExecutablePath = "/Users/xaio/.rbenv/shims/pod"
        //   use local.property to set the pod executalble path

        // 方式 1: 直接设置 pod.executable
        // 这会告诉 Gradle 使用这个特定的路径来执行所有 pod 命令
        // ✅ Declare Reown AppKit dependency with version inline
//        pod("reown-swift/ReownAppKit") {
//            extraOpts += listOf("-compiler-option", "-fmodules")
//            version = "1.0.4"
//            //moduleName = "rewonAppKit"
//       }

        //APPKIT OFFICCIAL
//        pod('reown-swift/ReownAppKit') {
//            git = 'https://github.com/reown-com/reown-swift.git'
//            tag = '1.0.4'
//        }

        //20251205 use SPM INSTEAD, Podfile should be modified manually too
//        pod("reown-swift/ReownAppKit") {
//            // 🚨 终极修复：阻止 Cinterop 失败
//            // 对于 Pure Swift 库，我们不需要生成 KLIB 绑定，因此设置 headers = listOf()
//            // 告诉 KMP 跳过 Cinterop 任务，只进行链接。
//            //Because you set linkOnly = true, Gradle doesn’t generate podspec integration for transitive dependencies
//            // — you must declare them explicitly.
//            //   ===>declare pod one by one can't be accepted
//            //   should declare dependency in Podfile.
//
//            linkOnly = true
//            //source = git("git@github.com:vkormushkin/kmmpodlibrary.git"){ tag => '$version' }"
//            source = git("https://github.com/reown-com/reown-swift.git") {
//                tag = "1.0.4"
//            }
//        }


        //maunally add the dependency
        // Explicitly add transitive subspecs
        // Subspecs — no source, but linkOnly to avoid cinterop
//        pod("reown-swift/WalletConnectSign") { linkOnly = true }
//        pod("reown-swift/WalletConnectUtils") { linkOnly = true }
//        pod("reown-swift/WalletConnectRelay") { linkOnly = true }
//        pod("reown-swift/WalletConnectVerify") { linkOnly = true }
//        pod("reown-swift/WalletConnectNetworking") { linkOnly = true }
//        pod("reown-swift/WalletConnectPairing") { linkOnly = true }
//        pod("reown-swift/WalletConnectSigner") { linkOnly = true }
//        pod("reown-swift/WalletConnectPush") { linkOnly = true }
//        pod("reown-swift/WalletConnectJWT") { linkOnly = true }
//        pod("reown-swift/WalletConnectKMS") { linkOnly = true }
//        pod("reown-swift/Events") { linkOnly = true }
//        pod("reown-swift/JSONRPC") { linkOnly = true }
//        pod("reown-swift/HTTPClient") { linkOnly = true }
//        pod("reown-swift/Commons") { linkOnly = true }
//        pod("reown-swift/ReownAppKitUI") { linkOnly = true }
//        pod("reown-swift/ReownAppKitBackport") { linkOnly = true }
        // error: The Podfile does not contain any dependencies.



        // External dependencies
//        pod("YttriumWrapper", "0.9.68")
//        pod("DSF_QRCode", "~> 16.1.1")
//        pod("CoinbaseWalletSDK", "~> 1.1.0")

//        pod("YttriumWrapper") {
//            linkOnly = true
        //            source = git("https://github.com/WalletConnect/yttrium-wrapper.git") {

//            source = git("https://github.com/reown-com//yttrium-wrapper.git") {
//                tag = "0.9.68"
//            }
//        }
        //error: [!] The Podfile does not contain any dependencies.

//        pod("DSF_QRCode"){
//            linkOnly = true
//            source = git("https://github.com/WalletConnect/yttrium-wrapper.git") {
//                tag = "16.1.1"
//            }
//        }
//        pod("CoinbaseWalletSDK"){
//            linkOnly = true
//            source = git("https://github.com/WalletConnect/yttrium-wrapper.git") {
//                tag = "1.1.0"
//            }
//        }
        // Extra compiler options for all pods/frameworks
//        extraOpts += listOf("-compiler-option", "-fmodules")
//        specRepos(){
//            url("https://github.com/reown-com/reown-swift/blob/develop/reown-swift.podspec")
//        }


     //20251205
     // ❌ 删除这个：它专门为 ReownAppKit 定义了一个 KMP framework，与您的 SPM 计划冲突
//        framework {
//            baseName = "ReownAppKit"
//            isStatic = false
//        }
    }

    listOf(
        iosX64(), iosArm64(), iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
            // 🚨 修复：将 deploymentTarget 属性移到 framework 块内部。
            // 明确设置最低部署目标为 15.3，以匹配 podspec 和您的依赖要求
        }
    }
//    cocoapods {
//        summary = "ComposeApp shared module"
//        homepage = "http://localhost/"
//        ios.deploymentTarget = "15.3"
//        // ✅ 声明 Reown AppKit 依赖
//        version = "1.0.4"
//
//        pod("reown-swift/ReownAppKit") {
//            extraOpts += listOf("-compiler-option", "-fmodules")
//
//        }//        podInstallCommand = listOf("pod", "install", "--repo-update")
//    }

//    listOf(
//        iosX64(),
//        iosArm64(),
//        iosSimulatorArm64()
//    ).forEach { iosTarget ->
//        iosTarget.binaries.framework {
//            baseName = "ComposeApp"
//            isStatic = true
//        }
//    }


    sourceSets {
        val commonMain by getting {
            resources.srcDirs("/composeResources")
        }


        androidMain.dependencies {
            implementation(libs.androidx.compose.ui.tooling.preview)
            implementation(libs.androidx.activity.compose)
            //implementation(libs.ktor.client.okhttp)

            implementation(compose.preview)
            implementation(kotlin("stdlib"))
            implementation(libs.secp256k1.kmp.jni.android)
            implementation(libs.ktor.client.android)

            // ⚠️ WalletConnect AppKit Dependencies (dApp Role)
            //val BOM_VERSION = "1.3.0" // <-- 替换为您的实际 BOM 版本号
            val BOM_VERSION = "1.5.1" // <-- 替换为您的实际 BOM 版本号
            // 1. 引入 BOM 来管理所有 Reown/WalletConnect 依赖的版本
            implementation(platform("com.reown:android-bom:$BOM_VERSION"))
            // 2. 核心协议库
            implementation("com.reown:android-core")
            // 3. AppKit 客户端 (包含 dApp UI 组件)
            implementation("com.reown:appkit")
//            implementation("com.reown:sign-android")
            implementation("com.reown:sign")
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            //pod 'WalletConnectSwiftV2', '~> 1.0'
            // iOS 依赖不需要在 Gradle 中声明，
            // 它们通过 Xcode 的 Swift Package Manager/CocoaPods 添加，
            // 然后由 Kotlin/Native 自动桥接（通过 module.modulemap）。

        }

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.ui)
            implementation("org.jetbrains.compose.ui:ui-text")
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(compose.material) // For Material 1 (BottomNavigation, Scaffold, etc.)
            implementation(compose.material3) // For Material 1 (BottomNavigation, Scaffold, etc.)
            // **保留：图标数据扩展库** (用于访问 Icons.Outlined.XXX 等)

            // 其他工具和资源
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            implementation(libs.navigation.compose)
            implementation(libs.lifecycle.runtime.compose)
            implementation(libs.material.icons.core)

            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.network)
            //implementation(libs.ktor.utils)

            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)

            implementation(libs.atomicfu)


//            implementation(project.dependencies.platform(libs.koin.bom))
//            implementation(libs.koin.core)
//            implementation(libs.koin.compose)
//            implementation(libs.koin.compose.viewmodel)
            //implementation(libs.koin.compose.viewmodel.navigation)

            api(project.dependencies.platform(libs.koin.bom))
            api(libs.koin.core)
            api(libs.koin.compose)
            api(libs.koin.compose.viewmodel)

            implementation(libs.kotlinx.io.core) // Replace 0.3.6 with the latest version
            implementation(libs.kotlinx.serialization.json) // Or the latest version
            implementation(libs.kotlinx.datetime)

            implementation(kotlin("stdlib"))
            implementation(libs.secp256k1.kmp);
            implementation(kotlin("stdlib-common"))


            implementation(libs.voyager.navigator) // Check for the latest version!
            implementation(libs.voyager.tab.navigator) // For tab-like navigation
            implementation(libs.voyager.transitions) // Optional, for screen transitions
            implementation(libs.voyager.screenmodel)
            implementation(libs.voyager.koin)

            implementation(libs.ui)
            implementation(kotlincrypto.hash.sha2)
            implementation(kotlincrypto.hash.sha3)

            implementation(libs.filekit.core)
            //implementation("io.github.vinceglb:filekit-core:0.10.0-beta04")

            // QRKit 依赖，用于生成和扫描 QR 码
            //implementation("network.chaintech:qr-kit:3.1.3")
            implementation("network.chaintech:qr-kit:2.0.0")

            implementation("com.ionspin.kotlin:bignum:0.3.10")

            implementation("io.github.kashif-mehmood-km:camerak:0.0.12")
        }
    }
    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>().all {
        val mainCompilation = compilations.getByName("main")

        // point our crashlytics.def file
        mainCompilation.cinterops.create("phoneshell") {
            // Pass the header files location
            includeDirs("/Users/xaio/Documents/Projects/Products/Debug-iphoneos/PhoneShell.framework/Headers")
            compilerOpts("-DNS_FORMAT_ARGUMENT(A)=", "-D_Nullable_result=_Nullable")
        }

//        mainCompilation.cinterops.create("iosAppKit") {
//            // Point to the directory containing SigAd-Swift.h
//            includeDirs("/Users/xaio/Documents/Projects/KMP-App-Template-main/iosApp/iosApp/swift",
//                "/Users/xaio/Library/Developer/Xcode/DerivedData/iosApp-biimwjmqcqcbqxgmejveqruhzafs/Build/Intermediates.noindex/iosApp.build/Debug-iphoneos/iosApp.build/DerivedSources")
//            // Required for Swift/Clang modules interop
//            //compilerOpts("-target arm64-apple-ios15.0")
//            compilerOpts("-target", "arm64-apple-ios15.0")
//        }
    }

//
//    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>().all {
//        val mainCompilation = compilations.getByName("main")
//
//        mainCompilation.cinterops.create("walletbridge") {
//            // Point to the directory containing SigAd-Swift.h or WalletBridge.h
//            includeDirs("/Users/xaio/Documents/Projects/SigAd/iosApp/build/DerivedSources")
//
//            // Required for Swift/Clang modules interop
//            compilerOpts("-fmodules")
//        }
//    }

}
android {
    namespace = "io.xa.sigad"
    compileSdk = 35

    defaultConfig {
        applicationId = "io.xa.sigad"
        minSdk = 33
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            // **添加精确的冲突文件路径**
            excludes.add("META-INF/versions/9/OSGI-INF/MANIFEST.MF")
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
//    dependencies {
//        // Android 平台依赖 WalletConnect Kotlin SDK
//        val wc_version = "2.10.0" // 请替换为当前版本号
//        implementation("com.walletconnect:android-core: 1.35.2")
//        implementation("com.walletconnect:web3modal:1.6.6")
//
//        // https://mvnrepository.com/artifact/com.walletconnect/sign
//        //runtimeOnly("com.walletconnect:sign:2.35.2")
//    }
}

dependencies {
    debugImplementation(libs.androidx.compose.ui.tooling)
}
