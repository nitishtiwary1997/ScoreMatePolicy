package com.cric.cricketscoring.presentation.tournament.fixture

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cric.cricketscoring.domain.model.Fixture
import com.cric.cricketscoring.domain.model.FixtureStage
import com.cric.cricketscoring.domain.model.FixtureStatus
import com.cric.cricketscoring.domain.model.TournamentTeam
import com.cric.cricketscoring.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FixtureListScreen(
    onBack: () -> Unit,
    onFixtureClick: (fixtureId: String) -> Unit,
    viewModel: FixtureListViewModel = hiltViewModel()
) {
    val c = LocalAppColors.current
    val tournament by viewModel.tournament.collectAsState()
    val fixtures by viewModel.fixtures.collectAsState()
    val teamsMap by viewModel.teamsMap.collectAsState()
    val teamCount by viewModel.teamCount.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    val grouped = remember(fixtures) { fixtures.groupedByStage() }
    val snackbarHost = remember { SnackbarHostState() }
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHost.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        containerColor = c.bg,
        snackbarHost = { SnackbarHost(snackbarHost) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Fixtures",
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
                    if (fixtures.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .background(EmeraldContainer, RoundedCornerShape(8.dp))
                                .border(1.dp, EmeraldPrimary.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                "${fixtures.size} matches",
                                style = MaterialTheme.typography.labelMedium,
                                color = EmeraldPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = c.bg)
            )
        },
        floatingActionButton = {
            if (fixtures.isEmpty() && teamCount >= 2) {
                ExtendedFloatingActionButton(
                    onClick = { showDatePicker = true },
                    containerColor = EmeraldPrimary,
                    contentColor = Color.Black,
                    shape = RoundedCornerShape(16.dp),
                    icon = {
                        if (uiState.isGenerating) {
                            CircularProgressIndicator(
                                color = Color.Black,
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.CalendarMonth, null, modifier = Modifier.size(20.dp))
                        }
                    },
                    text = {
                        Text(
                            if (uiState.isGenerating) "Generating…" else "Generate Schedule",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                )
            }
        }
    ) { padding ->
        when {
            fixtures.isEmpty() -> {
                FixtureEmptyState(
                    teamCount = teamCount,
                    isGenerating = uiState.isGenerating,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    onGenerate = { showDatePicker = true }
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(c.bg)
                        .padding(padding),
                    contentPadding = PaddingValues(
                        start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp
                    )
                ) {
                    grouped.forEach { (stage, stageFixtures) ->
                        item(key = stage.name) {
                            StageHeader(stage = stage, count = stageFixtures.size)
                            Spacer(Modifier.height(6.dp))
                        }
                        items(stageFixtures, key = { it.id }) { fixture ->
                            FixtureCard(
                                fixture = fixture,
                                team1 = teamsMap[fixture.team1Id],
                                team2 = teamsMap[fixture.team2Id],
                                onClick = { onFixtureClick(fixture.id) }
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                        item(key = "${stage.name}_space") { Spacer(Modifier.height(8.dp)) }
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val c2 = LocalAppColors.current
        val dateState = rememberDatePickerState(
            initialSelectedDateMillis = System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                Button(
                    onClick = {
                        val selected = dateState.selectedDateMillis
                            ?: System.currentTimeMillis()
                        showDatePicker = false
                        viewModel.generateSchedule(selected)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmeraldPrimary,
                        contentColor = Color.Black
                    )
                ) { Text("Generate", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = c2.textSecondary)
                }
            }
        ) {
            DatePicker(state = dateState, title = { Text("  Select start date", color = c2.textPrimary) })
        }
    }
}

// ── Stage Header ──────────────────────────────────────────────────────────────

@Composable
private fun StageHeader(stage: FixtureStage, count: Int) {
    val c = LocalAppColors.current
    val color = when (stage) {
        FixtureStage.GROUP              -> EmeraldPrimary
        FixtureStage.QUARTER_FINAL      -> Color(0xFF64B5F6)
        FixtureStage.SEMI_FINAL         -> GoldPrimary
        FixtureStage.THIRD_PLACE_PLAYOFF -> c.textSecondary
        FixtureStage.FINAL              -> GoldPrimary
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(Modifier.width(3.dp).height(16.dp).background(color, RoundedCornerShape(2.dp)))
        Text(
            stage.label.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )
        Box(
            modifier = Modifier
                .background(color.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 1.dp)
        ) {
            Text(
                count.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )
        }
    }
}

// ── Fixture Card ──────────────────────────────────────────────────────────────

@Composable
private fun FixtureCard(
    fixture: Fixture,
    team1: TournamentTeam?,
    team2: TournamentTeam?,
    onClick: () -> Unit
) {
    val c = LocalAppColors.current
    val (accentColor, statusLabel, bgColor) = when (fixture.status) {
        FixtureStatus.UPCOMING  -> Triple(EmeraldPrimary, "UPCOMING", EmeraldContainer)
        FixtureStatus.LIVE      -> Triple(LiveRed, "LIVE", Color(0x1FFF4444))
        FixtureStatus.COMPLETED -> Triple(DoneGreen, "DONE", Color(0x1F4CAF50))
        FixtureStatus.ABANDONED -> Triple(c.textTertiary, "ABANDONED", c.surface2)
        FixtureStatus.NO_RESULT -> Triple(GoldPrimary, "NO RESULT", GoldContainer)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(c.surface)
            .border(1.dp, c.outline, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .background(
                        Brush.verticalGradient(listOf(accentColor, accentColor.copy(alpha = 0.2f)))
                    )
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 14.dp, top = 12.dp, end = 14.dp, bottom = 12.dp)
            ) {
                // Header row: match number + status + date
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .background(c.surface2, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "M${fixture.matchNumber}",
                                style = MaterialTheme.typography.labelSmall,
                                color = c.textTertiary,
                                fontSize = 9.sp
                            )
                        }
                        StatusPill(label = statusLabel, color = accentColor, bg = bgColor)
                        if (fixture.stage == FixtureStage.GROUP) {
                            Box(
                                modifier = Modifier
                                    .background(c.surface2, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    "Grp ${fixture.groupName}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = c.textTertiary,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(Icons.Default.Schedule, null, tint = c.textTertiary, modifier = Modifier.size(11.dp))
                        Text(
                            formatFixtureDate(fixture.scheduledAt),
                            style = MaterialTheme.typography.labelSmall,
                            color = c.textTertiary,
                            fontSize = 10.sp
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Teams
                TeamMatchupRow(team1 = team1, team2 = team2)

                // Result summary
                if (fixture.resultSummary.isNotBlank()) {
                    Spacer(Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DoneGreen.copy(alpha = 0.07f), RoundedCornerShape(6.dp))
                            .border(1.dp, DoneGreen.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            fixture.resultSummary,
                            style = MaterialTheme.typography.bodySmall,
                            color = DoneGreen,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

// ── Team Matchup Row ──────────────────────────────────────────────────────────

@Composable
private fun TeamMatchupRow(team1: TournamentTeam?, team2: TournamentTeam?) {
    val c = LocalAppColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Team 1
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.Start
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val name = team1?.name ?: "TBD"
                TeamAvatar(name = name, size = 32)
                Text(
                    name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = c.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        // VS divider
        Box(
            modifier = Modifier.padding(horizontal = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "vs",
                style = MaterialTheme.typography.labelSmall,
                color = c.textTertiary,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        }
        // Team 2
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.End
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val name = team2?.name ?: "TBD"
                Text(
                    name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = c.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                TeamAvatar(name = name, size = 32)
            }
        }
    }
}

// ── Team Avatar ───────────────────────────────────────────────────────────────

@Composable
private fun TeamAvatar(name: String, size: Int) {
    val idx = abs(name.hashCode()) % avatarBgs.size
    Box(
        modifier = Modifier
            .size(size.dp)
            .background(avatarBgs[idx], RoundedCornerShape((size / 4).dp))
            .border(1.dp, avatarFgs[idx].copy(alpha = 0.5f), RoundedCornerShape((size / 4).dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            name.split(" ").take(2).joinToString("") { it.firstOrNull()?.uppercase() ?: "" }.take(2).ifBlank { name.take(2).uppercase() },
            fontSize = (size / 3.2f).sp,
            fontWeight = FontWeight.ExtraBold,
            color = avatarFgs[idx]
        )
    }
}

// ── Status Pill ───────────────────────────────────────────────────────────────

@Composable
private fun StatusPill(label: String, color: Color, bg: Color) {
    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(4.dp))
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold, fontSize = 9.sp, letterSpacing = 0.5.sp)
    }
}

// ── Empty State ───────────────────────────────────────────────────────────────

@Composable
private fun FixtureEmptyState(
    teamCount: Int,
    isGenerating: Boolean,
    modifier: Modifier = Modifier,
    onGenerate: () -> Unit
) {
    val c = LocalAppColors.current
    Column(
        modifier = modifier.padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .background(EmeraldContainer, CircleShape)
                .border(2.dp, EmeraldPrimary.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Schedule,
                null,
                tint = EmeraldPrimary,
                modifier = Modifier.size(48.dp)
            )
        }
        Spacer(Modifier.height(20.dp))
        Text("No Fixtures Yet", style = MaterialTheme.typography.titleMedium, color = c.textPrimary, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(
            when {
                teamCount < 2 -> "Add at least 2 teams to generate a schedule."
                else -> "All teams are registered. Tap the button below to auto-generate the full match schedule."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = c.textSecondary,
            textAlign = TextAlign.Center
        )
        if (teamCount >= 2) {
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onGenerate,
                enabled = !isGenerating,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 28.dp, vertical = 12.dp)
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Generating…", fontWeight = FontWeight.Bold)
                } else {
                    Icon(Icons.Default.CalendarMonth, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Generate Schedule", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private val dateFmt = SimpleDateFormat("d MMM, h:mm a", Locale.getDefault())
private fun formatFixtureDate(ms: Long) = dateFmt.format(Date(ms))

private val avatarBgs = listOf(
    Color(0xFF1E3A5F), Color(0xFF2D1B4E), Color(0xFF1A3A2A),
    Color(0xFF3A1A1A), Color(0xFF1A2E3A), Color(0xFF2A2A14)
)
private val avatarFgs = listOf(
    Color(0xFF64B5F6), Color(0xFFCE93D8), EmeraldPrimary,
    Color(0xFFEF9A9A), Color(0xFF80DEEA), GoldPrimary
)
