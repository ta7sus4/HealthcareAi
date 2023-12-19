package jp.ta7sus4.healthcareai.diagnosis

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import jp.ta7sus4.healthcareai.R
import jp.ta7sus4.healthcareai.common.HealthyButton

@Composable
fun DiagnosisStartScreen(
    viewModel: DiagnosisViewModel,
    modifier: Modifier = Modifier,
) {
    when (viewModel.diagnosisState) {
        DiagnosisState.RESULT -> {
            DiagnosisResultScreen(viewModel = viewModel)
        }
        DiagnosisState.STARTED -> {
            DiagnosisScreen(viewModel = viewModel)
        }
        DiagnosisState.HISTORY -> {
            DiagnosisHistoryScreen(viewModel = viewModel)
        }
        else -> {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = modifier.fillMaxSize(),
            ) {
                Spacer(Modifier.weight(0.85f))
                Text(
                    text = stringResource(id = R.string.diagnosis),
                    fontSize = 30.sp,
                    style = TextStyle(textDecoration = TextDecoration.Underline),
                    color = Color.White,
                )
                Spacer(Modifier.weight(0.2f))
                Text(text = stringResource(id = R.string.diagnosis_description), color = Color.White)
                Spacer(Modifier.weight(0.8f))
                HealthyButton(
                    text = if (viewModel.diagnosisState == DiagnosisState.LOADING) stringResource(id = R.string.thinking_question) else stringResource(id = R.string.start_diagnosis),
                    onClick = { viewModel.startButtonPressed() },
                )
                Spacer(Modifier.weight(0.2f))
                HealthyButton(
                    text = stringResource(id = R.string.show_history),
                    onClick = { viewModel.historyButtonPressed() },
                    enabled = viewModel.diagnosisState != DiagnosisState.LOADING,
                )
                Spacer(Modifier.weight(0.6f))
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun DiagnosisStartScreenPrev() {
    DiagnosisStartScreen(DiagnosisViewModel())
}