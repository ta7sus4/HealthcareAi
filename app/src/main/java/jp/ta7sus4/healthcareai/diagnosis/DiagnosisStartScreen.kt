package jp.ta7sus4.healthcareai.diagnosis

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    when (viewModel.diagnosisState.value) {
        is DiagnosisState.Result -> {
            DiagnosisResultScreen(viewModel = viewModel)
        }
        is DiagnosisState.Started -> {
            DiagnosisScreen(viewModel = viewModel)
        }
        is DiagnosisState.History -> {
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
                )
                Spacer(Modifier.weight(0.2f))
                Text(text = stringResource(id = R.string.diagnosis_description))
                Spacer(Modifier.weight(0.8f))
                val query = stringResource(id = R.string.query_make_question)
                HealthyButton(
                    text = if (viewModel.diagnosisState.value == DiagnosisState.Loading) stringResource(id = R.string.thinking_question) else stringResource(id = R.string.start_diagnosis),
                    onClick = {
                        viewModel.startButtonPressed(query = query)
                    },
                )
                Spacer(Modifier.weight(0.2f))
                HealthyButton(
                    text = stringResource(id = R.string.show_history),
                    onClick = { viewModel.historyButtonPressed() },
                    enabled = viewModel.diagnosisState.value !is DiagnosisState.Loading,
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