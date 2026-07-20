package com.example.iavcompressor.ui

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.iavcompressor.ui.screens.CompressionResultScreen
import com.example.iavcompressor.ui.screens.HomeScreen


import kotlinx.serialization.Serializable

// Home Screen (No parameters needed)
@Serializable
object HomeRoute

// Compression Result Screen (Takes the encoded URI string as an argument)
@Serializable
data class CompressionRoute(val encodedUri: String)


@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = HomeRoute
    ) {
        // Screen 1: Home Screen
        composable<HomeRoute> {
            HomeScreen(
                onNavigateToCompress = { selectedUri ->
                    // Safely encode the content Uri string before passing in navigation
                    val encodedUri = Uri.encode(selectedUri.toString())
                    navController.navigate(CompressionRoute(encodedUri = encodedUri))
                }
            )
        }

        // Screen 2: Compression Result Screen
        composable<CompressionRoute> { backStackEntry ->
            // Reconstruct the route arguments automatically using toRoute()
            val route: CompressionRoute = backStackEntry.toRoute()
            val decodedUri = Uri.parse(Uri.decode(route.encodedUri))

            CompressionResultScreen(
                imageUri = decodedUri,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}