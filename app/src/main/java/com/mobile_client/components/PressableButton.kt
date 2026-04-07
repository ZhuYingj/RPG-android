package com.mobile_client.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun PressableButton(
    modifier: Modifier = Modifier,
    shadowColor: Color,
    shadowDepth: Dp = 5.dp,
    shadowTopInset: Dp = 3.dp,
    enabled: Boolean = true,
    cornerRadius: Dp = 8.dp,
    content: @Composable (MutableInteractionSource, Modifier) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val darkenedColor = lerp(shadowColor, Color.Black, 0.3f)

    val animatedOffset by animateDpAsState(
        targetValue = if (isPressed) shadowDepth else 0.dp,
        label = "pressOffset"
    )

    Box(modifier = modifier.padding(bottom = shadowDepth)) {
        if(enabled) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(y = shadowDepth)
                    .padding(top = shadowTopInset, bottom = shadowTopInset)
                    .drawBehind {
                        drawRoundRect(
                            color = darkenedColor,
                            cornerRadius = CornerRadius(cornerRadius.toPx())
                        )
                    }
            )
        }
        content(interactionSource, Modifier.offset(y = animatedOffset))
    }
}
