package com.rpn.salatetime.presentation.screen.components


import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.rpn.salatetime.R

/**
 * A TV-ready icon button that responds to:
 *  - Touch press
 *  - Mouse / remote hover
 *  - D-Pad / keyboard focus
 *
 * All three states animate the background, border, and scale independently.
 */
@Composable
fun TvImgButton(
    @DrawableRes icon: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 52.dp,
    iconPadding: Dp = 0.dp,
    shape: RoundedCornerShape = RoundedCornerShape(8.dp),
    defaultColor: Color = Color.White.copy(alpha = 0.08f),
    hoverColor: Color = Color.White.copy(alpha = 0.18f),
    pressColor: Color = Color.White.copy(alpha = 0.28f),
    focusColor: Color = Color.White.copy(alpha = 0.22f),
    borderColor: Color = Color.White.copy(alpha = 0.35f),
) {
    val interactionSource = remember { MutableInteractionSource() }

    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isFocused by interactionSource.collectIsFocusedAsState()

    val isHighlighted = isPressed || isHovered || isFocused

    // Background colour transitions smoothly between states
    val bgColor by animateColorAsState(
        targetValue = when {
            isPressed -> pressColor
            isHovered -> hoverColor
            isFocused -> focusColor
            else -> defaultColor
        },
        animationSpec = tween(150),
        label = "btnBg",
    )

    // Slight scale-up on hover/focus; scale-down on press (tactile feel)
    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.90f
            isHighlighted -> 1.2f
            else -> 1f
        },
        animationSpec = tween(150),
        label = "btnScale",
    )

    Surface(
        onClick = onClick,
        modifier = modifier
            .wrapContentSize()
            .scale(scale)
            .border(
                width = if (isHighlighted) 1.5.dp else 0.5.dp,
                color = if (isHighlighted) borderColor else borderColor.copy(alpha = 0.15f),
                shape = shape,
            )
            .focusable(interactionSource = interactionSource),
        shape = shape,
        color = bgColor,
        interactionSource = interactionSource,
    ) {
        Box(
            modifier = Modifier.wrapContentSize(),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(id = icon),
                contentDescription = null,
                modifier = Modifier.heightIn(max = size), // image size
                contentScale = ContentScale.FillHeight
            )
            /*Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = Color.Unspecified,   // keeps original drawable colours
                modifier = Modifier.size(size - iconPadding * 2),
            )*/
        }
    }
}

@Preview
@Composable
private fun ImageButtonPreview() {
    MaterialTheme() {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            TvImgButton(
                icon = R.drawable.qr,
                onClick = { },
            )
            Spacer(Modifier.weight(1f))
            TvImgButton(
                icon = R.drawable.logo,
                onClick = { },
            )
        }
    }
}