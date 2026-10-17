package vn.ivsjsc.nocnom.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import vn.ivsjsc.nocnom.ui.components.HealthMethodologyScreen
import vn.ivsjsc.nocnom.ui.components.NocnomHeader
import vn.ivsjsc.nocnom.ui.components.RecommendationDialog
import vn.ivsjsc.nocnom.ui.screens.AuthScreen
import vn.ivsjsc.nocnom.ui.screens.HealthScreen
import vn.ivsjsc.nocnom.ui.screens.HistoryScreen
import vn.ivsjsc.nocnom.ui.screens.HomeScreen
import vn.ivsjsc.nocnom.ui.screens.LibraryScreen
import vn.ivsjsc.nocnom.ui.screens.ProfileScreen
import vn.ivsjsc.nocnom.ui.theme.NocnomTheme

private data class NavDestination(
    val route: String,
    val label: String,
    val icon: @Composable () -> Unit,
)

@Composable
fun NocnomApp(viewModel: NocnomViewModel = hiltViewModel()) {
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val systemInDark = isSystemInDarkTheme()
    var isDarkTheme by remember { mutableStateOf<Boolean?>(null) }
    val darkThemeActive = isDarkTheme ?: systemInDark

    NocnomTheme(darkTheme = darkThemeActive) {
        if (authState.user == null) {
            AuthScreen(
                state = authState,
                onSignInEmail = viewModel::signInWithEmail,
                onCreateAccount = viewModel::createAccount,
                onGoogleToken = viewModel::signInWithGoogleToken,
                onResetPassword = viewModel::sendPasswordReset,
                onClearMessage = viewModel::clearAuthMessage,
                onGuestSignIn = viewModel::signInAsGuest,
            )
        } else {
            AuthenticatedNocnomApp(
                viewModel = viewModel,
                accountEmail = authState.user?.email,
                isDarkTheme = darkThemeActive,
                onToggleTheme = {
                    isDarkTheme = !darkThemeActive
                },
            )
        }
    }
}

@Composable
private fun AuthenticatedNocnomApp(
    viewModel: NocnomViewModel,
    accountEmail: String?,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    var showRecommendationsDialog by remember { mutableStateOf(false) }

    val destinations = listOf(
        NavDestination("home", "Trang chủ", { Icon(Icons.Default.Home, null) }),
        NavDestination("health", "Sức khỏe", { Icon(Icons.Default.Favorite, null) }),
        NavDestination("library", "Kho món", { Icon(Icons.AutoMirrored.Filled.MenuBook, null) }),
    )

    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    val isTopLevelRoute = currentRoute in listOf("home", "health", "library")

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (isTopLevelRoute) {
                NocnomHeader(
                    profile = state.profile,
                    accountEmail = accountEmail,
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = onToggleTheme,
                    onRefresh = viewModel::refresh,
                    onOpenProfile = { navController.navigate("profile") },
                    onOpenRecommendations = { showRecommendationsDialog = true },
                    onOpenMethodology = { navController.navigate("methodology") },
                    onSignOut = viewModel::signOut,
                    onLogoClick = {
                        if (currentRoute != "home") {
                            navController.navigate("home") {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                )
            }
        },
        bottomBar = {
            if (isTopLevelRoute) {
                NavigationBar {
                    destinations.forEach { destination ->
                        val selected = currentRoute == destination.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = destination.icon,
                            label = { Text(destination.label) },
                        )
                    }
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
                    state = state,
                    contentPadding = padding,
                    onOpenHistory = { navController.navigate("history") },
                    onUpdateMeal = viewModel::updateMealSlot,
                    onUpdateMealForDay = viewModel::updateMealSlotForDay,
                    onMarkMealEaten = viewModel::markMealEaten,
                    onUnmarkMealEaten = viewModel::unmarkMealEaten,
                )
            }

            composable("health") {
                HealthScreen(
                    state = state,
                    contentPadding = padding,
                    onOpenProfile = { navController.navigate("profile") },
                    onUpsertMealLog = viewModel::upsertMealLog,
                    onRemoveMealLog = viewModel::removeMealLog,
                )
            }

            composable("library") {
                LibraryScreen(
                    state = state,
                    contentPadding = padding,
                    onAddDish = viewModel::addDish,
                    onUpdateDish = viewModel::updateDish,
                    onToggleFavorite = viewModel::toggleFavoriteDish,
                )
            }

            composable("history") {
                HistoryScreen(
                    state = state,
                    contentPadding = padding,
                    onBack = { navController.popBackStack() },
                )
            }

            composable("methodology") {
                HealthMethodologyScreen(
                    onBack = { navController.popBackStack() },
                )
            }

            composable("profile") {
                ProfileScreen(
                    state = state,
                    contentPadding = padding,
                    accountEmail = accountEmail,
                    onSaveProfile = viewModel::updateProfile,
                    onUpdateRecommendation = viewModel::updateRecommendationPreferences,
                    onRefresh = viewModel::refresh,
                    onSignOut = viewModel::signOut,
                    onDeleteAccount = viewModel::deleteAccount,
                    onBack = { navController.popBackStack() },
                )
            }
        }

    }

    if (showRecommendationsDialog) {
        RecommendationDialog(
            initialMode = state.profile.recommendationMode,
            initialBudget = state.profile.mealBudgetVnd,
            onDismiss = { showRecommendationsDialog = false },
            onSave = { mode, budget ->
                viewModel.updateRecommendationPreferences(mode, budget)
            },
        )
    }
}
