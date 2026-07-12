package com.dps.businessexpensetracker.ui

import android.content.Context
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateRectAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.edit

/**
 * Remembers whether the first-run guided tour has already been shown, in a
 * dedicated preferences file so clearing expense data does not replay the tour.
 */
object GuidedTourPrefs {
    private const val PREFS_NAME = "onboarding"
    private const val KEY_TOUR_SEEN = "tour_seen_v1"

    fun isTourSeen(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_TOUR_SEEN, false)

    fun markTourSeen(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit { putBoolean(KEY_TOUR_SEEN, true) }
    }
}

/** Stable keys for the home-screen elements the tour can spotlight. */
object TourTargets {
    const val DASHBOARD = "dashboard"
    const val ADD_EXPENSE = "add_expense"
    const val SEARCH_FILTERS = "search_filters"
    const val EXPORT = "export"
    const val BACKUP = "backup"
}

data class TourStep(
    val title: String,
    val body: String,
    val icon: ImageVector,
    val targetKey: String? = null
)

/**
 * Steps are ordered to match the real workflow: form the mental model first,
 * then capture, find, report, and protect — one idea per step.
 */
fun expenseTourSteps(): List<TourStep> = listOf(
    TourStep(
        title = "Welcome to Business Tracker",
        body = "Keep expenses and daily sales together in one local business register. " +
            "This short tour shows where everything lives — it takes under a minute.",
        icon = Icons.AutoMirrored.Outlined.ReceiptLong
    ),
    TourStep(
        title = "Your money at a glance",
        body = "The dashboard shows operating cashflow, today's sales, received revenue, " +
            "paid expenses, and pending sales. Switch between the Expenses and Sales " +
            "ledgers directly below it.",
        icon = Icons.Outlined.Insights,
        targetKey = TourTargets.DASHBOARD
    ),
    TourStep(
        title = "Add expenses or sales",
        body = "In Expenses, scan a bill or enter it manually. In Sales, record daily " +
            "revenue, payment status, customer, channel, quantity, tax, and discount. " +
            "Invoice scanning stays on your phone.",
        icon = Icons.Outlined.Add,
        targetKey = TourTargets.ADD_EXPENSE
    ),
    TourStep(
        title = "Find any expense fast",
        body = "Each ledger has its own search, status filters, category or channel " +
            "filters, and sorting. Exports use the currently selected filtered ledger.",
        icon = Icons.Outlined.Search,
        targetKey = TourTargets.SEARCH_FILTERS
    ),
    TourStep(
        title = "Share reports",
        body = "Export the current view as CSV for Excel or accounting software, or as " +
            "an HTML report anyone can open in a browser — no app needed on their side.",
        icon = Icons.Outlined.FileDownload,
        targetKey = TourTargets.EXPORT
    ),
    TourStep(
        title = "Keep your records safe",
        body = "Everything is stored on this phone. Backups now include both ledgers and " +
            "expense attachments, and can restore older expense-only v1 backups too.",
        icon = Icons.Outlined.Backup,
        targetKey = TourTargets.BACKUP
    ),
    TourStep(
        title = "You're ready",
        body = "Move expenses through review and payment, and sales through pending, " +
            "received, or refunded. You can replay this tour anytime from the ⋮ menu.",
        icon = Icons.Outlined.CheckCircle
    )
)

/** Records this element's on-screen bounds so the tour can spotlight it. */
fun Modifier.tourTarget(
    bounds: MutableMap<String, Rect>,
    key: String
): Modifier = onGloballyPositioned { coordinates ->
    bounds[key] = coordinates.boundsInRoot()
}

@Composable
fun GuidedTourOverlay(
    steps: List<TourStep>,
    stepIndex: Int,
    targetBounds: Map<String, Rect>,
    onStepChange: (Int) -> Unit,
    onFinish: () -> Unit
) {
    val step = steps.getOrNull(stepIndex) ?: return
    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .testTag("tour_overlay")
            // Swallow every tap so the dimmed UI cannot be pressed mid-tour.
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            )
    ) {
        val screenHeightPx = with(density) { maxHeight.toPx() }
        // Ignore targets that scrolled out of view (their recorded bounds go
        // stale); a centered card without a spotlight is clearer than a wrong one.
        val targetRect = step.targetKey?.let(targetBounds::get)
            ?.takeIf { it.width > 0f && it.bottom > 0f && it.top < screenHeightPx }
        val highlightRect = targetRect?.let { rect ->
            val inflate = with(density) { 8.dp.toPx() }
            Rect(
                left = rect.left - inflate,
                top = rect.top - inflate,
                right = rect.right + inflate,
                bottom = rect.bottom + inflate
            )
        }
        TourScrim(highlightRect)

        // Place the explanation card on the opposite half of the screen from the
        // spotlighted element so the card never covers what it is describing.
        val cardAlignment = when {
            highlightRect == null -> Alignment.Center
            highlightRect.center.y > screenHeightPx / 2 -> Alignment.TopCenter
            else -> Alignment.BottomCenter
        }

        Box(
            modifier = Modifier
                .align(cardAlignment)
                .padding(
                    horizontal = 20.dp,
                    vertical = if (cardAlignment == Alignment.Center) 0.dp else 72.dp
                )
        ) {
            TourStepCard(
                step = step,
                stepIndex = stepIndex,
                stepCount = steps.size,
                onBack = { onStepChange(stepIndex - 1) },
                onNext = {
                    if (stepIndex == steps.lastIndex) onFinish() else onStepChange(stepIndex + 1)
                },
                onSkip = onFinish
            )
        }
    }
}

@Composable
private fun TourScrim(highlightRect: Rect?) {
    val animatedRect by animateRectAsState(
        targetValue = highlightRect ?: Rect.Zero,
        animationSpec = tween(durationMillis = 350),
        label = "tour_highlight"
    )
    val pulse by rememberInfiniteTransition(label = "tour_pulse").animateFloat(
        initialValue = 0.35f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tour_pulse_alpha"
    )
    val showCutout = highlightRect != null && animatedRect.width > 1f

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    ) {
        drawRect(Color.Black.copy(alpha = 0.72f))
        if (showCutout) {
            val corner = CornerRadius(18.dp.toPx())
            drawRoundRect(
                color = Color.Transparent,
                topLeft = animatedRect.topLeft,
                size = animatedRect.size,
                cornerRadius = corner,
                blendMode = BlendMode.Clear
            )
            drawRoundRect(
                color = Color.White.copy(alpha = pulse),
                topLeft = animatedRect.topLeft,
                size = animatedRect.size,
                cornerRadius = corner,
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }
}

@Composable
private fun TourStepCard(
    step: TourStep,
    stepIndex: Int,
    stepCount: Int,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("tour_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = step.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = step.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                text = step.body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(stepCount) { index ->
                    Box(
                        modifier = Modifier
                            .size(if (index == stepIndex) 8.dp else 6.dp)
                            .background(
                                color = if (index == stepIndex) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                },
                                shape = CircleShape
                            )
                    )
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text = "${stepIndex + 1} of $stepCount",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (stepIndex < stepCount - 1) {
                    TextButton(
                        onClick = onSkip,
                        modifier = Modifier.testTag("tour_skip")
                    ) {
                        Text("Skip tour")
                    }
                }
                Spacer(Modifier.weight(1f))
                if (stepIndex > 0) {
                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("tour_back")
                    ) {
                        Text("Back")
                    }
                    Spacer(Modifier.width(8.dp))
                }
                Button(
                    onClick = onNext,
                    modifier = Modifier.testTag("tour_next")
                ) {
                    Text(if (stepIndex == stepCount - 1) "Get started" else "Next")
                }
            }
        }
    }
}
