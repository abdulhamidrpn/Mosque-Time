package com.rpn.mosquetime.presentation.common

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonColors
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Icon
import com.rpn.mosquetime.R


@Composable
fun TvButton(
    text: String?=null,
    icon: ImageVector,
    isLoading: Boolean = false,
    colors: ButtonColors? = null,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.wrapContentSize(),
        contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
        colors = colors ?: ButtonDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            pressedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            focusedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            pressedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
        shape = ButtonDefaults.shape(shape = MaterialTheme.shapes.medium),

        ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            } else {

                Icon(
                    imageVector = icon,
                    contentDescription = null
                )
                if (!text.isNullOrEmpty()) {
                    Spacer(Modifier.size(8.dp))
                    Text(
                        text = text,
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }
        }
    }
}


@Preview(showBackground = true, widthDp = 300)
@Composable
private fun PreviewTvButton() {
    TvButton(
        text = "Back to Home",
        icon = Icons.Default.ArrowBackIosNew,
        onClick = {}
    )
}


@Composable
fun TvImgButton(
    @DrawableRes icon: Int,
    contentDescription: String = "",
    isLoading: Boolean = false,
    colors: ButtonColors? = null,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.wrapContentSize(),
        contentPadding = PaddingValues(0.dp),
        colors = colors ?: ButtonDefaults.colors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            pressedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            focusedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            pressedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
        shape = ButtonDefaults.shape(shape = MaterialTheme.shapes.medium),

        ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            } else {
                Image(
                    painter = painterResource(id = icon),
                    contentDescription = contentDescription,
                    modifier = Modifier.heightIn(max = 70.dp), // image size
                    contentScale = ContentScale.FillHeight
                )
            }
        }
    }
}


@Preview(showBackground = true, widthDp = 300)
@Composable
private fun PreviewTvImgButton() {
    TvImgButton(
        icon = R.drawable.logo,
        onClick = {}
    )
}
