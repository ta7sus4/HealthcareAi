package jp.ta7sus4.healthcareai

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import jp.ta7sus4.healthcareai.common.HealthyButton
import jp.ta7sus4.healthcareai.ui.theme.HealthcareAiTheme
import jp.ta7sus4.healthcareai.ui.theme.WhiteOpacity50


@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    navHostController: NavHostController? = null,
) {
    Column(
        modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val context = LocalContext.current
        Text(
            text = stringResource(id = R.string.privacy_policy),
            fontSize = 9.sp,
            modifier = modifier
                .align(Alignment.End)
                .padding(11.dp)
                .clickable {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://doc-hosting.flycricket.io/repair-privacy-policy/530bc40a-e492-487e-8f01-34e031ab8a1c/privacy")
                    )
                    context.startActivity(intent)
                }
        )
        Spacer(modifier = Modifier.weight(1f))
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = null,
            modifier = Modifier
                .size(180.dp)
                .background(
                    color = WhiteOpacity50,
                    shape = MaterialTheme.shapes.large,
                )
        )
        Spacer(modifier = Modifier.weight(1f))
        HealthyButton(text = stringResource(R.string.start_chat), onClick = { navHostController?.navigate("chat") })
        Spacer(modifier = Modifier.weight(0.3f))
        HealthyButton(text = stringResource(R.string.start_diagnosis), onClick = { navHostController?.navigate("diagnosis") })
        Spacer(modifier = Modifier.weight(1f))

    }
}

@Preview(showBackground = false, widthDp = 360, heightDp = 640)
@Composable
fun MainScreenPreview() {
    HealthcareAiTheme {
        MainScreen()
    }
}