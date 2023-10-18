package jp.ta7sus4.healthcareai

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
                contentDescription = null,
                painter = rememberVectorPainter(image = Icons.Default.Person),
                modifier = Modifier
                    .padding(horizontal = 1.dp, vertical = 8.dp),
            )
        }
        Row(
            modifier = Modifier
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

@Preview(showBackground = true)
@Composable
fun MessagePrev() {
    Message(message = ChatMessage(text = "こんにちは", isMe = false))
}