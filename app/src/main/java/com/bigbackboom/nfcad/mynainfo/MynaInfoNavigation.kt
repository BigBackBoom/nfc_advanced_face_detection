package com.pay.nfc.ui.mynainfo

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
object MynaInfoRoute

fun NavGraphBuilder.addMynaInfoScreen(onBackClick: () -> Unit) {
    composable<MynaInfoRoute> {
        MynaInfoScreen(onBackClick)
    }
}