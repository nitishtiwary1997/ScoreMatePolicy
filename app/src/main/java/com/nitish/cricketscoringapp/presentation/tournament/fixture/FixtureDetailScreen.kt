package com.nitish.cricketscoringapp.presentation.tournament.fixture

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nitish.cricketscoringapp.domain.model.FixtureStatus
import com.nitish.cricketscoringapp.domain.model.TournamentTeam
import com.nitish.cricketscoringapp.ui.theme.DarkBg
import com.nitish.cricketscoringapp.ui.theme.DarkSurface
import com.nitish.cricketscoringapp.ui.theme.DividerColor
import com.nitish.cricketscoringapp.ui.theme.DoneGreen
import com.nitish.cricketscoringapp.ui.theme.EmeraldContainer
import com.nitish.cricketscoringapp.ui.theme.EmeraldPrimary
import com.nitish.cricketscoringapp.ui.theme.GoldContainer
import com.nitish.cricketscoringapp.ui.theme.GoldPrimary
import com.nitish.cricketscoringapp.ui.theme.LiveRed
import com.nitish.cricketscoringapp.ui.theme.OutlineColor
import com.nitish.cricketscoringapp.ui.theme.TextPrimary
import com.nitish.cricketscoringapp.ui.theme.TextSecondary
import com.nitish.cricketscoringapp.ui.theme.TextTertiary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FixtureDetailScreen(
    onBack: () -> Unit,
    onMatchStarted: (matchId: String) -> Unit,
    onMatchClick: (matchId: String) -> Unit,
    onWatchLive: (matchId: String) -> Unit = {},
    viewModel: FixtureDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHost = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let { snackbarHost.showSnackbar(it); viewModel.clearError() }
    }

    LaunchedEffect(state.startedMatchId) {
        state.startedMatchId?.let {
            onMatchStarted(it)
            viewModel.clearStartedMatch()
        }
    }

    Scaffold(
        containerColor = DarkBg,
        snackbarHost = { SnackbarHost(snackbarHost) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        state.fixture?.stageLabel ?: "Fixture",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = TextSecondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBg)
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = EmeraldPrimary) }
            return@Scaffold
        }

        val fixture = state.fixture ?: return@Scaffold
        val team1 = state.team1
        val team2 = state.team2
        val tournament = state.tournament

        val statusColor = when (fixture.status) {
            FixtureStatus.UPCOMING  -> EmeraldPrimary
            FixtureStatus.LIVE      -> LiveRed
            FixtureStatus.COMPLETED -> DoneGreen
            else                    -> TextTertiary
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBg)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Hero card ─────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(statusColor.copy(alpha = 0.12f), DarkSurface)
                        )
                    )
                    .border(1.dp, statusColor.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Match label
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatusBadge(label = fixture.status.label.uppercase(), color = statusColor)
                        Box(
                            modifier = Modifier
                                .background(DarkSurface.copy(alpha = 0.7f), RoundedCornerShape(5.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "Match ${fixture.matchNumber}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // Teams vs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TeamHeroColumn(team = team1, modifier = Modifier.weight(1f), align = Alignment.Start)
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.SportsCricket,
                                null,
                                tint = statusColor.copy(alpha = 0.4f),
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "VS",
                                style = MaterialTheme.typography.titleSmall,
                                color = TextTertiary,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp
                            )
                        }
                        TeamHeroColumn(team = team2, modifier = Modifier.weight(1f), align = Alignment.End)
                    }

                    // Result summary
                    if (fixture.resultSummary.isNotBlank()) {
                        Spacer(Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DoneGreen.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                                .border(1.dp, DoneGreen.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, null, tint = DoneGreen, modifier = Modifier.size(14.dp))
                                Text(
                                    fixture.resultSummary,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = DoneGreen,
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // ── Info card ─────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkSurface)
                    .border(1.dp, OutlineColor, RoundedCornerShape(14.dp))
            ) {
                InfoRow(
                    icon = { Icon(Icons.Default.CalendarToday, null, tint = TextTertiary, modifier = Modifier.size(14.dp)) },
                    label = "Scheduled",
                    value = longDateFmt.format(Date(fixture.scheduledAt))
                )
                HorizontalDivider(color = DividerColor, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                InfoRow(
                    icon = { Icon(Icons.Default.SportsCricket, null, tint = TextTertiary, modifier = Modifier.size(14.dp)) },
                    label = "Format",
                    value = "${tournament?.matchFormat?.label ?: "T20"} · ${tournament?.totalOvers ?: 20} overs"
                )
                if (fixture.venue.isNotBlank()) {
                    HorizontalDivider(color = DividerColor, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    InfoRow(
                        icon = { Icon(Icons.Default.Schedule, null, tint = TextTertiary, modifier = Modifier.size(14.dp)) },
                        label = "Venue",
                        value = fixture.venue
                    )
                }
                tournament?.let { t ->
                    if (t.venue.isNotBlank() && fixture.venue.isBlank()) {
                        HorizontalDivider(color = DividerColor, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                        InfoRow(
                            icon = { Icon(Icons.Default.Schedule, null, tint = TextTertiary, modifier = Modifier.size(14.dp)) },
                            label = "Venue",
                            value = t.venue
                        )
                    }
                }
            }

            // ── Action ────────────────────────────────────────────────────────
            when {
                fixture.status == FixtureStatus.UPCOMING && !fixture.hasStarted -> {
                    Button(
                        onClick = { viewModel.startMatch() },
                        enabled = !state.isStarting,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldPrimary,
                            contentColor = Color.Black
                        )
                    ) {
                        if (state.isStarting) {
                            CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(10.dp))
                            Text("Starting Match…", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        } else {
                            Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Start Match", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }

                fixture.hasStarted && fixture.matchId != null && fixture.status != FixtureStatus.COMPLETED -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { onMatchClick(fixture.matchId) },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = LiveRed, contentColor = Color.White)
                        ) {
                            Icon(Icons.Default.SportsCricket, null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Continue Scoring", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        Button(
                            onClick = { onWatchLive(fixture.matchId) },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0), contentColor = Color.White)
                        ) {
                            Text("Watch Live Score", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }

                fixture.status == FixtureStatus.COMPLETED && fixture.matchId != null -> {
                    Button(
                        onClick = { onMatchClick(fixture.matchId) },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DoneGreen, contentColor = Color.Black)
                    ) {
                        Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("View Scorecard", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }
    }
}

// ── Team Hero Column ──────────────────────────────────────────────────────────

@Composable
private fun TeamHeroColumn(
    team: TournamentTeam?,
    modifier: Modifier = Modifier,
    align: Alignment.Horizontal
) {
    val name = team?.name ?: "TBD"
    val initials = team?.initials?.take(2) ?: name.take(2).uppercase()
    val idx = abs(name.hashCode()) % heroBgs.size

    Column(
        modifier = modifier,
        horizontalAlignment = align
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(heroBgs[idx], RoundedCornerShape(16.dp))
                .border(1.5.dp, heroFgs[idx].copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(initials, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = heroFgs[idx])
        }
        Spacer(Modifier.height(8.dp))
        Text(
            name,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = if (align == Alignment.Start) TextAlign.Start else TextAlign.End,
            maxLines = 2
        )
        if (team?.shortName?.isNotBlank() == true) {
            Text(team.shortName, style = MaterialTheme.typography.labelSmall, color = heroFgs[idx], fontWeight = FontWeight.Bold, fontSize = 10.sp)
        }
    }
}

// ── Status Badge ──────────────────────────────────────────────────────────────

@Composable
private fun StatusBadge(label: String, color: Color) {
    val bg = when (color) {
        EmeraldPrimary -> EmeraldContainer
        LiveRed        -> Color(0x1FFF4444)
        DoneGreen      -> Color(0x1F4CAF50)
        else           -> GoldContainer
    }
    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(6.dp))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
    }
}

// ── Info Row ──────────────────────────────────────────────────────────────────

@Composable
private fun InfoRow(
    icon: @Composable () -> Unit,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            icon()
            Text(label, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        }
        Text(value, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private val longDateFmt = SimpleDateFormat("EEE, d MMM yyyy · h:mm a", Locale.getDefault())

private val heroBgs = listOf(
    Color(0xFF1E3A5F), Color(0xFF2D1B4E), Color(0xFF1A3A2A),
    Color(0xFF3A1A1A), Color(0xFF1A2E3A), Color(0xFF2A2A14)
)
private val heroFgs = listOf(
    Color(0xFF64B5F6), Color(0xFFCE93D8), EmeraldPrimary,
    Color(0xFFEF9A9A), Color(0xFF80DEEA), GoldPrimary
)
