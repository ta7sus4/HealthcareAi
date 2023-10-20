package jp.ta7sus4.healthcareai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import jp.ta7sus4.healthcareai.ui.theme.HealthcareAiTheme


data class ChatMessage(
    val text: String,
    val isMe: Boolean,
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HealthcareAiTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainNavigator()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavigator() {
    val chatViewModel = ChatViewModel()
    val diagnosisViewModel = DiagnosisViewModel()
    val navController = rememberNavController()
    Scaffold(
        bottomBar = { BottomBar(navController) },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "main",
            modifier = Modifier.padding(padding)
        ) {
            composable("main") {
                MainScreen(
                    navHostController = navController,
                )
            }
            composable("chat") { ChatScreen(viewModel = chatViewModel) }
            composable("diagnosis") { DiagnosisStartScreen(viewModel = diagnosisViewModel) }
        }
    }
}

@Composable
fun BottomBar(
    navHostController: NavHostController
) {

    BottomNavigation(
        backgroundColor = MaterialTheme.colorScheme.surface,
    ) {
        BottomNavigationItem(
            icon = { Icon(Icons.Filled.Home, contentDescription = null) },
            selected = false,
            onClick = {
                navHostController.navigate("main")
            }
        )
        BottomNavigationItem(
            icon = { Icon(Icons.Filled.Face, contentDescription = null) },
            selected = false,
            onClick = {
                navHostController.navigate("chat")
            }
        )
        BottomNavigationItem(
            icon = { Icon(Icons.Filled.List, contentDescription = null) },
            selected = false,
            onClick = {
                navHostController.navigate("diagnosis")
            }
        )
    }
}
