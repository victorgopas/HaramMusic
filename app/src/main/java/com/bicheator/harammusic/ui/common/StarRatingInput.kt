package com.bicheator.harammusic.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun StarRatingInput(
    rating: Double,
    onRatingChange: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val noRipple = remember { MutableInteractionSource() }

    Row(modifier, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        for (i in 1..5) {
            val starValue = i.toDouble()
            val icon = when {
                rating >= starValue -> Icons.Filled.Star
                rating >= starValue - 0.5 -> Icons.Filled.StarHalf
                else -> Icons.Outlined.StarOutline
            }

            Box(Modifier.size(28.dp)) {
                Icon(icon, contentDescription = null, modifier = Modifier.fillMaxSize())

                Row(Modifier.fillMaxSize()) {
                    Box(
                        Modifier.weight(1f).fillMaxHeight()
                            .clickable(
                                interactionSource = noRipple,
                                indication = null
                            ) { onRatingChange(i - 0.5) }
                    )
                    Box(
                        Modifier.weight(1f).fillMaxHeight()
                            .clickable(
                                interactionSource = noRipple,
                                indication = null
                            ) { onRatingChange(i.toDouble()) }
                    )
                }
            }
        }
    }
}