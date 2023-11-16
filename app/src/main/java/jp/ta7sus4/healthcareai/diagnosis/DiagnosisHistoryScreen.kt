package jp.ta7sus4.healthcareai.diagnosis

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jp.ta7sus4.healthcareai.R

@Composable
fun DiagnosisHistoryScreen(
    viewModel: DiagnosisViewModel,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize()
    ) {
        Row (
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth()
        ) {
            IconButton(
                onClick = {
                    viewModel.endButtonPressed()
                },
                modifier = Modifier
                    .size(25.dp)
            ) {
                Icon(
                    contentDescription = null,
                    painter = rememberVectorPainter(image = Icons.Filled.Close),
                    modifier = Modifier.fillMaxSize(),
                )
            }
            Spacer(modifier = Modifier.weight(0.07f))
            Text(
                text = stringResource(id = R.string.history),
                fontSize = 24.sp,
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = stringResource(id = R.string.delete_all),
                textAlign = TextAlign.Right,
                modifier = Modifier
                    .clickable(onClick = { viewModel.historyDeleteButtonPressed() })
            )
        }
        if (viewModel.historyList.size == 0 ) {
            Spacer(modifier = Modifier.weight(1.1f))
            Row (
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Outlined.Info,
                    tint = Color.Gray,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(top = 2.dp, end = 4.dp)
                        .size(22.dp)
                )
                Text(
                    text = stringResource(id = R.string.no_history),
                    fontSize = 18.sp,
                    color = Color.Gray,
                )
            }
            Spacer(modifier = Modifier.weight(1f))
        } else {
            LazyColumn {
                items(viewModel.historyList) { history ->
                    key(history.id) {
                        DiagnosisHistoryCard(history = history)
                    }
                }
            }
        }
    }
}

@Composable
fun DiagnosisHistoryCard(history: DiagnosisEntity) {
    Card(
        modifier = Modifier
            .padding(
                horizontal = 12.dp,
                vertical = 6.dp
            )
    ) {
        Column (
            modifier = Modifier
                .padding(8.dp)
        ){
            Text(
                text = history.date.toString(),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(2.dp)
            )
            Text(
                text = "スコア: ${history.score}",
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(start = 4.dp)
            )
            Text(
                text = history.comment,
                fontSize = 14.sp,
                modifier = Modifier
                    .padding(start = 4.dp)
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun DiagnosisHistoryPrev() {
    DiagnosisHistoryScreen(viewModel = DiagnosisViewModel())
}