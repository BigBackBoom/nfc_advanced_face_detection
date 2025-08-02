package com.bigbackboom.nfcad.home

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable


@Serializable
object HomeRoute

fun NavGraphBuilder.addHomeScreen(goToMyNumber: () -> Unit) {
    composable<HomeRoute> {
        HomeScreen(goToMyNumber)
    }
}