package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GpsPoint
import com.example.ui.theme.AthleticCyan
import com.example.ui.theme.AthleticNeonGreen
import com.example.ui.theme.AthleticOrange

@Composable
fun RouteCanvas(
    routePoints: List<GpsPoint>,
    modifier: Modifier = Modifier,
    isLiveTracking: Boolean = false,
    primaryColor: Color = AthleticOrange,
    showGrid: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 8f,
        targetValue = 22f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseRadius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF14171E))
            .border(1.dp, Color(0xFF262B36), RoundedCornerShape(16.dp))
    ) {
        if (routePoints.size < 2) {
            // Waiting for GPS points
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                // Draw radar concentric circles
                drawCircle(Color(0xFF242A36), radius = 40.dp.toPx(), center = center, style = Stroke(1.5f))
                drawCircle(Color(0xFF1C222E), radius = 80.dp.toPx(), center = center, style = Stroke(1.5f))
                drawCircle(primaryColor.copy(alpha = 0.3f), radius = pulseRadius * 2f, center = center)
                drawCircle(primaryColor, radius = 6.dp.toPx(), center = center)
            }
            Text(
                text = if (isLiveTracking) "Menunggu sinyal GPS / Gerakan..." else "Tidak ada rute tercatat",
                color = Color(0xFF8E95A5),
                fontSize = 12.sp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp)
            )
        } else {
            // Render actual GPS Polyline scaled into canvas
            Canvas(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                drawRoutePolyline(
                    points = routePoints,
                    primaryColor = primaryColor,
                    isLive = isLiveTracking,
                    pulseRadius = pulseRadius,
                    pulseAlpha = pulseAlpha,
                    drawGrid = showGrid
                )
            }
        }
    }
}

private fun DrawScope.drawRoutePolyline(
    points: List<GpsPoint>,
    primaryColor: Color,
    isLive: Boolean,
    pulseRadius: Float,
    pulseAlpha: Float,
    drawGrid: Boolean
) {
    if (points.isEmpty()) return

    if (drawGrid) {
        // Draw subtle technical grid
        val stepX = size.width / 4f
        val stepY = size.height / 4f
        for (i in 1..3) {
            drawLine(
                color = Color(0xFF1F242F),
                start = Offset(stepX * i, 0f),
                end = Offset(stepX * i, size.height),
                strokeWidth = 1f
            )
            drawLine(
                color = Color(0xFF1F242F),
                start = Offset(0f, stepY * i),
                end = Offset(size.width, stepY * i),
                strokeWidth = 1f
            )
        }
    }

    // Calculate bounds with padding
    var minLat = Double.MAX_VALUE
    var maxLat = -Double.MAX_VALUE
    var minLng = Double.MAX_VALUE
    var maxLng = -Double.MAX_VALUE

    for (p in points) {
        if (p.latitude < minLat) minLat = p.latitude
        if (p.latitude > maxLat) maxLat = p.latitude
        if (p.longitude < minLng) minLng = p.longitude
        if (p.longitude > maxLng) maxLng = p.longitude
    }

    var latSpan = maxLat - minLat
    var lngSpan = maxLng - minLng

    // Prevent zero division
    if (latSpan < 0.0001) latSpan = 0.0001
    if (lngSpan < 0.0001) lngSpan = 0.0001

    val canvasW = size.width
    val canvasH = size.height

    // Coordinate conversion function
    fun toOffset(point: GpsPoint): Offset {
        val xNorm = (point.longitude - minLng) / lngSpan
        val yNorm = 1.0 - ((point.latitude - minLat) / latSpan) // Invert Y for screen coordinates
        return Offset(
            x = (xNorm * canvasW).toFloat(),
            y = (yNorm * canvasH).toFloat()
        )
    }

    val path = Path()
    val offsets = points.map { toOffset(it) }

    offsets.forEachIndexed { index, offset ->
        if (index == 0) {
            path.moveTo(offset.x, offset.y)
        } else {
            path.lineTo(offset.x, offset.y)
        }
    }

    // 1. Draw route glow underlay
    drawPath(
        path = path,
        color = primaryColor.copy(alpha = 0.25f),
        style = Stroke(
            width = 10.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )

    // 2. Draw route main stroke with vibrant gradient
    drawPath(
        path = path,
        brush = Brush.linearGradient(
            colors = listOf(AthleticNeonGreen, primaryColor, AthleticCyan)
        ),
        style = Stroke(
            width = 4.5.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )

    // 3. Draw Start Marker
    val startOffset = offsets.first()
    drawCircle(Color.Black, radius = 7.dp.toPx(), center = startOffset)
    drawCircle(AthleticNeonGreen, radius = 5.dp.toPx(), center = startOffset)

    // 4. Draw Current / End Marker
    val endOffset = offsets.last()
    if (isLive) {
        // Pulsing radar for current live position
        drawCircle(
            color = primaryColor.copy(alpha = pulseAlpha),
            radius = (pulseRadius * 1.5f).dp.toPx(),
            center = endOffset
        )
    }
    drawCircle(Color.White, radius = 7.dp.toPx(), center = endOffset)
    drawCircle(primaryColor, radius = 5.dp.toPx(), center = endOffset)
}
