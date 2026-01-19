package io.xa.sigad.screens.detail

import androidx.lifecycle.ViewModel
import io.xa.sigad.data.MuseumObject
import io.xa.sigad.data.MuseumRepository
import kotlinx.coroutines.flow.Flow

class DetailViewModel(private val museumRepository: MuseumRepository) : ViewModel() {
    fun getObject(objectId: Int): Flow<MuseumObject?> =
        museumRepository.getObjectById(objectId)
}
