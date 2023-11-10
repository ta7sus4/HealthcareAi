package jp.ta7sus4.healthcareai.diagnosis

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import jp.ta7sus4.healthcareai.R

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
            modifier = modifier.fillMaxSize(),
        ) {
            Spacer(Modifier.weight(0.85f))
            Text(
                text = stringResource(id = R.string.diagnosis),
                fontSize = 30.sp,
            )
            Spacer(Modifier.weight(0.2f))
            Text(text = stringResource(id = R.string.diagnosis_description))
            Spacer(Modifier.weight(0.8f))
            OutlinedButton(onClick = { viewModel.startButtonPressed() }) {
                Text(
                    text = if (viewModel.isLoading) stringResource(id = R.string.thinking_question) else stringResource(id = R.string.start_diagnosis)
                )
            }
            Spacer(Modifier.weight(0.2f))
            OutlinedButton(
                onClick = { viewModel.historyButtonPressed() },
                enabled = !viewModel.isLoading
            ) {
                Text(
                    text = stringResource(id = R.string.show_history)
                )
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