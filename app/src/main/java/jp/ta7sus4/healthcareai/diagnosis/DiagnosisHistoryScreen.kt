package jp.ta7sus4.healthcareai.diagnosis

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jp.ta7sus4.healthcareai.R
import java.text.SimpleDateFormat
import java.util.Locale

object DateFormats {
    val historyFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.ROOT)
}

@Composable
fun LineGraph(
    data: List<Int>,
    modifier: Modifier = Modifier,
) {
    Box (
        modifier = modifier.padding(horizontal = 10.dp, vertical = 15.dp)
    ) {
        Text("${data.maxOrNull() ?: 0f}", fontSize = 10.sp, modifier = Modifier.align(Alignment.TopStart))
        Text("${data.minOrNull() ?: 0f}", fontSize = 10.sp, modifier = Modifier.align(Alignment.BottomStart))
        Canvas(modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .padding(start = 30.dp, end = 10.dp, top = 5.dp, bottom = 5.dp)
        ) {
            val maxValue = data.maxOrNull() ?: 1
            val minValue = data.minOrNull() ?: 0
            val heightScale = size.height / (maxValue - minValue)
            val widthStep = size.width / (data.size - 1)
            for (i in 0 until data.size - 1) {
                val x1 = i * widthStep
                val y1 = size.height - ((data[i] - minValue) * heightScale)
                val x2 = (i + 1) * widthStep
                val y2 = size.height - ((data[i + 1] - minValue) * heightScale)
                drawLine(
                    start = Offset(x1, y1),
                    end = Offset(x2, y2),
                    color = Color.Gray,
                    strokeWidth = 5f
                )
            }
        }
    }
}


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
                    .clickable(onClick = { viewModel.historyDeleteAllButtonPressed() })
            )
        }
        if (viewModel.historyList.size == 0 ) {
            Spacer(modifier = Modifier.weight(1.1f))
            HistoryInformation(textResource = R.string.no_history)
            Spacer(modifier = Modifier.weight(1f))
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("スコアの推移", modifier = Modifier.padding(horizontal = 10.dp))
                if (viewModel.historyList.size >= 2) {
                    Card(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        LineGraph(data = viewModel.historyList.map { it.score })
                    }
                } else {
                    HistoryInformation(textResource = R.string.history_not_enough, modifier = Modifier.padding(vertical = 50.dp))
                }
                Text("履歴", modifier = Modifier.padding(start = 10.dp, end = 10.dp, bottom = 10.dp))
            }
            LazyColumn {
                items(viewModel.historyList.reversed()) { history ->
                    key(history.id) {
                        DiagnosisHistoryCard(viewModel = viewModel, history = history)
                    }
                }
            }
        }
    }
}

@Composable
fun DiagnosisHistoryCard(
    viewModel: DiagnosisViewModel,
    history: DiagnosisEntity,
) {
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
            Row {
                Column {
                    Text(
                        text = DateFormats.historyFormat.format(history.date),
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
                            .padding(start = 4.dp, bottom = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { viewModel.historyDeleteButtonPressed(history) }) {
                    Icon(Icons.Outlined.Delete, contentDescription = null)
                }
            }
            Text(
                text = history.comment,
                fontSize = 14.sp,
                modifier = Modifier
                    .padding(start = 4.dp)
            )
        }
    }
}

@Composable
fun HistoryInformation(
    textResource: Int,
    modifier: Modifier = Modifier,
) {
    Row (
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
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
            text = stringResource(id = textResource),
            fontSize = 18.sp,
            color = Color.Gray,
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun DiagnosisHistoryPrev() {
    DiagnosisHistoryScreen(viewModel = DiagnosisViewModel())
}