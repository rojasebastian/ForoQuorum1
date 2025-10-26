package com.example.quorum.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavType
import androidx.navigation.navArgument

// --- Definiciones de Navegación para reutilizar ---

object PostDetailDest {
    const val route = "post_detail"
    const val postIdArg = "postId"
    val routeWithArgs = "$route/{$postIdArg}"
    val arguments = listOf(navArgument(postIdArg) { type = NavType.StringType })
}

enum class AppDestinations(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    HOME("home", "Ciencia", Icons.Default.Home),
    FAVORITES("favorites", "Favoritos", Icons.Default.Favorite),
    PROFILE("profile", "Perfil", Icons.Default.AccountBox),
    LOGOUT("logout", "Salir", Icons.Default.ExitToApp)
}

val navBarDestinations = listOf(
    AppDestinations.HOME,
    AppDestinations.FAVORITES,
    AppDestinations.PROFILE,
    AppDestinations.LOGOUT
)
