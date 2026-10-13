package io.github.andraantariksa.crates.ui

import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.LocalActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import io.github.andraantariksa.crates.data.Session
import io.github.andraantariksa.crates.ui.account.AccountScreen
import io.github.andraantariksa.crates.ui.crate.CrateScreen
import io.github.andraantariksa.crates.ui.home.HomeScreen
import io.github.andraantariksa.crates.ui.mycrates.MyCratesScreen
import io.github.andraantariksa.crates.ui.search.SearchScreen
import io.github.andraantariksa.crates.ui.signin.SignInScreen
import io.github.andraantariksa.crates.ui.theme.CratesTheme
import io.github.andraantariksa.crates.ui.theme.ThemePreference
import kotlinx.serialization.Serializable
import org.koin.compose.koinInject
import kotlin.reflect.KClass

@Serializable data object HomeRoute
@Serializable data class SearchRoute(val query: String = "", val keyword: String? = null, val category: String? = null)
@Serializable data class CrateRoute(val name: String)
@Serializable data object MyCratesRoute
@Serializable data object AccountRoute
@Serializable data object SignInRoute

private enum class TopLevel(val route: Any, val routeClass: KClass<*>, val label: String, val icon: ImageVector, val selectedIcon: ImageVector) {
    Explore(HomeRoute, HomeRoute::class, "Explore", Icons.Outlined.Explore, Icons.Filled.Explore),
    MyCrates(MyCratesRoute, MyCratesRoute::class, "My crates", Icons.Outlined.Inventory2, Icons.Filled.Inventory2),
    Account(AccountRoute, AccountRoute::class, "Account", Icons.Outlined.AccountCircle, Icons.Filled.AccountCircle),
}

@Composable
fun CratesApp() {
    val themePreference = koinInject<ThemePreference>()
    val session = koinInject<Session>()
    val themeMode by themePreference.mode.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { session.refresh() }

    CratesTheme(themeMode) {
        val navController = rememberNavController()
        val backStack by navController.currentBackStackEntryAsState()
        val destination = backStack?.destination
        // Sub-screens (search, crate) keep the tab they were opened from highlighted.
        var currentTab by rememberSaveable { mutableStateOf(TopLevel.Explore) }
        LaunchedEffect(destination) {
            TopLevel.entries.firstOrNull { destination?.hasRoute(it.routeClass) == true }?.let { currentTab = it }
        }
        val inSignIn = destination?.hasRoute(SignInRoute::class) == true
        SystemBarIcons()

        NavigationSuiteScaffold(
            layoutType = if (inSignIn) NavigationSuiteType.None
            else NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(currentWindowAdaptiveInfo()),
            navigationSuiteItems = {
                TopLevel.entries.forEach { item ->
                    val selected = item == currentTab
                    item(
                        selected = selected,
                        onClick = {
                            currentTab = item // the restored destination may be a sub-screen
                            navController.navigateTopLevel(item.route)
                        },
                        icon = { Icon(if (selected) item.selectedIcon else item.icon, contentDescription = null) },
                        label = { Text(item.label) },
                    )
                }
            },
        ) {
            NavHost(navController, startDestination = HomeRoute) {
                val openCrate = { name: String -> navController.navigate(CrateRoute(name)) }
                val signIn = { navController.navigate(SignInRoute) }
                composable<HomeRoute> {
                    HomeScreen(onCrate = openCrate, onSearch = { navController.navigate(it) })
                }
                composable<SearchRoute> {
                    SearchScreen(onCrate = openCrate, onBack = { navController.popBackStack() })
                }
                composable<CrateRoute> {
                    CrateScreen(
                        onCrate = openCrate,
                        onKeyword = { navController.navigate(SearchRoute(keyword = it)) },
                        onBack = { navController.popBackStack() },
                    )
                }
                composable<MyCratesRoute> { MyCratesScreen(onCrate = openCrate, onSignIn = signIn) }
                composable<AccountRoute> { AccountScreen(onSignIn = signIn) }
                composable<SignInRoute> { SignInScreen(onDone = { navController.popBackStack() }) }
            }
        }
    }
}

private fun NavHostController.navigateTopLevel(route: Any) = navigate(route) {
    popUpTo(graph.findStartDestination().id) { saveState = true }
    launchSingleTop = true
    restoreState = true
}

/** Keeps status/navigation bar icons readable when the in-app theme differs from the system one. */
@Composable
private fun SystemBarIcons() {
    val activity = LocalActivity.current as? ComponentActivity ?: return
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    DisposableEffect(dark) {
        val transparent = Color.Transparent.toArgb()
        val style = if (dark) SystemBarStyle.dark(transparent)
        else SystemBarStyle.light(transparent, Color.Black.copy(alpha = 0.3f).toArgb())
        activity.enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
        onDispose {}
    }
}
