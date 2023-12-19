package jp.ta7sus4.healthcareai.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import jp.ta7sus4.healthcareai.R
import jp.ta7sus4.healthcareai.ui.theme.LightGreen
import jp.ta7sus4.healthcareai.ui.theme.WhiteOpacity50
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    Box(modifier = modifier) {
        Messages(viewModel = viewModel)
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(4.dp)
                .fillMaxWidth(),
        ) {
            var input by rememberSaveable { mutableStateOf("") }
            val firstMessage = stringResource(id = R.string.first_message)
            IconButton(
                onClick = {
                    viewModel.messages.value =
                        listOf(ChatMessage(text = firstMessage, isMe = false))
                },
                modifier = Modifier
                    .padding(12.dp)
                    .size(35.dp),
            ) {
                Icon(
                    contentDescription = null,
                    painter = rememberVectorPainter(image = Icons.Default.Refresh),
                    tint = LightGreen,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            OutlinedTextField(
                value = input,
                onValueChange = { newText ->
                    input = newText.trimStart { it == '0' }
                },
                modifier = Modifier
                    .weight(1f)
                    .background(WhiteOpacity50),
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
                    painter = rememberVectorPainter(image = Icons.Default.Send),
                    tint = LightGreen,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Preview(showBackground = false, widthDp = 360, heightDp = 640)
@Composable
fun ChatPrev() {
    ChatScreen(ChatViewModel())
}