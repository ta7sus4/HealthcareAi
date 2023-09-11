package jp.ta7sus4.healthcareai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import jp.ta7sus4.healthcareai.ui.theme.HealthcareAiTheme
import kotlinx.coroutines.launch

private const val FIRST_MESSAGE = "こんにちは！何かお困りですか？"

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
    Column (
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


@Composable
fun Message(
    message: ChatMessage,
) {
    Row(
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth(),
        horizontalArrangement = if (message.isMe) Arrangement.End else Arrangement.Start,
    ) {
        if (!message.isMe) {
            Icon(
                contentDescription = null, painter = rememberVectorPainter(image = Icons.Default.Person),
                modifier = Modifier
                    .padding(horizontal = 1.dp, vertical = 8.dp),
            )
        }
        Row(modifier = Modifier
            .background(
                color = if (message.isMe) Color(0xFFE5ECDD) else Color(0xFFC8F099),
                shape = MaterialTheme.shapes.medium,
            )
        ) {
            Text(
                text = message.text,
                fontSize = 18.sp,
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                color = if (message.isMe) MaterialTheme.colorScheme.primary else Color(0x99283C28),
            )
        }
    }
}

@Composable
fun Messages(
    viewModel: ChatViewModel,
) {
    val messages: List<ChatMessage> by viewModel.messages
    val listState = rememberLazyListState()
    LaunchedEffect(key1 = messages.size) {
        listState.animateScrollToItem(index = messages.size - 1)
    }
    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(vertical = 16.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 70.dp),
    ) {
        items(messages) { msg ->
            Message(message = msg)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
) {
    val coroutineScope = rememberCoroutineScope()

    Box {
        Messages(viewModel = viewModel)
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(4.dp)
                .fillMaxWidth(),
        ) {
            var input by rememberSaveable { mutableStateOf("") }
            IconButton(
                onClick = {
                    viewModel.messages.value = listOf(ChatMessage(text = FIRST_MESSAGE, isMe = false))
                },
                modifier = Modifier
                    .padding(12.dp)
                    .size(35.dp),
            ) {
                Icon(
                    contentDescription = null,
                    painter = rememberVectorPainter(image = Icons.Default.Refresh),
                    modifier = Modifier
                        .fillMaxSize(),
                )
            }
            OutlinedTextField(value = input, onValueChange = { newText ->
                input = newText.trimStart { it == '0' } },
                modifier = Modifier
                    .weight(1f),
            )
            IconButton(
                onClick = {
                    if (input == "") return@IconButton
                    val query = input
                    input = ""
                    coroutineScope.launch {
                        viewModel.sendMessage(ChatMessage(text = query, isMe = true))
                    }
                },
                modifier = Modifier
                    .padding(12.dp)
                    .size(35.dp),
            ) {
                Icon(
                    contentDescription = null,
                    painter = rememberVectorPainter(image = Icons.Default.Done),
                    modifier = Modifier
                        .fillMaxSize(),
                )
            }
        }

    }
}

@Composable
fun DiagnosisScreen () {
    Column (
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = stringResource(R.string.diagnosis),
            modifier = Modifier.weight(1f),
            fontSize = 30.sp,
        )
        Spacer(modifier = Modifier.weight(1f))
        val radioOptions = listOf("A", "B", "C")
        val (selectedOption, onOptionSelected) = remember { mutableStateOf(radioOptions[1] ) }
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            radioOptions.forEach { text ->
                Row(
                    Modifier
                        .selectable(
                            selected = (text == selectedOption),
                            onClick = {
                                onOptionSelected(text)
                            }
                        )
                        .padding(horizontal = 16.dp),
                ) {
                    RadioButton(
                        selected = (text == selectedOption),
                        onClick = { onOptionSelected(text) }
                    )
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
            }
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


@Preview(showBackground = true)
@Composable
fun MsgsPrev() {
    Messages(ChatViewModel())
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun ChatPrev() {
    ChatScreen(ChatViewModel())
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun DiagnosisPrev() {
    DiagnosisScreen()
}