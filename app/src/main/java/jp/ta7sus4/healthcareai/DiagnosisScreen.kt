package jp.ta7sus4.healthcareai

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DiagnosisScreen(
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
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = viewModel.currentQuestion(),
            modifier = Modifier.weight(1f),
            fontSize = 24.sp,
        )
        Spacer(modifier = Modifier.weight(1f))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.weight(1f))
            OutlinedButton(
                onClick = { viewModel.onClickNo() },
            ) {
                Text(
                    text = "No",
                    color = Color.Black,
                    fontSize = 18.sp,
                )
            }
            Spacer(Modifier.weight(1f))
            OutlinedButton(
                onClick = { viewModel.onClickYes() },
            ) {
                Text(
                    text = "Yes",
                    color = Color.Black,
                    fontSize = 18.sp,
                )
            }
            Spacer(Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.weight(0.5f))
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun DiagnosisPrev() {
    DiagnosisScreen(viewModel = DiagnosisViewModel())
}