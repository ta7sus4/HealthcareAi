package jp.ta7sus4.healthcareai

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import jp.ta7sus4.healthcareai.ui.theme.HealthcareAiTheme


@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    navHostController: NavHostController? = null,
) {
    Column(
        modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "アプリ名",
            modifier = Modifier.weight(1f),
            fontSize = 30.sp,
        )
        Button(
            onClick = { navHostController?.navigate("chat") },
            modifier = Modifier.weight(0.2f),
        ) {
            Text(text = stringResource(R.string.start_chat))
        }
        Spacer(modifier = Modifier.weight(0.2f))
        Button(
            onClick = { navHostController?.navigate("diagnosis") },
            modifier = Modifier.weight(0.2f),
        ) {
            Text(text = stringResource(R.string.start_diagnosis))
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    HealthcareAiTheme {
        MainScreen()
    }
}