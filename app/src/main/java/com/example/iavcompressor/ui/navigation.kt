package com.example.iavcompressor.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.iavcompressor.helper.CompressionQuality
import com.example.iavcompressor.ui.screens.CompressionResultScreen
import com.example.iavcompressor.ui.screens.HomeScreen
import com.example.iavcompressor.viewmodels.CompressionViewModel


import kotlinx.serialization.Serializable

// Home Screen (No parameters needed)
@Serializable
object HomeRoute

// Compression Result Screen (Takes the encoded URI string as an argument)
@Serializable
data class CompressionRoute(val compressionQuality: CompressionQuality)


@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    sharedViewModel : CompressionViewModel = viewModel(),
    context: Context = LocalContext.current


) {
    NavHost(
        navController = navController,
        startDestination = HomeRoute
    ) {
        // Screen 1: Home Screen
        composable<HomeRoute> {
            HomeScreen(

                onNavigateToCompress = { chosenQuality ->
                    navController.navigate(CompressionRoute(
                        compressionQuality = chosenQuality,
                    ))
                },
                sharedViewModel = sharedViewModel
            )
        }

        // Screen 2: Compression Result Screen
        composable<CompressionRoute> { backStackEntry ->
            // Reconstruct the route arguments automatically using toRoute()
            val route: CompressionRoute = backStackEntry.toRoute()

            // Trigger batch processing when entering destination
            LaunchedEffect(route.compressionQuality) {
                sharedViewModel.startCompressionProcess(
                    context = context,
                    quality = route.compressionQuality
                )
            }
            CompressionResultScreen(
                onNavigateBack = {
                    sharedViewModel.resetState()
                    navController.popBackStack()
                },
                sharedViewModel
            )
        }
    }
}