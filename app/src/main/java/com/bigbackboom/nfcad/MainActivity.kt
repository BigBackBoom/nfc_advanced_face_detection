package com.bigbackboom.nfcad

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.bigbackboom.nfcad.home.HomeRoute
import com.bigbackboom.nfcad.home.addHomeScreen
import com.bigbackboom.nfcad.mynainfo.MynaInfoRoute
import com.bigbackboom.nfcad.mynainfo.addMynaInfoScreen
import com.bigbackboom.nfcad.ui.theme.NfcAdvancedFaceDetectionTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NfcAdvancedFaceDetectionTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android", modifier = Modifier.padding(innerPadding)
                    )
                }

                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = HomeRoute) {
                    addHomeScreen(
                        goToMyNumber = {
                            navController.navigate(MynaInfoRoute)
                        })
                    addMynaInfoScreen {
                        navController.popBackStack()
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!", modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    NfcAdvancedFaceDetectionTheme {
        Greeting("Android")
    }
}