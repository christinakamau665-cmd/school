package com.emobilis.app.ui.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
// ✅ REMOVED: unused Firebase Remote Config imports that caused the error
// import com.google.firebase.remoteconfig.FirebaseRemoteConfig
// import com.google.firebase.remoteconfig.ktx.remoteConfig
// import com.google.firebase.ktx.Firebase
import androidx.compose.runtime.*
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import kotlinx.coroutines.delay

/**
 * SplashScreen — recreates the Emobilis brand identity from the school banner:
 *   • Deep maroon-to-purple gradient background (matching the slide)
 *   • Decorative blurred circles (purple / pink)
 *   • Gold animated sun icon
 *   • Bold EMOBILIS title
 *   • "AWARD WINNING SCHOOL" tagline with a smiley accent
 *   • Animated progress indicator
 */

@Composable
fun SplashScreen(onFinished: () -> Unit) {

    // ── Animation states ───────────────────────────────────────────────────────
    var logoVisible  by remember { mutableStateOf(false) }
    var textVisible  by remember { mutableStateOf(false) }
    var tagVisible   by remember { mutableStateOf(false) }

    val sunScale by animateFloatAsState(
        targetValue  = if (logoVisible) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label        = "sunScale"
    )
    val sunRotation by animateFloatAsState(
        targetValue  = if (logoVisible) 360f else 0f,
        animationSpec = tween(1200, easing = EaseOutCubic),
        label        = "sunRotation"
    )

    LaunchedEffect(Unit) {
        delay(200);  logoVisible = true
        delay(500);  textVisible = true
        delay(400);  tagVisible  = true
        delay(1800); onFinished()
    }

    // ── Brand colours from the photo ──────────────────────────────────────────
    val deepMaroon  = Color(0xFF3B0A2A)
    val richPurple  = Color(0xFF6D1B6D)
    val midPink     = Color(0xFFB5326F)
    val gold        = Color(0xFFFFCC00)
    val softPink    = Color(0xFFFF80AB)
    val lavender    = Color(0xFF9575CD)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(deepMaroon, richPurple, midPink))
            )
    ) {

        // ── Decorative background blobs (mimic the slide's coloured circles) ──
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(color = lavender.copy(alpha = 0.35f),  radius = 180f, center = Offset(60f, size.height * 0.25f))
            drawCircle(color = softPink.copy(alpha = 0.30f),  radius = 140f, center = Offset(size.width - 40f, size.height * 0.72f))
            drawCircle(color = gold.copy(alpha = 0.08f),      radius = 220f, center = Offset(size.width * 0.7f, 80f))
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // ── Animated Gold Sun ─────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(sunScale)
                    .rotate(sunRotation),
                contentAlignment = Alignment.Center
            ) {
                // Outer glow ring
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(gold.copy(alpha = 0.18f), CircleShape)
                )
                // Sun body
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            Brush.radialGradient(listOf(Color(0xFFFFEE58), gold, Color(0xFFFF8F00))),
                            CircleShape
                        )
                )
                // Sun rays drawn with Canvas
                Canvas(modifier = Modifier.size(120.dp)) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val innerR = 44f
                    val outerR = 58f
                    val rayCount = 12
                    for (i in 0 until rayCount) {
                        val angle = Math.toRadians((i * 360.0 / rayCount)).toFloat()
                        drawLine(
                            color  = gold,
                            start  = Offset(cx + innerR * cos(angle), cy + innerR * sin(angle)),
                            end    = Offset(cx + outerR * cos(angle), cy + outerR * sin(angle)),
                            strokeWidth = 5f,
                            cap    = StrokeCap.Round
                        )
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── EMOBILIS Title ────────────────────────────────────────────────
            AnimatedVisibility(
                visible = textVisible,
                enter   = fadeIn(tween(600)) + slideInVertically { -30 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Underline bar (matches the slide's white line under "EMOBILIS")
                    Box(
                        modifier = Modifier
                            .width(260.dp)
                            .height(2.dp)
                            .background(Color.White.copy(alpha = 0.4f))
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text  = "EMOBILIS",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight   = FontWeight.ExtraBold,
                            color        = Color.White,
                            letterSpacing = 8.sp,
                            fontStyle    = FontStyle.Italic
                        )
                    )
                    Box(
                        modifier = Modifier
                            .width(260.dp)
                            .height(2.dp)
                            .background(Color.White.copy(alpha = 0.4f))
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── "AWARD WINNING SCHOOL" tagline ────────────────────────────────
            AnimatedVisibility(
                visible = tagVisible,
                enter   = fadeIn(tween(700)) + slideInVertically { 40 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text  = "AWARD WINNING SCHOOL",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight   = FontWeight.Bold,
                            color        = Color.White,
                            letterSpacing = 2.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(20.dp))

                    // Smiley face accent (from the slide)
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(
                                Brush.radialGradient(listOf(Color(0xFFFFEE58), gold)),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(72.dp)) {
                            val cx = size.width / 2
                            val cy = size.height / 2
                            val r  = size.minDimension / 2f
                            // Eyes
                            drawCircle(Color(0xFF5D4037), radius = 5f, center = Offset(cx - 12f, cy - 8f))
                            drawCircle(Color(0xFF5D4037), radius = 5f, center = Offset(cx + 12f, cy - 8f))
                            // Smile arc (approximated with lines)
                            for (a in -40..40 step 5) {
                                val rad1 = Math.toRadians(a.toDouble()).toFloat()
                                val rad2 = Math.toRadians((a + 5).toDouble()).toFloat()
                                drawLine(
                                    color       = Color(0xFF5D4037),
                                    start       = Offset(cx + 18 * sin(rad1), cy + 10 + 12 * cos(rad1) - 12),
                                    end         = Offset(cx + 18 * sin(rad2), cy + 10 + 12 * cos(rad2) - 12),
                                    strokeWidth = 4f,
                                    cap         = StrokeCap.Round
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(32.dp))

                    // Sub-label
                    Text(
                        "School Management System",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Color.White.copy(alpha = 0.75f),
                            letterSpacing = 1.sp
                        )
                    )
                    Text(
                        "Nairobi, Kenya",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.White.copy(alpha = 0.55f))
                    )

                    Spacer(Modifier.height(40.dp))

                    LinearProgressIndicator(
                        modifier = Modifier.width(160.dp).height(3.dp).clip(RoundedCornerShape(2.dp)),
                        color = gold,
                        trackColor = Color.White.copy(alpha = 0.2f)
                    )
                }
            }
        }
    }
}