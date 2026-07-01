package com.cric.cricketscoring.presentation.tournament.points

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cric.cricketscoring.domain.model.PointsEntry
import com.cric.cricketscoring.ui.theme.*
import kotlin.math.abs

// Column widths (right side scrollable section)
private val COL_P   = 32.dp
private val COL_W   = 32.dp
private val COL_L   = 32.dp
private val COL_T   = 32.dp
private val COL_PTS = 40.dp
private val COL_NRR = 64.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PointsTableScreen(
    onBack: () -> Unit,
    viewModel: PointsTableViewModel = hiltViewModel()
) {
    val c = LocalAppColors.current
    val tournament by viewModel.tournament.collectAsState()
    val table by viewModel.pointsTable.collectAsState()
    val isRecalculating by viewModel.isRecalculating.collectAsState()
    val error by viewModel.error.collectAsState()
    val snackbarHost = remember { SnackbarHostState() }

    LaunchedEffect(error) {
        error?.let { snackbarHost.showSnackbar(it); viewModel.clearError() }
    }

    Scaffold(
        containerColor = c.bg,
        snackbarHost = { SnackbarHost(snackbarHost) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Points Table",
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp,
                            color = c.textPrimary
                        )
                        tournament?.let {
                            Text(
                                it.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = c.textSecondary)
                    }
                },
                actions = {
                    AnimatedVisibility(visible = isRecalculating, enter = fadeIn(), exit = fadeOut()) {
                        CircularProgressIndicator(
                            color = EmeraldPrimary,
                            modifier = Modifier
                                .size(20.dp)
                                .padding(end = 4.dp),
                            strokeWidth = 2.dp
                        )
                    }
                    IconButton(onClick = { viewModel.recalculate() }) {
                        Icon(Icons.Default.Refresh, "Recalculate", tint = EmeraldPrimary)
                    }
                    Spacer(Modifier.width(4.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = c.bg)
            )
        }
    ) { padding ->
        if (table.isEmpty()) {
            EmptyState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                isLoading = isRecalculating
            )
            return@Scaffold
        }

        val qualifiedCount = table.count { it.isQualified }
        val scrollState = rememberScrollState()

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(c.bg)
                .padding(padding),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Legend
            item {
                Spacer(Modifier.height(8.dp))
                QualificationLegend(qualifiedCount = qualifiedCount)
                Spacer(Modifier.height(8.dp))
            }

            // Header row
            item {
                TableHeader(scrollState = rememberScrollState())
            }

            // Table rows
            itemsIndexed(table, key = { _, e -> e.teamId }) { index, entry ->
                val isLastQualified = qualifiedCount > 0 && index == qualifiedCount - 1
                TableRow(
                    entry = entry,
                    position = index + 1,
                    isQualified = entry.isQualified,
                    scrollState = scrollState
                )
                if (isLastQualified && index < table.lastIndex) {
                    QualificationDivider()
                } else {
                    HorizontalDivider(
                        color = c.divider,
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            // Points system legend
            item {
                Spacer(Modifier.height(16.dp))
                PointsLegend()
            }
        }
    }
}

// ── Table Header ──────────────────────────────────────────────────────────────

@Composable
private fun TableHeader(scrollState: androidx.compose.foundation.ScrollState) {
    val c = LocalAppColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(c.surface2)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Fixed left: Pos + Team
        Row(modifier = Modifier.padding(start = 16.dp)) {
            HeaderCell("#", 28.dp, TextAlign.Center)
            Spacer(Modifier.width(8.dp))
            HeaderCell("TEAM", 160.dp, TextAlign.Start)
        }

        // Scrollable right columns
        Row(modifier = Modifier.horizontalScroll(scrollState)) {
            HeaderCell("P",   COL_P,   TextAlign.Center)
            HeaderCell("W",   COL_W,   TextAlign.Center)
            HeaderCell("L",   COL_L,   TextAlign.Center)
            HeaderCell("T",   COL_T,   TextAlign.Center)
            HeaderCell("PTS", COL_PTS, TextAlign.Center)
            HeaderCell("NRR", COL_NRR, TextAlign.Center)
            Spacer(Modifier.width(16.dp))
        }
    }
}

@Composable
private fun HeaderCell(text: String, width: Dp, align: TextAlign) {
    val c = LocalAppColors.current
    Box(modifier = Modifier.width(width), contentAlignment = Alignment.Center) {
        Text(
            text,
            style = MaterialTheme.typography.labelSmall,
            color = c.textTertiary,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            textAlign = align,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ── Table Row ─────────────────────────────────────────────────────────────────

@Composable
private fun TableRow(
    entry: PointsEntry,
    position: Int,
    isQualified: Boolean,
    scrollState: androidx.compose.foundation.ScrollState
) {
    val c = LocalAppColors.current
    val bgColor = when {
        isQualified -> EmeraldPrimary.copy(alpha = 0.04f)
        else        -> Color.Transparent
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .height(IntrinsicSize.Min)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Qualified stripe
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight()
                .background(
                    if (isQualified) EmeraldPrimary.copy(alpha = 0.6f) else Color.Transparent
                )
        )

        // Position badge
        Box(
            modifier = Modifier
                .padding(start = 13.dp)
                .size(28.dp)
                .background(posBadgeBg(position), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "$position",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = posBadgeFg(position, c.textTertiary)
            )
        }

        Spacer(Modifier.width(8.dp))

        // Team name (fixed width)
        Row(
            modifier = Modifier.width(160.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TeamDot(name = entry.teamName)
            Text(
                entry.teamName,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = c.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Scrollable stats (synced with header)
        Row(modifier = Modifier.horizontalScroll(scrollState)) {
            StatCell(entry.matchesPlayed.toString(), COL_P)
            StatCell(entry.won.toString(),           COL_W, highlight = if (entry.won > 0) DoneGreen else null)
            StatCell(entry.lost.toString(),          COL_L)
            StatCell(entry.tied.toString(),          COL_T)
            StatCell(entry.points.toString(),        COL_PTS, fontWeight = FontWeight.ExtraBold, highlight = EmeraldPrimary)
            NrrCell(nrr = entry.nrr, width = COL_NRR)
            Spacer(Modifier.width(16.dp))
        }
    }
}

@Composable
private fun StatCell(
    value: String,
    width: Dp,
    fontWeight: FontWeight = FontWeight.Normal,
    highlight: Color? = null
) {
    val c = LocalAppColors.current
    Box(modifier = Modifier.width(width), contentAlignment = Alignment.Center) {
        Text(
            value,
            fontSize = 13.sp,
            fontWeight = fontWeight,
            color = highlight ?: c.textPrimary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun NrrCell(nrr: Double, width: Dp) {
    val c = LocalAppColors.current
    val color = when {
        nrr > 0  -> DoneGreen
        nrr < 0  -> Color(0xFFEF9A9A)
        else     -> c.textSecondary
    }
    Box(modifier = Modifier.width(width), contentAlignment = Alignment.Center) {
        Text(
            nrrString(nrr),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = color,
            textAlign = TextAlign.Center
        )
    }
}

// ── Team Dot Avatar ───────────────────────────────────────────────────────────

@Composable
private fun TeamDot(name: String) {
    val idx = abs(name.hashCode()) % dotBgs.size
    Box(
        modifier = Modifier
            .size(22.dp)
            .background(dotBgs[idx], CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            name.firstOrNull()?.uppercase() ?: "?",
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            color = dotFgs[idx]
        )
    }
}

// ── Qualification Divider ─────────────────────────────────────────────────────

@Composable
private fun QualificationDivider() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(EmeraldPrimary.copy(alpha = 0.08f))
                .padding(horizontal = 16.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(EmeraldPrimary, CircleShape)
            )
            Text(
                "Qualification zone — above this line advance to knockouts",
                style = MaterialTheme.typography.labelSmall,
                color = EmeraldPrimary,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ── Qualification Legend ──────────────────────────────────────────────────────

@Composable
private fun QualificationLegend(qualifiedCount: Int) {
    if (qualifiedCount == 0) return
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(EmeraldPrimary.copy(alpha = 0.06f))
            .border(1.dp, EmeraldPrimary.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(Icons.Default.EmojiEvents, null, tint = EmeraldPrimary, modifier = Modifier.size(14.dp))
        Text(
            "Top $qualifiedCount teams qualify for knockouts",
            style = MaterialTheme.typography.labelSmall,
            color = EmeraldPrimary,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ── Points Legend ─────────────────────────────────────────────────────────────

@Composable
private fun PointsLegend() {
    val c = LocalAppColors.current
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(c.surface)
            .border(1.dp, c.outline, RoundedCornerShape(10.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            "POINTS SYSTEM",
            style = MaterialTheme.typography.labelSmall,
            color = c.textTertiary,
            letterSpacing = 1.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("Win" to "2 pts", "Tie" to "1 pt", "No Result" to "1 pt", "Loss" to "0 pts").forEach { (label, pts) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(pts, style = MaterialTheme.typography.labelMedium, color = EmeraldPrimary, fontWeight = FontWeight.Bold)
                    Text(label, style = MaterialTheme.typography.labelSmall, color = c.textSecondary, fontSize = 10.sp)
                }
            }
        }
    }
}

// ── Empty State ───────────────────────────────────────────────────────────────

@Composable
private fun EmptyState(modifier: Modifier = Modifier, isLoading: Boolean) {
    val c = LocalAppColors.current
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(color = EmeraldPrimary, modifier = Modifier.size(48.dp))
        } else {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .background(EmeraldContainer, CircleShape)
                    .border(2.dp, EmeraldPrimary.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.TableChart, null, tint = EmeraldPrimary, modifier = Modifier.size(44.dp))
            }
            Spacer(Modifier.height(20.dp))
            Text("No Table Yet", style = MaterialTheme.typography.titleMedium, color = c.textPrimary, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(
                "Complete group matches for the points table to populate automatically.",
                style = MaterialTheme.typography.bodyMedium,
                color = c.textSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun posBadgeBg(pos: Int) = when (pos) {
    1    -> Color(0x30FFD700)
    2    -> Color(0x28C0C0C0)
    3    -> Color(0x28CD7F32)
    else -> Color(0x18FFFFFF)
}

private fun posBadgeFg(pos: Int, textTertiary: Color) = when (pos) {
    1    -> Color(0xFFFFD700)
    2    -> Color(0xFFC0C0C0)
    3    -> Color(0xFFCD7F32)
    else -> textTertiary
}

private fun nrrString(nrr: Double): String {
    val prefix = if (nrr >= 0) "+" else ""
    return "$prefix${String.format("%.3f", nrr)}"
}

private val dotBgs = listOf(
    Color(0xFF1E3A5F), Color(0xFF2D1B4E), Color(0xFF1A3A2A),
    Color(0xFF3A1A1A), Color(0xFF1A2E3A), Color(0xFF2A2A14),
    Color(0xFF2A1A2A), Color(0xFF1A2A2A)
)
private val dotFgs = listOf(
    Color(0xFF64B5F6), Color(0xFFCE93D8), EmeraldPrimary,
    Color(0xFFEF9A9A), Color(0xFF80DEEA), GoldPrimary,
    Color(0xFFF48FB1), Color(0xFF80CBC4)
)
