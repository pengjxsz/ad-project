package io.xa.sigad

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute

import io.xa.sigad.screens.detail.DetailScreen
import io.xa.sigad.screens.list.ListScreen
import io.xa.sigad.utils.Constants
import kotlinx.serialization.Serializable

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars

/////////////////////////////////////////////////
import androidx.compose.material3.AlertDialog
import  androidx.compose.material3.Surface
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.CircularProgressIndicator
///////////////////////////////////////////////

//import androidx.compose.material3.Icon
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.NavigationBar
//import androidx.compose.material3.NavigationBarItem
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Surface
//import androidx.compose.material3.darkColorScheme
//import androidx.compose.material3.lightColorScheme


//import androidx.compose.material.AlertDialog
//import androidx.compose.material.BottomNavigation
//import androidx.compose.material.BottomNavigationItem
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Home
//import androidx.compose.material.Button
//import androidx.compose.material.Text
//import androidx.compose.material.MaterialTheme
//import androidx.compose.material.NavigationBar
//import androidx.compose.material.NavigationBarItem
//import androidx.compose.material.Scaffold
//import androidx.compose.material.Surface
//import androidx.compose.material.darkColorScheme
//import androidx.compose.material.lightColorScheme
//import androidx.compose.material.CircularProgressIndicator
//import androidx.compose.material.lightColors

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.navigation
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import io.xa.sigad.screens.HomeScreen
import io.xa.sigad.screens.HomeScreenTab
import io.xa.sigad.screens.ProfileScreen
import io.xa.sigad.screens.setting.SettingsScreen
import io.xa.sigad.screens.SettingsScreenTab
import io.xa.sigad.screens.ProjectScreenTab
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.runtime.collectAsState

import androidx.compose.ui.Alignment
import io.xa.sigad.crop.picker.isDevicePKBound
import io.xa.sigad.crop.picker.webAccount
import io.xa.sigad.crop.ui.theme.Shapes
import io.xa.sigad.screens.Ads3Tab
import io.xa.sigad.screens.WalletTab
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

//import io.xa.sigad.wallet.AppTemp

@Serializable
object ListDestination


@Serializable
object TwoLayer


@Serializable
object profile

@Serializable
object settings

@Serializable
data class DetailDestination(val objectId: Int)

//@Composable
//fun App0() {
//    MaterialTheme(
//        colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
//    ) {
//        Surface {
//            val navController: NavHostController = rememberNavController()
//            NavHost(navController = navController, startDestination = ListDestination) {
//                composable<ListDestination> {
//                    ListScreen(navigateToDetails = { objectId ->
//                        navController.navigate(DetailDestination(objectId))
//                    })
//                }
//                composable<DetailDestination> { backStackEntry ->
//                    DetailScreen(
//                        objectId = backStackEntry.toRoute<DetailDestination>().objectId,
//                        navigateBack = {
//                            navController.popBackStack()
//                        }
//                    )
//                }
//            }
//        }
//    }
//}


//@Composable
//fun App01() {
//    MaterialTheme(
//        colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
//    ) {
//        Surface {
//            val navController = rememberNavController()
//
//            Scaffold(
//                bottomBar = {
//                    NavigationBar {
//                        Constants.BottomNavItems.forEach { tab ->
//                            NavigationBarItem(
//                                icon = {
//                                    Icon(imageVector = tab.icon, contentDescription = tab.label)
//                                },
//                                label = { Text(tab.label) },
//                                selected = navController.currentBackStackEntry?.destination?.route == tab.label,
//                                onClick = { navController.navigate(tab.label) }
//                            )
//                        }
//                    }
//                }
//            ) {
//                NavHost(navController, startDestination = TwoLayer) {
//                    //                    composable("home") { HomeScreen() }
//                    composable<profile> { ProfileScreen() }
//                    composable<settings> { SettingsScreen() }
//                    navigation<TwoLayer>(startDestination = ListDestination) {
//                        composable<ListDestination> {
//                            ListScreen(navigateToDetails = { objectId ->
//                                navController.navigate(DetailDestination(objectId))
//                            })
//                        }
//                        composable<DetailDestination> { backStackEntry ->
//                            DetailScreen(
//                                objectId = backStackEntry.toRoute<DetailDestination>().objectId,
//                                navigateBack = {
//                                    navController.popBackStack()
//                                }
//                            )
//                        }
//                    }
//
//                }
//            }
//        }
//    }
//}

@Composable
fun AppNavigation() {
    val navController: NavHostController = rememberNavController()
    NavHost(navController = navController, startDestination = ListDestination) {
        composable<ListDestination> {
            ListScreen(navigateToDetails = { objectId ->
                navController.navigate(DetailDestination(objectId))
            })
        }
        composable<DetailDestination> { backStackEntry ->
            DetailScreen(
                objectId = backStackEntry.toRoute<DetailDestination>().objectId,
                navigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

//
// In Material 3, it has been renamed to NavigationBar
//@OptIn(ExperimentalAnimationApi::class)
//@Composable
//fun BottomTabNavigation() {
//    val tabs = Constants.BottomNavItems.map { item -> item.label }
//    var selectedTab by remember { mutableStateOf(tabs[0]) }
//
//    Scaffold(
//        bottomBar = {
//            NavigationBar {
//                Constants.BottomNavItems.forEach { tab ->
//                    NavigationBarItem(
//                        icon = {
//                            Icon(imageVector = tab.icon, contentDescription = tab.label)
//                        },
//                        label = { Text(tab.label) },
//                        selected = selectedTab == tab.label,
//                        onClick = { selectedTab = tab.label }
//                    )
//                }
//            }
//        }
//    ) {
//        AnimatedContent(targetState = selectedTab, transitionSpec = {
//            fadeIn() with fadeOut()
//        }) {
//            when (selectedTab) {
//                "Home" -> HomeScreen()
//                "Profile" -> ProfileScreen()
//                //"Settings" -> SettingsScreen()
//            }
//        }
//    }
//}

//@Composable
//fun PermissionGate(
//    onPermissionsGranted: () -> Unit,
//    onPermissionsDenied: (List<String>) -> Unit
//) {
//    val context = LocalContext.current
//    val missingPermissions = remember { mutableStateListOf }
//
//    LaunchedEffect(Unit) {
//        val required = requiredPermissions()
//        missingPermissions.addAll(required.filterNot {
//            // Replace with actual checkPermission() implementation
//            checkPermission(context, it)
//        })
//        if (missingPermissions.isEmpty()) {
//            onPermissionsGranted()
//        } else {
//            onPermissionsDenied(missingPermissions)
//        }
//    }
//
//    if (missingPermissions.isNotEmpty()) {
//        AlertDialog(
//            onDismissRequest = {},
//            title = { Text("Permissions Required") },
//            text = { Text("The app requires the following permissions to start:\n${missingPermissions.joinToString()}") },
//            confirmButton = {
//                Button(onClick = { exitApp() }) {
//                    Text("Exit App")
//                }
//            }
//        )
//    }
//
//}

@Composable
fun DiagnosticBlock(unhealthy: List<SystemCheck>) {
    AlertDialog(
        onDismissRequest = {},
        confirmButton = {
            Button(onClick = { exitApp() }) {
                Text("Exit App")
            }
        },
        title = { Text("Startup Requirements Not Met") },
        text = {
            Column {
                Text("The app can't continue because the following setting not enabled:",
                    )
                Spacer(modifier = Modifier.height(8.dp))
                unhealthy.forEach {
                    Text("• ${it.readableName()}", )
                }
            }
        }
    )
}





@Composable
fun App66(
) {
    var unhealthyStates by remember { mutableStateOf<List<SystemCheck>?>(null) } // Null indicates not yet checked
    LaunchedEffect(Unit) {
        unhealthyStates = checkPermissions()
        println( "Unhealthy states: ${unhealthyStates?.map { it.readableName() }}")
        unhealthyStates
    }
    MaterialTheme {
        when {
            unhealthyStates == null -> {
                // Show a loading indicator while checks are in progress
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            unhealthyStates!!.isEmpty() -> {
                App()
            }
            else -> {
                DiagnosticBlock(unhealthyStates!!)
            }
        }
    }
}

//for initial setup
@Composable
fun InitialSetupDialog(onInitialize: () -> Unit) {
    // 使用 Dialog 或一个全屏 Composable 来实现模态效果

    // 选项 1: 使用 Compose Dialog (如果您的 KMP 支持，通常需要平台层实现)
    // 选项 2: 使用一个覆盖整个屏幕的 Composable，模拟模态对话框

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background // 覆盖整个屏幕背景
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("应用需要初始化配置", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(32.dp))

            // 模态对话框中的 "初始化" 按钮
            Button(onClick = onInitialize) {
                Text("点我初始化")
            }
        }
    }
}

// 简单的加载屏幕
@Composable
fun LoadingScreen(message: String="", step: Int=0) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (message == "") {
            Text(if (step == 0) "正在扫描手机壳并绑定....." else "正在注册......")
            CircularProgressIndicator()
        }else
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "警告或错误提示", // 必须添加描述字符串
                tint = MaterialTheme.colorScheme.error, // 可选：设置为主题的错误颜色
                modifier = Modifier.size(48.dp) // 可选：设置图标大小，使其更醒目
            )
        Spacer(Modifier.height(16.dp))
        Text(if (message=="") "正在检查配置..." else message)
    }
}


@Composable
fun App(){
    //AppTemp()
    App111()
}

// shared/src/commonMain/kotlin/App.kt

@Composable
fun App111() {
    // 观察初始化状态
    val setupState by ConfigurationManager.setupState.collectAsState()

    when (setupState) {
        AppSetupState.Loading -> {
            // 正在检查或正在初始化时显示加载屏幕
            LoadingScreen()
        }
        AppSetupState.Required -> {
            // 需要初始化时显示模态对话框/全屏设置
            InitialSetupDialog(onInitialize = {
                // 在 CoroutineScope 中运行初始化逻辑
                CoroutineScope(Dispatchers.Main).launch {
                    ConfigurationManager.runInitialization()
                    var n = 0;
                    val waitInternal = 4*30 //wait for 30 SECs, NFC timeout should be less than 30s
                    while(n < waitInternal) {
                        delay(250)
                        println("waiting for bind phoneshell......$n ${webAccount.devicePK}")
                        //if (webAccount.devicePK != "null" && webAccount.devicePK !="") {
                        if (isDevicePKBound()){
                            println(" device pk checked.....")
                            break;
                        }
                        n++;
                    }
                    //if (webAccount.devicePK.length < 5 ) { // "null", ""
                    if (n == waitInternal){
                        println(" device pk not checked......")
                        ConfigurationManager.setErrorStatus();
                    }else {
                        if (true){
                       //if (webAccount.user_id.length < 5) { //"null" or ""
                       // if (webAccount.deviceChip != ""){
                           //ConfigurationManager.setRegisterStatus()
                           ConfigurationManager.runRegister()
                       }else {
                           if (webAccount.masterKey.length < 5)
                               ConfigurationManager.setErrorStatus("邮局私钥缺失，不能继续")
                           else
                               ConfigurationManager.restoreRegister();
                           //ConfigurationManager.setCompleteStatus();
                       }
                    }
                }
            })
        }
        AppSetupState.Completed -> {
            // 初始化已完成，显示主应用界面
            //println("....enter main ")
            MainAppContent()
        }

        is AppSetupState.AppSetupError ->  {
            // 正在检查或正在初始化时显示加载屏幕
            println(" ....enter error")
            LoadingScreen((setupState as AppSetupState.AppSetupError).message)
        }

        AppSetupState.RegisterRequired -> {
            // 正在检查或正在初始化时显示加载屏幕
            println(" ....enter register")
            LoadingScreen(step=1)
        }
    }
}

// 主应用内容 Composable
@Composable
fun MainAppContent() {
    // 您原来的 Scaffold 和 TabNavigator 逻辑
    TabNavigator(HomeScreenTab) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .padding(WindowInsets.navigationBars.asPaddingValues()),
            content = { paddingValues ->
                Column(Modifier.fillMaxSize().padding(paddingValues)) {
                    CurrentTab()
                }
            },
            bottomBar = {
                NavigationBar {
                    TabNavigationItem(HomeScreenTab)
                    TabNavigationItem(WalletTab)
                    TabNavigationItem(Ads3Tab)
                    TabNavigationItem(SettingsScreenTab)
                }
            }
        )
    }
}



@Composable
fun RowScope.TabNavigationItem(tab: Tab) {
    val tabNavigator = LocalTabNavigator.current
    val defaultIcon = rememberVectorPainter(Icons.Default.Home)
    val isSelected = tabNavigator.current == tab
    NavigationBarItem(
        modifier = Modifier.background(color = Color.White),
        selected = isSelected,
        onClick = { tabNavigator.current = tab },
        icon = {
            androidx.compose.material3.Icon(
                painter = tab.options.icon ?: defaultIcon,
                contentDescription = tab.options.title,
                tint = if (isSelected) Color.Blue else Color.Gray
            )
        },
        // 核心修正点：确保 label 参数存在且使用了 title
        label = {
            // 确保文本被渲染
            Text(
                text = tab.options.title, // 🌟 使用 title 属性
                // 可以添加样式，确保可见性
                style = MaterialTheme.typography.labelSmall
            )
        },
    )
}
//@Composable
//private fun TabNavigationItem(tab: Tab) {
//    val tabNavigator = LocalTabNavigator.current //TabNavigator.current
//    NavigationBarItem(
//        selected = tabNavigator.current.options.index == tab.options.index,
//        onClick = { tabNavigator.current = tab },
//        icon = { Icon(tab.options.icon!!, contentDescription = tab.options.title) },
//        label = { Text(tab.options.title) }
//    )
//}


//
//
//class MainActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContent {
//            DemoTheme(dynamicColor = false, darkTheme = false) {
//                val navController = rememberNavController()
//                Surface(color = Color.White) {
//                    // Scaffold Component
//                    Scaffold(
//                        // Bottom navigation
//                        bottomBar = {
//                            BottomNavigationBar(navController = navController)
//                        }, content = { padding ->
//                            // Nav host: where screens are placed
//                            NavHostContainer(navController = navController, padding = padding)
//                        }
//                    )
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun NavHostContainer(
//    navController: NavHostController,
//    padding: PaddingValues
//) {
//
//    NavHost(
//        navController = navController,
//
//        // set the start destination as home
//        startDestination = "home",
//
//        // Set the padding provided by scaffold
//        modifier = Modifier.padding(paddingValues = padding),
//
//        builder = {
//
//            // route : Home
//            composable("home") {
//                HomeScreen()
//            }
//
//            // route : search
//            composable("search") {
//                SearchScreen()
//            }
//
//            // route : profile
//            composable("profile") {
//                ProfileScreen()
//            }
//        })
//}
//
//@Composable
//fun BottomNavigationBar(navController: NavHostController) {
//
//    NavigationBar(
//
//        // set background color
//        containerColor = Color(0xFF0F9D58)) {
//
//        // observe the backstack
//        val navBackStackEntry by navController.currentBackStackEntryAsState()
//
//        // observe current route to change the icon
//        // color,label color when navigated
//        val currentRoute = navBackStackEntry?.destination?.route
//
//        // Bottom nav items we declared
//        Constants.BottomNavItems.forEach { navItem ->
//
//            // Place the bottom nav items
//            NavigationBarItem(
//
//                // it currentRoute is equal then its selected route
//                selected = currentRoute == navItem.route,
//
//                // navigate on click
//                onClick = {
//                    navController.navigate(navItem.route)
//                },
//
//                // Icon of navItem
//                icon = {
//                    Icon(imageVector = navItem.icon, contentDescription = navItem.label)
//                },
//
//                // label
//                label = {
//                    Text(text = navItem.label)
//                },
//                alwaysShowLabel = false,
//
//                colors = NavigationBarItemDefaults.colors(
//                    selectedIconColor = Color.White, // Icon color when selected
//                    unselectedIconColor = Color.White, // Icon color when not selected
//                    selectedTextColor = Color.White, // Label color when selected
//                    indicatorColor = Color(0xFF195334) // Highlight color for selected item
//                )
//            )
//        }
//    }
//}

