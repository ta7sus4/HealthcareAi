package jp.ta7sus4.healthcareai

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun Messages(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier,
) {
    val messages: List<ChatMessage> by viewModel.messages
    val listState = rememberLazyListState()
    LaunchedEffect(key1 = messages.size) {
        listState.animateScrollToItem(index = messages.size - 1)
    }
    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(vertical = 16.dp),
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 70.dp),
    ) {
        items(messages) { msg ->
            Message(message = msg)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MsgsPrev() {
    Messages(viewModel = ChatViewModel())
}