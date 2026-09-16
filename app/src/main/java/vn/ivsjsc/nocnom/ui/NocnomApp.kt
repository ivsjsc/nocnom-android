package vn.ivsjsc.nocnom.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import vn.ivsjsc.nocnom.ui.screens.AuthScreen
import vn.ivsjsc.nocnom.ui.screens.HealthScreen
import vn.ivsjsc.nocnom.ui.screens.HomeScreen
import vn.ivsjsc.nocnom.ui.screens.HistoryScreen
import vn.ivsjsc.nocnom.ui.screens.LibraryScreen
import vn.ivsjsc.nocnom.ui.screens.PlanScreen
import vn.ivsjsc.nocnom.ui.screens.ProfileScreen

private data class Destination(
    val route: String,
    val label: String,
    val icon: @Composable () -> Unit,
)

@Composable
fun NocnomApp(viewModel: NocnomViewModel = hiltViewModel()) {
    val authState by viewModel.authState.collectAsStateWithLifecycle()

    if (authState.user == null) {
        AuthScreen(
            state = authState,
            onSignInEmail = viewModel::signInWithEmail,
            onCreateAccount = viewModel::createAccount,
            onGoogleToken = viewModel::signInWithGoogleToken,
            onResetPassword = viewModel::sendPasswordReset,
            onClearMessage = viewModel::clearAuthMessage,
        )
        return
    }

    AuthenticatedNocnomApp(
        viewModel = viewModel,
        accountEmail = authState.user?.email,
    )
}

@Composable
private fun AuthenticatedNocnomApp(
    viewModel: NocnomViewModel,
    accountEmail: String?,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val destinations = listOf(
        Destination("home", "Hôm nay", { Icon(Icons.Default.Home, null) }),
        Destination("plan", "Kế hoạch", { Icon(Icons.Default.Restaurant, null) }),
        Destination("library", "Kho món", { Icon(Icons.Default.MenuBook, null) }),
        Destination("health", "Sức khỏe", { Icon(Icons.Default.FavoriteBorder, null) }),
        Destination("profile", "Tài khoản", { Icon(Icons.Default.Person, null) }),
    )
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                destinations.forEach { destination ->
                    NavigationBarItem(
                        selected = currentRoute == destination.route,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = destination.icon,
                        label = { Text(destination.label) },
                    )
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.fillMaxSize(),
        ) {
            composable("home") {
                HomeScreen(
                    state,
                    padding,
                    onOpenHistory = { navController.navigate("history") },
                )
            }
            composable("plan") { PlanScreen(state, padding) }
            composable("library") { LibraryScreen(state, padding) }
            composable("health") { HealthScreen(state, padding) }
            composable("history") {
                HistoryScreen(state, padding, onBack = { navController.popBackStack() })
            }
            composable("profile") {
                ProfileScreen(
                    state = state,
                    contentPadding = padding,
                    accountEmail = accountEmail,
                    onUpdateTarget = viewModel::updateDailyTarget,
                    onRefresh = viewModel::refresh,
                    onSignOut = viewModel::signOut,
                    onDeleteAccount = viewModel::deleteAccount,
                )
            }
        }
    }
}
