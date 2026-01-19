package io.xa.sigad.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
// commonMain/kotlin/com/example/bottomnav/screens/Screens.kt

import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import androidx.compose.foundation.layout.Box
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.composables.menuEarning
import com.composables.menuPixart
import com.composables.menuSetting
import com.composables.menuWallet
import io.xa.sigad.crop.ui.SimpleDemo
import io.xa.sigad.screens.ads3.AdsPageScreen
import io.xa.sigad.screens.detail.CropFileScreen
import io.xa.sigad.screens.detail.DetailTabScreen
import io.xa.sigad.screens.list.ListFileScreen
import io.xa.sigad.screens.list.ListScreen
import io.xa.sigad.screens.setting.ConfigScreen
import io.xa.sigad.screens.setting.SettingsScreen
import io.xa.sigad.wallet.WalletPage

@Composable
fun AdsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon on the screen
        Icon(
            imageVector = Icons.Default.Home,
            contentDescription = "home",
            tint = Color(0xFF0F9D58)
        )
        // Text on the screen
        Text(text = "Home", color = Color.Black)
    }
}

class WalletScreen : Screen {
    @Composable
    override fun Content() {
        WalletPage()
    }
}

@Composable
fun ProfileScreen1() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon on the screen
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "Profile",
            tint = Color(0xFF0F9D58)
        )
        // Text on the screen
        Text(text = "Profile", color = Color.Black)
    }
}


// --- Screens for our Tabs ---

object HomeScreenTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val icon = rememberVectorPainter(menuPixart)
            return TabOptions(0u, "PixArt", icon)
        }

    @Composable
    override fun Content() {
        Navigator(HomeScreen())
//        { navigator ->
//            // You can nest a Navigator here if you want separate navigation stacks per tab
//            // For simplicity, we'll just show the content directly for this example.
//            HomeScreenContent()
//        }
    }
}


object ProfileScreenTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val icon = rememberVectorPainter(Icons.Default.Person)
            return TabOptions(1u, "Profile", icon)
        }

    @Composable
    override fun Content() {
        Navigator(ProfileScreen()) { navigator ->
            ProfileScreenContent()
        }
    }
}

@Composable
fun ProfileScreenContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.Person, contentDescription = null)
            Text("Profile Screen")
        }
    }
}


object ProjectScreenTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val icon = rememberVectorPainter(Icons.Default.AddCircle)
            return TabOptions(2u, "Projecting", icon)
        }

    @Composable
    override fun Content() {
        Navigator(ProjectScreen())
    }
}

object Ads3Tab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val icon = rememberVectorPainter(menuEarning,
                ) //Icons.Default.AccountCircle)
            return TabOptions(2u, "Earning", icon)
        }

    @Composable
    override fun Content() {
        Navigator(Ads3Screen())
    }
}

object WalletTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val icon = rememberVectorPainter(menuWallet) //Icons.Default.Lock)
            return TabOptions(2u, "Wallet", icon)
        }

    @Composable
    override fun Content() {
        Navigator(WalletScreen())
    }
}


object SettingsScreenTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val icon = rememberVectorPainter(menuSetting) //Icons.Default.Settings)
            return TabOptions(2u, "Settings", icon)
        }

    @Composable
    override fun Content() {
        Navigator(SettingsScreen)
    }
}



object ListTabScreenTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val icon = rememberVectorPainter(Icons.Default.Search)
            return TabOptions(2u, "List", icon)
        }

    @Composable
    override fun Content() {
        Navigator(ListTabScreen())
    }
}


// --- Actual Screens (can be empty if you use Content directly in Tab) ---
// These are here for demonstration purposes, showing how you might structure it
// if you wanted a separate Navigator per Tab for deeper navigation.
// For this example, the actual content is in the Tab's Content() directly.

class HomeScreen : Screen {
//    @Composable
//    override fun Content() {
//        val navigator = LocalNavigator.currentOrThrow
//        println("${navigator.level} ${navigator.items}")
//
//        ListFileScreen(navigateToDetails = { objectId ->
//            //navController.navigate(DetailDestination(objectId))
//            println("click detail file....$objectId")
//            println(navigator);
//            navigator.push(DetailFileScreen(objectId))
//        })
//    }

    @Composable
    override fun Content() {

        //val navigator = LocalNavigator.current
        val navigator = LocalNavigator.currentOrThrow
        println("${navigator.level} ${navigator.items}")

//        val tabNavigator = LocalTabNavigator.current
        ListFileScreen(navigateToDetails = { objectId ->
            println("click detail file....$objectId")
            navigator.push(CropFileScreen(objectId))
            //navigator.push(DetailFileScreen(objectId))
//            tabNavigator.current = SettingsScreenTab
        })
    }
}



class Ads3Screen() : Screen {
    @Composable
    override fun Content() {
        println("class SettingsScreen")
        MaterialTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colors.background
            ) {

                //val navigator = LocalNavigator.currentOrThrow
                println("class Ads3Screen content: AdsPage before")

                //val screenModel = navigator.getNavigatorScreenModel<DetailFileScreenModel>()
                //val obj by screenModel.getObject(objectId).collectAsStateWithLifecycle(initialValue = null)

                Navigator(AdsPageScreen())
            }
        }
    }
}



class ProfileScreen : Screen {
    @Composable
    override fun Content() {
        ProfileScreenContent()
    }
}

class ProjectScreen() : Screen {
    @Composable
    override fun Content() {
        println("class ProjectScreen")
        MaterialTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colors.background
            ) {

                //val navigator = LocalNavigator.currentOrThrow
                println("class ProjectScreen content: SimpleDemo before")

                //val screenModel = navigator.getNavigatorScreenModel<DetailFileScreenModel>()
                //val obj by screenModel.getObject(objectId).collectAsStateWithLifecycle(initialValue = null)

                SimpleDemo(modifier = Modifier.fillMaxSize())

            }
        }
    }
}
/**
 *
 */
class SettingsScreen0() : Screen {
    @Composable
    override fun Content() {
        println("class SettingsScreen")
        MaterialTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colors.background
            ) {

                //val navigator = LocalNavigator.currentOrThrow
                println("class SettingsScreen content: configscreen before")

                //val screenModel = navigator.getNavigatorScreenModel<DetailFileScreenModel>()
                //val obj by screenModel.getObject(objectId).collectAsStateWithLifecycle(initialValue = null)

                ConfigScreen()
            }
        }
    }
}


/**
 * mesume screen
 */
class ListTabScreen : Screen {
    @Composable
    override fun Content() {

            val navigator = LocalNavigator.currentOrThrow
            println("${navigator.level} ${navigator.items}")

            ListScreen(navigateToDetails = { objectId ->
                //navController.navigate(DetailDestination(objectId))
                println("click detail.....$objectId")
                println(navigator);
                navigator.push(DetailTabScreen(objectId))
            })

        }
}

//
//val tabs = listOf(
//    HomeTab,
//    WalletTab,
//    SettingsTab
//)

