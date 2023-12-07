package jp.ta7sus4.healthcareai

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = null,
            modifier = Modifier.size(180.dp)
        )
        Spacer(modifier = Modifier.weight(1f))
        OutlinedButton(
            onClick = { navHostController?.navigate("chat") },
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
        ) {
            Text(text = stringResource(R.string.start_chat))
        }
        Spacer(modifier = Modifier.weight(0.3f))
        OutlinedButton(
            onClick = { navHostController?.navigate("diagnosis") },
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
        ) {
            Text(text = stringResource(R.string.start_diagnosis))
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun MainScreenPreview() {
    HealthcareAiTheme {
        MainScreen()
    }
}