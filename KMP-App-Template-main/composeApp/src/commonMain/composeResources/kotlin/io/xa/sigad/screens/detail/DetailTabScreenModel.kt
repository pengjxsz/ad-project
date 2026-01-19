package io.xa.sigad.screens.detail

import cafe.adriel.voyager.core.model.StateScreenModel
import io.xa.sigad.State
import io.xa.sigad.data.MuseumObject
import io.xa.sigad.data.MuseumRepository
import kotlinx.coroutines.flow.Flow

class DetailTabScreenModel(private val museumRepository: MuseumRepository) : StateScreenModel<State>(State.Init) {

//    private val productRepository = ProductRepository()
//
//    init {
//        screenModelScope.launch{
//            mutableState.value = State.Loading
//            productRepository.getProducts().collect { products ->
//                mutableState.value = State.Result(products)
//            }
//        }
//    }

    fun getObject(objectId: Int): Flow<MuseumObject?> =
        museumRepository.getObjectById(objectId)
}

//class DetailViewModel(private val museumRepository: MuseumRepository) : ViewModel() {
//    fun getObject(objectId: Int): Flow<MuseumObject?> =
//        museumRepository.getObjectById(objectId)
//}
