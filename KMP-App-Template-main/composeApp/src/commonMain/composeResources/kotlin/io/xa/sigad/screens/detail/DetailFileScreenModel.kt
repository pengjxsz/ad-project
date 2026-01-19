package io.xa.sigad.screens.detail

import cafe.adriel.voyager.core.model.StateScreenModel
import com.attafitamim.krop.core.images.ImageSrc
import io.xa.sigad.State
import io.xa.sigad.crop.picker.getImageSrcFromResource
import io.xa.sigad.data.FileItem
import io.xa.sigad.data.resourcePictures
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class DetailFileScreenModel() : StateScreenModel<State>(State.Init) {

    fun getObject(resourceId: String): Flow<ImageSrc?> =
        flow {
            val fileItem = resourcePictures.find { it.resource == resourceId }
            //scope.launch {
                // 调用 suspend 函数
            if (fileItem == null) emit(null);
            else {
                val imageSrc = getImageSrcFromResource("drawable/${fileItem.resource}")
                //}
                emit(imageSrc)
            }
        }

    fun getObject1(resourceId: String): Flow<FileItem?> =
        flow {
            val fileItem = resourcePictures.find { it.resource == resourceId }
            //scope.launch {
            // 调用 suspend 函数

                emit(fileItem)

        }


}
