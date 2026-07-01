package com.cric.cricketscoring.presentation.summary

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cric.cricketscoring.domain.model.BatsmanScore
import com.cric.cricketscoring.domain.model.BowlerStats
import com.cric.cricketscoring.domain.model.FallOfWicket
import com.cric.cricketscoring.domain.model.InningsScore
import com.cric.cricketscoring.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchSummaryScreen(
    onBack: () -> Unit,
    viewModel: MatchSummaryViewModel = hiltViewModel()
) {
    val c = LocalAppColors.current
    val state          by viewModel.uiState.collectAsState()
    val isPdfGenerating by viewModel.isPdfGenerating.collectAsState()
    val context        = LocalContext.current

    // Launch system share sheet when PDF is ready
    LaunchedEffect(Unit) {
        viewModel.pdfUri.collect { uri ->
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Cricket Scorecard — ${state.result}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share Scorecard PDF"))
        }
    }

    Scaffold(
        containerColor = c.bg,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Match Summary",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = c.textPrimary
                        )
                        Text(
                            "Full scorecard",
                            fontSize = 11.sp,
                            color = c.textSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Home, "Home", tint = EmeraldPrimary)
                    }
                },
                actions = {
                    IconButton(
                        onClick  = { viewModel.generatePdf() },
                        enabled  = !state.isLoading && !isPdfGenerating
                    ) {
                        if (isPdfGenerating) {
                            CircularProgressIndicator(
                                modifier    = Modifier.size(20.dp),
                                color       = EmeraldPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Share, contentDescription = "Share as PDF", tint = EmeraldPrimary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = c.bg)
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = EmeraldPrimary)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(c.bg)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Result Banner ──────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(listOf(GoldContainer, c.surface3)),
                        RoundedCornerShape(16.dp)
                    )
                    .border(1.dp, GoldPrimary.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🏆", fontSize = 36.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        state.result,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = GoldPrimary
                    )
                }
            }

            // ── Innings Cards ──────────────────────────────────────────────
            state.innings1Score?.let { InningsScorecardCard(it) }

            state.innings2Score?.let {
                if (it.batsmen.isNotEmpty() || it.totalRuns > 0) {
                    InningsScorecardCard(it)
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun InningsScorecardCard(score: InningsScore) {
    val c = LocalAppColors.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = c.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, c.outline)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${score.battingTeamName} Innings",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall,
                    color = EmeraldPrimary
                )
                Box(
                    modifier = Modifier
                        .background(EmeraldContainer, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        "${score.totalRuns}/${score.wickets} (${score.oversDisplay})",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium,
                        color = EmeraldLight
                    )
                }
            }

            HorizontalDivider(color = c.divider)

            // ── Batting ──────────────────────────────────────────────────
            SectionLabel("Batting", "🏏")
            Row(Modifier.fillMaxWidth()) {
                Text("Batsman", Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = c.textSecondary)
                listOf("R", "B", "4s", "6s").forEach {
                    Text(it, Modifier.width(30.dp), textAlign = TextAlign.End, style = MaterialTheme.typography.labelSmall, color = c.textSecondary)
                }
                Text("SR", Modifier.width(46.dp), textAlign = TextAlign.End, style = MaterialTheme.typography.labelSmall, color = c.textSecondary)
            }
            score.batsmen.filter { it.balls > 0 || it.isOut }.forEach { b ->
                BattingRow(b)
            }

            // Extras
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Extras", style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
                Text(
                    "${score.extras} (w ${score.wides}, nb ${score.noBalls}, b ${score.byes}, lb ${score.legByes})",
                    style = MaterialTheme.typography.bodySmall,
                    color = c.textSecondary
                )
            }

            HorizontalDivider(color = c.divider)

            // ── Bowling ──────────────────────────────────────────────────
            SectionLabel("Bowling", "⚾")
            Row(Modifier.fillMaxWidth()) {
                Text("Bowler", Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = c.textSecondary)
                listOf("O", "M", "R", "W", "Eco").forEach {
                    Text(it, Modifier.width(30.dp), textAlign = TextAlign.End, style = MaterialTheme.typography.labelSmall, color = c.textSecondary)
                }
            }
            score.bowlers.filter { it.totalLegalBalls > 0 }.forEach { b ->
                BowlingRow(b)
            }

            // ── Fall of Wickets ───────────────────────────────────────────
            if (score.fallOfWickets.isNotEmpty()) {
                HorizontalDivider(color = c.divider)
                FallOfWicketsSection(score.fallOfWickets)
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String, emoji: String) {
    val c = LocalAppColors.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(emoji, fontSize = 14.sp)
        Text(
            text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = c.textPrimary
        )
    }
}

@Composable
private fun BattingRow(b: BatsmanScore) {
    val c = LocalAppColors.current
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(
                b.player.name,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = c.textPrimary
            )
            b.dismissalInfo?.let {
                Text(it, style = MaterialTheme.typography.labelSmall, color = c.textSecondary)
            } ?: Text(
                if (!b.isOut) "not out" else "",
                style = MaterialTheme.typography.labelSmall,
                color = EmeraldPrimary
            )
        }
        Text(
            "${b.runs}",
            Modifier.width(30.dp),
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = if (b.runs >= 50) GoldPrimary else c.textPrimary
        )
        Text("${b.balls}",  Modifier.width(30.dp), textAlign = TextAlign.End, style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
        Text("${b.fours}",  Modifier.width(30.dp), textAlign = TextAlign.End, style = MaterialTheme.typography.bodySmall, color = CricketBlue)
        Text("${b.sixes}",  Modifier.width(30.dp), textAlign = TextAlign.End, style = MaterialTheme.typography.bodySmall, color = CricketPurple)
        Text("${"%.1f".format(b.strikeRate)}", Modifier.width(46.dp), textAlign = TextAlign.End, style = MaterialTheme.typography.bodySmall, color = c.textSecondary, maxLines = 1, softWrap = false)
    }
}

@Composable
private fun BowlingRow(b: BowlerStats) {
    val c = LocalAppColors.current
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(b.player.name, Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, color = c.textPrimary)
        Text(b.oversDisplay,      Modifier.width(30.dp), textAlign = TextAlign.End, style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
        Text("${b.maidens}",      Modifier.width(30.dp), textAlign = TextAlign.End, style = MaterialTheme.typography.bodySmall,
            color = if (b.maidens > 0) EmeraldPrimary else c.textSecondary)
        Text("${b.runs}",         Modifier.width(30.dp), textAlign = TextAlign.End, style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
        Text(
            "${b.wickets}",
            Modifier.width(30.dp),
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = if (b.wickets > 0) CricketRed else c.textSecondary
        )
        Text("${"%.1f".format(b.economy)}", Modifier.width(30.dp), textAlign = TextAlign.End, style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
    }
}

@Composable
private fun FallOfWicketsSection(fow: List<FallOfWicket>) {
    val c = LocalAppColors.current
    SectionLabel("Fall of Wickets", "🎯")

    // Column header row
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Batsman",
            Modifier.weight(1f),
            style = MaterialTheme.typography.labelSmall,
            color = c.textSecondary
        )
        Text(
            "Score",
            Modifier.width(52.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelSmall,
            color = c.textSecondary
        )
        Text(
            "Over",
            Modifier.width(42.dp),
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.labelSmall,
            color = c.textSecondary
        )
    }

    fow.forEach { w ->
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Left: wicket chip + player name + dismissal sub-line
            Row(
                Modifier.weight(1f),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Wicket number chip
                Box(
                    modifier = Modifier
                        .padding(top = 1.dp)
                        .background(CricketRedDim, RoundedCornerShape(4.dp))
                        .padding(horizontal = 5.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "${w.wicketNumber}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = CricketRed
                    )
                }
                // Name + dismissal stacked
                Column {
                    Text(
                        w.playerName,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = c.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        w.dismissalInfo,
                        style = MaterialTheme.typography.labelSmall,
                        color = c.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Score at fall e.g. "33/3"
            Text(
                "${w.score}/${w.wicketNumber}",
                Modifier
                    .width(52.dp)
                    .padding(top = 2.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = GoldPrimary
            )

            // Over e.g. "4.3"
            Text(
                w.overDisplay,
                Modifier
                    .width(42.dp)
                    .padding(top = 2.dp),
                textAlign = TextAlign.End,
                style = MaterialTheme.typography.bodySmall,
                color = c.textSecondary
            )
        }

        HorizontalDivider(color = c.divider.copy(alpha = 0.5f))
    }
}
