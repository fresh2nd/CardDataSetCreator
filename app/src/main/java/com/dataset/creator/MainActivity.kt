package com.dataset.creator

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dataset.creator.ui.camera.CameraScreen
import com.dataset.creator.ui.cards.CardDetailsScreen
import com.dataset.creator.ui.cards.CardListScreen
import com.dataset.creator.ui.cards.FilteredCardListScreen
import com.dataset.creator.ui.cards.FullScreenImageScreen
import com.dataset.creator.ui.common.BottomNavBar
import com.dataset.creator.ui.intro.IntroScreen
import com.dataset.creator.ui.options.OptionsScreen
import com.dataset.creator.ui.theme.CardDataSetCreatorTheme
import com.dataset.creator.viewmodel.CardsViewModel
import java.net.URLDecoder
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    private val cardsViewModel: CardsViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cardsViewModel.loadCards(this)
        setContent {
            CardDataSetCreatorTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                Scaffold(
                    bottomBar = {
                        if (currentRoute == "cardList" || currentRoute == "filteredCardList" || currentRoute == "options") {
                            BottomNavBar(navController = navController)
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        CardDataSetCreatorApp(navController = navController, viewModel = cardsViewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun CardDataSetCreatorApp(navController: NavHostController, viewModel: CardsViewModel) {
    NavHost(navController = navController, startDestination = "intro") {
        composable("intro") {
            IntroScreen {
                navController.navigate("cardList") {
                    popUpTo("intro") { inclusive = true }
                }
            }
        }
        composable("cardList") {
            CardListScreen(navController = navController, viewModel = viewModel) { cardName ->
                navController.navigate("cardDetails/$cardName")
            }
        }
        composable("filteredCardList") {
            FilteredCardListScreen(navController = navController, viewModel = viewModel) { cardName ->
                navController.navigate("cardDetails/$cardName")
            }
        }
        composable("options") {
            OptionsScreen(navController = navController)
        }
        composable("cardDetails/{cardName}") { backStackEntry ->
            val cardName = backStackEntry.arguments?.getString("cardName")
            if (cardName != null) {
                CardDetailsScreen(cardName = cardName, navController = navController) { imageUri ->
                    val encodedUri = URLEncoder.encode(imageUri.toString(), "UTF-8")
                    navController.navigate("fullScreenImage/$encodedUri")
                }
            }
        }
        composable("camera/{cardName}") { backStackEntry ->
            val cardName = backStackEntry.arguments?.getString("cardName")
            if (cardName != null) {
                CameraScreen(cardName = cardName, navController = navController, viewModel = viewModel)
            }
        }
        composable(
            "fullScreenImage/{imageUri}",
            arguments = listOf(navArgument("imageUri") { type = NavType.StringType })
        ) { backStackEntry ->
            val encodedUri = backStackEntry.arguments?.getString("imageUri")
            if (encodedUri != null) {
                val decodedUri = URLDecoder.decode(encodedUri, "UTF-8")
                FullScreenImageScreen(imageUri = Uri.parse(decodedUri), navController = navController)
            }
        }
    }
}