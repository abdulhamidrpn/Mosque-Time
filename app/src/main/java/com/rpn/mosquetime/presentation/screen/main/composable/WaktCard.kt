package com.rpn.mosquetime.presentation.screen.main.composable


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rpn.mosquetime.domain.model.time.CompatLocalTime

@Preview(showBackground = true)
@Composable
private fun PreviewWaktCard() {
    WaktCard(
        title = "Fajr",
        time = "19:30",
        is24HourFormat = true,
        modifier = Modifier.padding(16.dp)
    )
}

@Composable
fun WaktCard(
    title: String,
    time: String,
    is24HourFormat: Boolean = false,
    modifier: Modifier = Modifier
) {
    val nowTime = CompatLocalTime(time.split(":")[0].toInt(), time.split(":")[1].toInt())
    val now = nowTime.timeFormat(is24 = is24HourFormat).toString()
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.75f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title smaller & subtle
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = dimensionResource(id = com.intuit.ssp.R.dimen._12ssp).value.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.9f)
                ),
                maxLines = 1
            )

            Spacer(Modifier.height(6.dp))
            Text(
                text = now,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = dimensionResource(id = com.intuit.ssp.R.dimen._22ssp).value.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.9f)
                ),
                maxLines = 1,
            )
        }
    }
}
