package com.rpn.salatetime.presentation.screen.setting.composable

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rpn.salatetime.domain.model.Theme

// ─────────────────────────────────────────────────────────────────────────────
// Theme model (kept in domain layer — this file just maps icons/labels)
// ─────────────────────────────────────────────────────────────────────────────

private data class ThemeOption(
    val theme: Theme,
    val label: String,
    val icon: ImageVector,
)

private val themeOptions = listOf(
    ThemeOption(Theme.LIGHT,  "Light",  Icons.Default.LightMode),
    ThemeOption(Theme.DARK,   "Dark",   Icons.Default.DarkMode),
    ThemeOption(Theme.SYSTEM, "System", Icons.Default.BrightnessAuto),
)

// ─────────────────────────────────────────────────────────────────────────────
// ThemeSelection — drop this inside any SettingsCard
// ─────────────────────────────────────────────────────────────────────────────

/**
 * A three-option theme selector that works with touch, hover, and D-Pad/keyboard.
 *
 * @param selectedTheme  Currently active [Theme].
 * @param onThemeSelected Callback when the user picks a different theme.
 * @param modifier       Applied to the outer [Row].
 */
@Composable
fun ThemeSelection(
    selectedTheme: Theme,
    onThemeSelected: (Theme) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier              = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        themeOptions.forEach { option ->
            ThemeTile(
                option     = option,
                isSelected = selectedTheme == option.theme,
                onClick    = { onThemeSelected(option.theme) },
                modifier   = Modifier.weight(1f),
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Single theme tile — TV + phone focus/hover/press
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ThemeTile(
    option: ThemeOption,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused  by interactionSource.collectIsFocusedAsState()
    val isHovered  by interactionSource.collectIsHoveredAsState()
    val isPressed  by interactionSource.collectIsPressedAsState()

    val highlighted = isFocused || isHovered || isPressed || isSelected

    val containerColor by animateColorAsState(
        targetValue = when {
            isSelected  -> MaterialTheme.colorScheme.primaryContainer
            highlighted -> MaterialTheme.colorScheme.secondaryContainer
            else        -> MaterialTheme.colorScheme.surfaceVariant
        },
        animationSpec = tween(200),
        label         = "themeBg",
    )

    val contentColor by animateColorAsState(
        targetValue = when {
            isSelected  -> MaterialTheme.colorScheme.onPrimaryContainer
            highlighted -> MaterialTheme.colorScheme.onSecondaryContainer
            else        -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(200),
        label         = "themeContent",
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            isSelected  -> MaterialTheme.colorScheme.primary
            isFocused   -> MaterialTheme.colorScheme.secondary
            else        -> Color.Transparent
        },
        animationSpec = tween(150),
        label         = "themeBorder",
    )

    val scale by animateFloatAsState(
        targetValue   = if (isPressed) 0.94f else if (highlighted && !isSelected) 1.04f else 1f,
        animationSpec = tween(150),
        label         = "themeScale",
    )

    Surface(
        onClick           = onClick,
        interactionSource = interactionSource,
        modifier          = modifier
            .scale(scale)
            .border(
                width = if (isSelected || isFocused) 2.dp else 0.dp,
                color = borderColor,
                shape = MaterialTheme.shapes.large,
            ),
        shape = MaterialTheme.shapes.large,
        color = containerColor,
    ) {
        Column(
            modifier            = Modifier.padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector        = option.icon,
                contentDescription = option.label,
                tint               = contentColor,
                modifier           = Modifier.size(28.dp),
            )
            Text(
                text       = option.label,
                style      = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color      = contentColor,
                ),
            )
            // Selection indicator dot
            if (isSelected) {
                Spacer(
                    modifier = Modifier
                        .size(6.dp)
                        .then(
                            Modifier.border(
                                2.dp,
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.shapes.small,
                            )
                        ),
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Previews
// ─────────────────────────────────────────────────────────────────────────────

@Preview(name = "Theme Selection – Light selected", showBackground = true, widthDp = 400)
@Composable
private fun PreviewThemeLight() {
    MaterialTheme(lightColorScheme()) {
        Card(
            modifier  = Modifier.padding(16.dp),
            elevation = CardDefaults.cardElevation(4.dp),
        ) {
            Column(
                modifier            = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("Theme", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
                HorizontalDivider()
                ThemeSelection(
                    selectedTheme    = Theme.LIGHT,
                    onThemeSelected  = {},
                )
            }
        }
    }
}

@Preview(name = "Theme Selection – System selected", showBackground = true, widthDp = 400)
@Composable
private fun PreviewThemeSystem() {
    MaterialTheme(lightColorScheme()) {
        ThemeSelection(
            selectedTheme   = Theme.SYSTEM,
            onThemeSelected = {},
            modifier        = Modifier.padding(16.dp),
        )
    }
}
