package jp.ta7sus4.healthcareai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
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
    val viewModel = ChatViewModel()
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
            composable("chat") { ChatScreen(viewModel = viewModel) }
            composable("diagnosis") { DiagnosisScreen() }
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
            icon = { Icon(Icons.Filled.Check, contentDescription = null) },
            selected = false,
            onClick = {
                navHostController.navigate("diagnosis")
            }
        )
    }
}

@Composable
fun MainScreen(
    navHostController: NavHostController? = null,
) {
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "アプリ名",
            modifier = Modifier.weight(1f),
            fontSize = 30.sp,
        )
        Button(
            onClick = { navHostController?.navigate("chat") },
            modifier = Modifier.weight(0.2f),
        ) {
            Text(text = stringResource(R.string.start_chat))
        }
        Spacer(modifier = Modifier.weight(0.2f))
        Button(
            onClick = { navHostController?.navigate("diagnosis") },
            modifier = Modifier.weight(0.2f),
        ) {
            Text(text = stringResource(R.string.start_diagnosis))
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    HealthcareAiTheme {
        MainScreen()
    }
}