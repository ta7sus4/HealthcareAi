package jp.ta7sus4.healthcareai

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp

@Composable
fun DiagnosisStartScreen(
    viewModel: DiagnosisViewModel,
    modifier: Modifier = Modifier,
) {
    if (viewModel.isResult){
        DiagnosisResultScreen(viewModel = viewModel)
    } else if (viewModel.isStart) {
        DiagnosisScreen(viewModel = viewModel)
    } else {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier.fillMaxWidth(),
        ) {
            Spacer(Modifier.weight(2f))
            Text(
                text = "診断",
                fontSize = 30.sp,
            )
            Spacer(Modifier.weight(0.2f))
            Text(text = "気持ちの健康を数値化します")
            Spacer(Modifier.weight(2f))
            OutlinedButton(onClick = { viewModel.startButtonPressed() }) {
                Text(text = if (viewModel.isLoading) "質問を考えています..." else "診断をはじめる")
            }
            Spacer(Modifier.weight(1f))
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun DiagnosisStartScreenPrev() {
    DiagnosisStartScreen(DiagnosisViewModel())
}