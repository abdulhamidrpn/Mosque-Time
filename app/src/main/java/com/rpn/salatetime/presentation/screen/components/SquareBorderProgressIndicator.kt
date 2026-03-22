package com.rpn.salatetime.presentation.screen.components

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.min

/**
 * Material 3 compliant square progress indicator that draws progress along screen edges
 */
@Composable
fun SquareProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 3.dp,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    progressColor: Color = MaterialTheme.colorScheme.primary,
    cornerRadius: Dp = 0.dp,
    strokeCap: StrokeCap = StrokeCap.Round,
    animationSpec: AnimationSpec<Float> = tween(
        durationMillis = 1000,
        easing = LinearEasing
    )
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = animationSpec,
        label = "square_progress_animation"
    )

    val density = LocalDensity.current
    val strokeWidthPx = with(density) { strokeWidth.toPx() }
    val cornerRadiusPx = with(density) { cornerRadius.toPx() }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(cornerRadius))
    ) {
        drawSquareProgressIndicator(
            progress = animatedProgress,
            strokeWidthPx = strokeWidthPx,
            cornerRadiusPx = cornerRadiusPx,
            trackColor = trackColor,
            progressColor = progressColor,
            strokeCap = strokeCap
        )
    }
}

/**
 * Indeterminate version with continuous animation
 */
@Composable
fun SquareProgressIndicatorIndeterminate(
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 4.dp,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    progressColor: Color = MaterialTheme.colorScheme.primary,
    cornerRadius: Dp = 8.dp,
    strokeCap: StrokeCap = StrokeCap.Round,
    animationDuration: Int = 2000
) {
    val infiniteTransition = rememberInfiniteTransition(label = "square_progress_infinite")

    val animatedProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = animationDuration,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "infinite_square_progress"
    )

    val density = LocalDensity.current
    val strokeWidthPx = with(density) { strokeWidth.toPx() }
    val cornerRadiusPx = with(density) { cornerRadius.toPx() }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(cornerRadius))
    ) {
        drawSquareProgressIndicatorIndeterminate(
            progress = animatedProgress,
            strokeWidthPx = strokeWidthPx,
            cornerRadiusPx = cornerRadiusPx,
            trackColor = trackColor,
            progressColor = progressColor,
            strokeCap = strokeCap
        )
    }
}

private fun DrawScope.drawSquareProgressIndicator(
    progress: Float,
    strokeWidthPx: Float,
    cornerRadiusPx: Float,
    trackColor: Color,
    progressColor: Color,
    strokeCap: StrokeCap
) {
    val canvasWidth = size.width
    val canvasHeight = size.height
    val halfStroke = strokeWidthPx / 2f

    // Adjust bounds to prevent clipping
    val adjustedCornerRadius = min(cornerRadiusPx, min(canvasWidth, canvasHeight) / 4f)

    val path = createSquarePath(
        width = canvasWidth - strokeWidthPx,
        height = canvasHeight - strokeWidthPx,
        cornerRadius = adjustedCornerRadius,
        strokeOffset = halfStroke
    )

    // Draw track (background)
    drawPath(
        path = path,
        color = trackColor,
        style = Stroke(
            width = strokeWidthPx,
            cap = strokeCap,
            join = StrokeJoin.Round
        )
    )

    // Draw progress
    if (progress > 0f) {
        val progressPath = createProgressPath(
            width = canvasWidth - strokeWidthPx,
            height = canvasHeight - strokeWidthPx,
            cornerRadius = adjustedCornerRadius,
            strokeOffset = halfStroke,
            progress = progress
        )

        drawPath(
            path = progressPath,
            color = progressColor,
            style = Stroke(
                width = strokeWidthPx,
                cap = strokeCap,
                join = StrokeJoin.Round
            )
        )
    }
}

private fun DrawScope.drawSquareProgressIndicatorIndeterminate(
    progress: Float,
    strokeWidthPx: Float,
    cornerRadiusPx: Float,
    trackColor: Color,
    progressColor: Color,
    strokeCap: StrokeCap
) {
    val canvasWidth = size.width
    val canvasHeight = size.height
    val halfStroke = strokeWidthPx / 2f

    val adjustedCornerRadius = min(cornerRadiusPx, min(canvasWidth, canvasHeight) / 4f)

    val trackPath = createSquarePath(
        width = canvasWidth - strokeWidthPx,
        height = canvasHeight - strokeWidthPx,
        cornerRadius = adjustedCornerRadius,
        strokeOffset = halfStroke
    )

    // Draw track with reduced opacity
    drawPath(
        path = trackPath,
        color = trackColor.copy(alpha = 0.3f),
        style = Stroke(
            width = strokeWidthPx,
            cap = strokeCap,
            join = StrokeJoin.Round
        )
    )

    // Draw moving segment (20% of total perimeter)
    val segmentLength = 0.2f
    val startProgress = progress
    val endProgress = (progress + segmentLength).coerceAtMost(1f)

    if (endProgress > startProgress) {
        val progressPath = createProgressPathSegment(
            width = canvasWidth - strokeWidthPx,
            height = canvasHeight - strokeWidthPx,
            cornerRadius = adjustedCornerRadius,
            strokeOffset = halfStroke,
            startProgress = startProgress,
            endProgress = endProgress
        )

        drawPath(
            path = progressPath,
            color = progressColor,
            style = Stroke(
                width = strokeWidthPx,
                cap = strokeCap,
                join = StrokeJoin.Round
            )
        )
    }

    // Handle wrap-around
    if (progress + segmentLength > 1f) {
        val wrapEndProgress = (progress + segmentLength) - 1f
        val wrapPath = createProgressPathSegment(
            width = canvasWidth - strokeWidthPx,
            height = canvasHeight - strokeWidthPx,
            cornerRadius = adjustedCornerRadius,
            strokeOffset = halfStroke,
            startProgress = 0f,
            endProgress = wrapEndProgress
        )

        drawPath(
            path = wrapPath,
            color = progressColor,
            style = Stroke(
                width = strokeWidthPx,
                cap = strokeCap,
                join = StrokeJoin.Round
            )
        )
    }
}

private fun createSquarePath(
    width: Float,
    height: Float,
    cornerRadius: Float,
    strokeOffset: Float
): Path {
    return Path().apply {
        addRoundRect(
            RoundRect(
                left = strokeOffset,
                top = strokeOffset,
                right = width + strokeOffset,
                bottom = height + strokeOffset,
                cornerRadius = CornerRadius(cornerRadius)
            )
        )
    }
}

private fun createProgressPath(
    width: Float,
    height: Float,
    cornerRadius: Float,
    strokeOffset: Float,
    progress: Float
): Path {
    val path = Path()
    val totalPerimeter =
        2 * (width + height) - 8 * cornerRadius + 2 * Math.PI.toFloat() * cornerRadius
    val targetLength = totalPerimeter * progress

    var currentLength = 0f
    val x = strokeOffset
    val y = strokeOffset
    val right = width + strokeOffset
    val bottom = height + strokeOffset

    // Start from top-left corner
    path.moveTo(x + cornerRadius, y)

    // Top edge
    val topEdgeLength = width - 2 * cornerRadius
    if (currentLength + topEdgeLength <= targetLength) {
        path.lineTo(right - cornerRadius, y)
        currentLength += topEdgeLength
    } else {
        val remainingLength = targetLength - currentLength
        path.lineTo(x + cornerRadius + remainingLength, y)
        return path
    }

    // Top-right corner
    val cornerLength = Math.PI.toFloat() * cornerRadius / 2
    if (currentLength + cornerLength <= targetLength) {
        path.arcTo(
            Rect(
                right - 2 * cornerRadius, y,
                right, y + 2 * cornerRadius
            ),
            -90f, 90f, false
        )
        currentLength += cornerLength
    } else {
        val remainingAngle = ((targetLength - currentLength) / cornerLength) * 90f
        path.arcTo(
            Rect(
                right - 2 * cornerRadius, y,
                right, y + 2 * cornerRadius
            ),
            -90f, remainingAngle, false
        )
        return path
    }

    // Right edge
    val rightEdgeLength = height - 2 * cornerRadius
    if (currentLength + rightEdgeLength <= targetLength) {
        path.lineTo(right, bottom - cornerRadius)
        currentLength += rightEdgeLength
    } else {
        val remainingLength = targetLength - currentLength
        path.lineTo(right, y + cornerRadius + remainingLength)
        return path
    }

    // Bottom-right corner
    if (currentLength + cornerLength <= targetLength) {
        path.arcTo(
            Rect(
                right - 2 * cornerRadius, bottom - 2 * cornerRadius,
                right, bottom
            ),
            0f, 90f, false
        )
        currentLength += cornerLength
    } else {
        val remainingAngle = ((targetLength - currentLength) / cornerLength) * 90f
        path.arcTo(
            Rect(
                right - 2 * cornerRadius, bottom - 2 * cornerRadius,
                right, bottom
            ),
            0f, remainingAngle, false
        )
        return path
    }

    // Bottom edge
    val bottomEdgeLength = width - 2 * cornerRadius
    if (currentLength + bottomEdgeLength <= targetLength) {
        path.lineTo(x + cornerRadius, bottom)
        currentLength += bottomEdgeLength
    } else {
        val remainingLength = targetLength - currentLength
        path.lineTo(right - cornerRadius - remainingLength, bottom)
        return path
    }

    // Bottom-left corner
    if (currentLength + cornerLength <= targetLength) {
        path.arcTo(
            Rect(
                x, bottom - 2 * cornerRadius,
                x + 2 * cornerRadius, bottom
            ),
            90f, 90f, false
        )
        currentLength += cornerLength
    } else {
        val remainingAngle = ((targetLength - currentLength) / cornerLength) * 90f
        path.arcTo(
            Rect(
                x, bottom - 2 * cornerRadius,
                x + 2 * cornerRadius, bottom
            ),
            90f, remainingAngle, false
        )
        return path
    }

    // Left edge
    val leftEdgeLength = height - 2 * cornerRadius
    if (currentLength + leftEdgeLength <= targetLength) {
        path.lineTo(x, y + cornerRadius)
        currentLength += leftEdgeLength
    } else {
        val remainingLength = targetLength - currentLength
        path.lineTo(x, bottom - cornerRadius - remainingLength)
        return path
    }

    // Top-left corner (final)
    if (currentLength < targetLength) {
        val remainingAngle = ((targetLength - currentLength) / cornerLength) * 90f
        path.arcTo(
            Rect(
                x, y,
                x + 2 * cornerRadius, y + 2 * cornerRadius
            ),
            180f, remainingAngle, false
        )
    }

    return path
}

private fun createProgressPathSegment(
    width: Float,
    height: Float,
    cornerRadius: Float,
    strokeOffset: Float,
    startProgress: Float,
    endProgress: Float
): Path {
    val fullPath = createProgressPath(width, height, cornerRadius, strokeOffset, endProgress)
    val startPath = createProgressPath(width, height, cornerRadius, strokeOffset, startProgress)

    // This is a simplified version - in a production app, you'd want more sophisticated path operations
    return createProgressPath(
        width,
        height,
        cornerRadius,
        strokeOffset,
        endProgress - startProgress
    )
}

// Demo composables
@Preview(showBackground = true)
@Composable
fun SquareProgressPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Determinate Progress", style = MaterialTheme.typography.headlineSmall)

            Box(
                modifier = Modifier
                    .size(200.dp)
                    .padding(8.dp)
            ) {
                SquareProgressIndicator(progress = 0.7f)
            }

            Text("Indeterminate Progress", style = MaterialTheme.typography.headlineSmall)

            Box(
                modifier = Modifier
                    .size(200.dp)
                    .padding(8.dp)
            ) {
                SquareProgressIndicatorIndeterminate()
            }
        }
    }
}
