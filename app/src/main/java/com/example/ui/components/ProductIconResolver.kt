package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R

@Composable
fun ProductIcon(
    iconKey: String,
    customImageUri: String? = null,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        if (!customImageUri.isNullOrBlank()) {
            AsyncImage(
                model = customImageUri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            val drawableRes = when (iconKey) {
                "tomato" -> R.drawable.ic_item_tomato
                "onion" -> R.drawable.ic_item_onion
                "cauliflower" -> R.drawable.ic_item_cauliflower
                "banana" -> R.drawable.ic_item_banana
                "apple" -> R.drawable.ic_item_apple
                "pear" -> R.drawable.ic_item_pear
                "pomegranate" -> R.drawable.ic_item_pomegranate
                "grapes" -> R.drawable.ic_item_grapes
                "strawberry" -> R.drawable.ic_item_strawberry
                "blueberry" -> R.drawable.ic_item_blueberry
                "fig" -> R.drawable.ic_item_fig
                "prickly_pear" -> R.drawable.ic_item_prickly_pear
                "potato" -> R.drawable.ic_item_potato
                "orange" -> R.drawable.ic_item_orange
                else -> R.drawable.ic_item_grocery
            }
            Image(
                painter = painterResource(id = drawableRes),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(6.dp)
            )
        }
    }
}
