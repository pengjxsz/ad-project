package io.xa.sigad.screens.detail

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getNavigatorScreenModel
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.koin.koinNavigatorScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import com.attafitamim.krop.core.images.ImageSrc
import io.xa.sigad.crop.picker.getImageSrcFromResource
import io.xa.sigad.crop.ui.SimpleDemo
import io.xa.sigad.data.FileItem
import io.xa.sigad.screens.EmptyScreenContent
import sigad.composeapp.generated.resources.Res
import sigad.composeapp.generated.resources.back
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

//@Composable
//fun DetailScreen1(
//    objectId: Int,
//    navigateBack: () -> Unit,
//) {
//    val viewModel = koinViewModel<DetailViewModel>()
//    println("DetailScreen....$objectId")
//    val obj by viewModel.getObject(objectId).collectAsStateWithLifecycle(initialValue = null)
//    AnimatedContent(obj != null) { objectAvailable ->
//        if (objectAvailable) {
//            ObjectDetails(obj!!, onBackClick = navigateBack)
//        } else {
//            EmptyScreenContent(Modifier.fillMaxSize())
//        }
//    }
//}

data class CropFileScreen(val objectId: String) : Screen {
    @Composable
    override fun Content() {
        //println("CropFileScreen Enter....$objectId")

        //val screenModel = getScreenModel<DetailTabScreenModel>()

        val navigator = LocalNavigator.currentOrThrow
        val screenModel = navigator.koinNavigatorScreenModel<DetailFileScreenModel>()
        val obj by screenModel.getObject(objectId).collectAsStateWithLifecycle(initialValue = null)

        AnimatedContent(obj != null) { objectAvailable ->
            if (objectAvailable) {
                //println(obj)
                CropDetails(obj!!, onBackClick = { navigator.pop() })
            } else {
                EmptyScreenContent(Modifier.fillMaxSize())
            }
        }
    }
}

data class ImageSrcScreen(val imageSrc: ImageSrc) : Screen {
    @Composable
    override fun Content() {
        println("ImageSrcScreen Enter....")

        //val screenModel = getScreenModel<DetailTabScreenModel>()

        val navigator = LocalNavigator.currentOrThrow


        AnimatedContent(true) {
                CropDetails(imageSrc, onBackClick = { navigator.pop() })

        }
    }
}



@Composable
private fun CropDetails(
    obj: ImageSrc?,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalPlatformContext.current

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(Res.string.back))
                    }
                }
            )
        },
        modifier = modifier.windowInsetsPadding(WindowInsets.systemBars),
    ) { paddingValues ->
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
        ) {


            //var imageSrc: ImageSrc? by remember { mutableStateOf(null) }
//            val scope = rememberCoroutineScope()
//            var imageSrc : ImageSrc? = null;
//            scope.launch {
//                // 调用 suspend 函数
//                imageSrc = getImageSrcFromResource("drawable/${obj.resource}")
//            }
            SimpleDemo(obj)
            //println("....................set null")
           //imageSrc = null;
        }
    }
}


data class DetailFileScreen(val objectId: String) : Screen {
    @Composable
    override fun Content() {
        println("DetailFileScreen Enter....$objectId")

        //val screenModel = getScreenModel<DetailTabScreenModel>()

        val navigator = LocalNavigator.currentOrThrow
        println("DetailFileScreen get navigator")

        val screenModel = navigator.koinNavigatorScreenModel<DetailFileScreenModel>()
        println("DetailFileScreen get screenmodel")

        val obj by screenModel.getObject1(objectId).collectAsStateWithLifecycle(initialValue = null)
        println("DetailFileScreen get detail object")
//        val obj =   FileItem(
//            resource = "img_1.jpg",
//            thumbnailResource = "img_1_t.jpg",
//            name = "Mountain K2",
//
//            )
        AnimatedContent(obj != null) { objectAvailable ->
            if (objectAvailable) {
                //println(obj)
                ObjectDetails(obj!!, onBackClick = { navigator.pop() })
            } else {
                EmptyScreenContent(Modifier.fillMaxSize())
            }
        }
    }
}


@Composable
private fun ObjectDetails(
    obj: FileItem,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalPlatformContext.current

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(Res.string.back))
                    }
                }
            )
        },
        modifier = modifier.windowInsetsPadding(WindowInsets.systemBars),
    ) { paddingValues ->
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(Res.getUri("drawable/${obj.resource}")) // <--- Access via nested properties!
                    .build(),
                contentDescription = obj.name,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.LightGray)
            )

//            SelectionContainer {
//                Column(Modifier.padding(12.dp)) {
//                    Text(obj.title, style = MaterialTheme.typography.headlineMedium)
//                    Spacer(Modifier.height(6.dp))
//                    LabeledInfo(stringResource(Res.string.label_title), obj.title)
//                    LabeledInfo(stringResource(Res.string.label_artist), obj.artistDisplayName)
//                    LabeledInfo(stringResource(Res.string.label_date), obj.objectDate)
//                    LabeledInfo(stringResource(Res.string.label_dimensions), obj.dimensions)
//                    LabeledInfo(stringResource(Res.string.label_medium), obj.medium)
//                    LabeledInfo(stringResource(Res.string.label_department), obj.department)
//                    LabeledInfo(stringResource(Res.string.label_repository), obj.repository)
//                    LabeledInfo(stringResource(Res.string.label_credits), obj.creditLine)
//                }
//            }
        }
    }
}

@Composable
private fun LabeledInfo(
    label: String,
    data: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier.padding(vertical = 4.dp)) {
        Spacer(Modifier.height(6.dp))
        Text(
            buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("$label: ")
                }
                append(data)
            }
        )
    }
}
