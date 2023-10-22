package jp.ta7sus4.healthcareai

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DiagnosisResultScreen(
    viewModel: DiagnosisViewModel,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize()
    ) {
        IconButton(
            onClick = {
                viewModel.endButtonPressed()
            },
            modifier = Modifier
                .padding(12.dp)
                .size(25.dp)
                .align(Alignment.Start),
        ) {
            Icon(
                contentDescription = null,
                painter = rememberVectorPainter(image = Icons.Filled.Close),
                modifier = Modifier.fillMaxSize(),
            )
        }
        Spacer(modifier = Modifier.weight(0.5f))
        Text(
            text = "結果",
            fontSize = 24.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(30.dp)
                .weight(1f),
        )
        Spacer(modifier = Modifier.weight(0.2f))
        Text(
            text = "${viewModel.resultScore()} / 100",
            fontSize = 24.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(30.dp)
                .weight(1f),
        )
        Spacer(modifier = Modifier.weight(0.2f))
        var resultSting by rememberSaveable { mutableStateOf("") }
        LaunchedEffect(Unit){
            resultSting = viewModel.resultString()
        }
        Text(
            text = if (resultSting.isEmpty()) "読み込み中..." else resultSting,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .wrapContentHeight()
                .padding(horizontal = 20.dp),
        )
        Spacer(modifier = Modifier.weight(0.3f))
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun DiagnosisResultPrev() {
    DiagnosisResultScreen(viewModel = DiagnosisViewModel())
}