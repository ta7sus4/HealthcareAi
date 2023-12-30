package jp.ta7sus4.healthcareai.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jp.ta7sus4.healthcareai.R
import jp.ta7sus4.healthcareai.ui.theme.WhiteOpacity50

@Composable
fun Message(
    message: ChatMessage,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .padding(4.dp)
            .fillMaxWidth(),
        horizontalArrangement = if (message.isMe) Arrangement.End else Arrangement.Start,
    ) {
        if (!message.isMe) {
            Icon(
                contentDescription = null,
                painter = rememberVectorPainter(image = Icons.Default.Person),
                modifier = Modifier
                    .padding(horizontal = 1.dp, vertical = 8.dp),
            )
        }
        Row(
            modifier = Modifier
                .background(
                    color = WhiteOpacity50,
                    shape = MaterialTheme.shapes.medium,
                )
        ) {
            Text(
                text = when {
                    message.isError -> stringResource(id = R.string.error) + message.text
                    message.text.isEmpty() -> stringResource(id = R.string.thinking)
                    else -> message.text
                },
                fontSize = 18.sp,
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 2.dp),
            )
        }
    }
}

@Preview
@Composable
fun MessagePrev() {
    Message(message = ChatMessage(text = "こんにちは", isMe = false))
}