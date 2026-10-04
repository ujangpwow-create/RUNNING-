package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GpsPoint
import com.example.ui.theme.AthleticCyan
import kotlin.math.roundToInt

@Composable
fun ElevationChart(
    points: List<GpsPoint>,
    modifier: Modifier = Modifier,
    fillColor: Color = AthleticCyan
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF14171E))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        if (points.size < 2) {
            Text(
                text = "Data ketinggian belum mencukupi",
                color = Color(0xFF6B7280),
                fontSize = 11.sp,
                modifier = Modifier.align(Alignment.Center)
            )
            return@Box
        }

        val altitudes = points.map { it.altitude }
        val minAlt = altitudes.minOrNull() ?: 0.0
        val maxAlt = altitudes.maxOrNull() ?: 1.0
        val spanAlt = maxOf(5.0, maxAlt - minAlt)

        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            val path = Path()
            val fillPath = Path()

            val stepX = width / (points.size - 1).toFloat()

            points.forEachIndexed { i, p ->
                val x = i * stepX
                val normalizedY = 1.0 - ((p.altitude - minAlt) / spanAlt)
                val y = (normalizedY * (height - 16.dp.toPx())).toFloat() + 8.dp.toPx()

                if (i == 0) {
                    path.moveTo(x, y)
                    fillPath.moveTo(x, height)
                    fillPath.lineTo(x, y)
                } else {
                    path.lineTo(x, y)
                    fillPath.lineTo(x, y)
                }
            }

            fillPath.lineTo(width, height)
            fillPath.close()

            // Fill area with gradient
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        fillColor.copy(alpha = 0.35f),
                        fillColor.copy(alpha = 0.02f)
                    )
                )
            )

            // Draw line stroke
            drawPath(
                path = path,
                color = fillColor,
                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
            )

            // Draw min and max altitude markers
            drawLine(
                color = Color(0xFF262C38),
                start = Offset(0f, 8.dp.toPx()),
                end = Offset(width, 8.dp.toPx()),
                strokeWidth = 1f
            )
        }

        // Overlay text badges
        Text(
            text = "Max: ${maxAlt.roundToInt()}m",
            color = Color(0xFF94A3B8),
            fontSize = 10.sp,
            modifier = Modifier.align(Alignment.TopStart)
        )
        Text(
            text = "Min: ${minAlt.roundToInt()}m",
            color = Color(0xFF94A3B8),
            fontSize = 10.sp,
            modifier = Modifier.align(Alignment.BottomStart)
        )
    }
}
