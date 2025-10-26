package com.example.quorum

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.quorum.ui.AppDestinations
import com.example.quorum.ui.FavoritesScreen
import com.example.quorum.ui.HomeScreen
import com.example.quorum.ui.LoginScreen
import com.example.quorum.ui.PostDetailDest
import com.example.quorum.ui.PostDetailScreen
import com.example.quorum.ui.ProfileScreen
import com.example.quorum.ui.navBarDestinations
import com.example.quorum.ui.theme.QuorumTheme

class MainActivity : ComponentActivity() {

    private val authViewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QuorumTheme {
                val uiState by authViewModel.uiState.collectAsStateWithLifecycle()

                QuorumAppNavigation(
                    uiState = uiState,
                    onEmailChanged = authViewModel::onEmailChanged,
                    onPasswordChanged = authViewModel::onPasswordChanged,
                    onSignInClicked = authViewModel::onSignInClicked,
                    onSignUpClicked = authViewModel::onSignUpClicked,
                    onSignOutClicked = authViewModel::onSignOutClicked
                )
            }
        }
    }
}

@Composable
fun QuorumAppNavigation(
    uiState: LoginUiState,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onSignInClicked: () -> Unit,
    onSignUpClicked: () -> Unit,
    onSignOutClicked: () -> Unit
) {
    if (uiState.isAuthenticated) {
        QuorumApp(onSignOut = onSignOutClicked)
    } else {
        LoginScreen(
            uiState = uiState,
            onEmailChanged = onEmailChanged,
            onPasswordChanged = onPasswordChanged,
            onSignInClicked = onSignInClicked,
            onSignUpClicked = onSignUpClicked
        )
    }
}

// --- APLICACIÓN PRINCIPAL ---

@Composable
fun QuorumApp(onSignOut: () -> Unit = {}) {
    val navController: NavHostController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val homeViewModel: HomeViewModel = viewModel()

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            navBarDestinations.forEach { destination ->
                val isSelected = destination.route == currentRoute
                item(
                    icon = { Icon(destination.icon, contentDescription = destination.label) },
                    label = { Text(destination.label) },
                    selected = isSelected,
                    onClick = {
                        if (destination == AppDestinations.LOGOUT) {
                            onSignOut()
                        } else {
                            navController.navigate(destination.route) {
                                launchSingleTop = true
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                            }
                        }
                    }
                )
            }
        }
    ) { 
        NavHost(
            navController = navController,
            startDestination = AppDestinations.HOME.route
        ) {
            composable(route = AppDestinations.HOME.route) {
                HomeScreen(viewModel = homeViewModel, navController = navController)
            }

            composable(route = AppDestinations.FAVORITES.route) {
                val favoritesViewModel: FavoritesViewModel = viewModel()
                FavoritesScreen(
                    favoritesViewModel = favoritesViewModel,
                    homeViewModel = homeViewModel,
                    navController = navController
                )
            }

            composable(route = AppDestinations.PROFILE.route) {
                val profileViewModel: ProfileViewModel = viewModel()
                ProfileScreen(
                    profileViewModel = profileViewModel,
                    homeViewModel = homeViewModel,
                    navController = navController
                )
            }

            composable(
                route = PostDetailDest.routeWithArgs,
                arguments = PostDetailDest.arguments
            ) {
                val postDetailViewModel: PostDetailViewModel = viewModel()
                PostDetailScreen(viewModel = postDetailViewModel)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun QuorumAppPreview() {
    QuorumTheme {
        QuorumApp()
    }
}
