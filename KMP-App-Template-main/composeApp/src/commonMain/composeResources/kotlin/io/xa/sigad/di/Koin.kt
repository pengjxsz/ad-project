package io.xa.sigad.di

import io.xa.sigad.data.DataRepository
import io.xa.sigad.data.InMemoryMuseumStorage
import io.xa.sigad.data.KtorMuseumApi
import io.xa.sigad.data.MuseumApi
import io.xa.sigad.data.MuseumRepository
import io.xa.sigad.data.MuseumStorage
import io.xa.sigad.screens.detail.DetailFileScreenModel
import io.xa.sigad.screens.detail.DetailTabScreenModel
import io.xa.sigad.screens.detail.DetailViewModel
import io.xa.sigad.screens.list.ListViewModel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import org.koin.dsl.KoinAppDeclaration // ⚠️ 确保导入这个！

val dataModule = module {
    single {
        val json = Json { ignoreUnknownKeys = true }
        HttpClient {
            install(ContentNegotiation) {
                // TODO Fix API so it serves application/json
                json(json, contentType = ContentType.Any)
            }
        }
    }


    single<MuseumApi> { KtorMuseumApi(get()) }
    single<MuseumStorage> { InMemoryMuseumStorage() }
    single {
        MuseumRepository(get(), get()).apply {
            initialize()
        }
    }
    single {
        DataRepository().apply{
            initialize()
        }
    }
}

//
//get<T>()：
// 从 Koin 容器中获取类型为 T 的实例。
// 作用域：取决于你如何定义模块（single, factory, 等），get() 可能返回同一个实例或每次调用时创建的新实例。
// 灵活的依赖管理：支持多种方式来满足不同场景下的依赖需求，包括但不限于单例模式、工厂模式、命名依赖、以及带参数的构造函数等。
////factoryOf
//factory { SomeClass(get(), get(), ...) }
val viewModelModule = module {
    factoryOf(::ListViewModel)
    factoryOf(::DetailViewModel)
    factoryOf(::DetailTabScreenModel)
    factoryOf(::DetailFileScreenModel)

}
//
//fun initKoin() {
//    startKoin {
//        modules(
//            dataModule,
//            viewModelModule,
//            // 传入平台模块列表
//            platformModule
//        )
//    }
//}


fun initKoin(appDeclaration: KoinAppDeclaration = {}) { // <--- 关键修改
    startKoin {
        // 1. 在这里执行平台特定的配置（如 androidContext）
        appDeclaration()

        // 2. 注册通用模块和平台模块
        modules(
            //dataModule,
            viewModelModule,
            platformModule
        )
    }
}

