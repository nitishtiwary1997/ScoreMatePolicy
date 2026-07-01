package com.cric.cricketscoring.presentation.tournament.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cric.cricketscoring.domain.model.Fixture
import com.cric.cricketscoring.domain.model.PointsEntry
import com.cric.cricketscoring.domain.model.TournamentStatus
import com.cric.cricketscoring.domain.model.TournamentTeam
import com.cric.cricketscoring.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentDashboardScreen(
    onBack: () -> Unit,
    onTeams: () -> Unit,
    onFixtures: () -> Unit,
    onPointsTable: () -> Unit,
    onKnockoutBracket: () -> Unit,
    onStats: () -> Unit,
    onLiveFixture: (matchId: String) -> Unit,
    viewModel: TournamentDashboardViewModel = hiltViewModel()
) {
    val c = LocalAppColors.current
    val state by viewModel.uiState.collectAsState()
    val statusUpdating by viewModel.statusUpdating.collectAsState()
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = c.bg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        state.tournament?.name ?: "Tournament",
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp,
                        color = c.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = c.textSecondary)
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.Delete, "More", tint = c.textSecondary)
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            val t = state.tournament
                            if (t?.status == TournamentStatus.UPCOMING) {
                                DropdownMenuItem(
                                    text = { Text("Start Tournament", color = EmeraldPrimary) },
                                    leadingIcon = { Icon(Icons.Default.PlayArrow, null, tint = EmeraldPrimary) },
                                    onClick = { viewModel.startTournament(); showMenu = false }
                                )
                            }
                            if (t?.status == TournamentStatus.ONGOING) {
                                DropdownMenuItem(
                                    text = { Text("Mark Completed", color = DoneGreen) },
                                    leadingIcon = { Icon(Icons.Default.CheckCircle, null, tint = DoneGreen) },
                                    onClick = { viewModel.completeTournament(); showMenu = false }
                                )
                            }
                            if (t?.status != TournamentStatus.CANCELLED && t?.status != TournamentStatus.COMPLETED) {
                                DropdownMenuItem(
                                    text = { Text("Cancel Tournament", color = c.textSecondary) },
                                    leadingIcon = { Icon(Icons.Default.Cancel, null, tint = c.textSecondary) },
                                    onClick = { viewModel.cancelTournament(); showMenu = false }
                                )
                            }
                            HorizontalDivider(color = c.divider)
                            DropdownMenuItem(
                                text = { Text("Delete Tournament", color = CricketRed) },
                                leadingIcon = { Icon(Icons.Default.Delete, null, tint = CricketRed) },
                                onClick = { showDeleteDialog = true; showMenu = false }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = c.bg)
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = GoldPrimary) }
            return@Scaffold
        }

        val tournament = state.tournament ?: return@Scaffold

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(c.bg)
                .padding(padding),
            contentPadding = PaddingValues(bottom = 40.dp)
        ) {
            // ── Tournament hero header ─────────────────────────────────────
            item {
                TournamentHeroCard(
                    tournament = tournament,
                    statusUpdating = statusUpdating,
                    modifier = Modifier.padding(16.dp)
                )
            }

            // ── Quick stats ───────────────────────────────────────────────
            item {
                QuickStatsRow(
                    teamCount = state.teamCount,
                    maxTeams = state.maxTeams,
                    totalFixtures = state.totalFixtures,
                    completedFixtures = state.completedFixtures,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(16.dp))
            }

            // ── Live fixtures banner ──────────────────────────────────────
            if (state.liveFixtures.isNotEmpty()) {
                item {
                    LiveFixturesBanner(
                        fixtures = state.liveFixtures,
                        teamsMap = state.teamsMap,
                        onFixtureClick = { fixture ->
                            fixture.matchId?.let { onLiveFixture(it) }
                        },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                }
            }

            // ── Navigation cards ──────────────────────────────────────────
            item {
                SectionHeader(title = "MANAGE", modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(Modifier.height(8.dp))
            }
            item {
                NavCardGrid(
                    state = state,
                    onTeams = onTeams,
                    onFixtures = onFixtures,
                    onPointsTable = onPointsTable,
                    onKnockoutBracket = onKnockoutBracket,
                    onStats = onStats,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(16.dp))
            }

            // ── Points table mini-preview ─────────────────────────────────
            if (state.showPointsTable && state.topPointsEntries.isNotEmpty()) {
                item {
                    SectionHeader(title = "STANDINGS", modifier = Modifier.padding(horizontal = 16.dp))
                    Spacer(Modifier.height(8.dp))
                }
                item {
                    PointsTablePreview(
                        entries = state.topPointsEntries,
                        onViewAll = onPointsTable,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                }
            }

            // ── Tournament info ───────────────────────────────────────────
            item {
                SectionHeader(title = "DETAILS", modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(Modifier.height(8.dp))
            }
            item {
                TournamentInfoCard(
                    tournament = tournament,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }

    if (showDeleteDialog) {
        val c = LocalAppColors.current
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = c.surface,
            titleContentColor = c.textPrimary,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Delete, null, tint = CricketRed, modifier = Modifier.size(20.dp))
                    Text("Delete Tournament", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    "Delete \"${state.tournament?.name}\" and all its teams, players, and fixtures? This cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = c.textSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteTournament { onBack() }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CricketRed)
                ) { Text("Delete", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = c.textSecondary)
                }
            }
        )
    }
}

// ── Tournament Hero Card ──────────────────────────────────────────────────────

@Composable
private fun TournamentHeroCard(
    tournament: com.cric.cricketscoring.domain.model.Tournament,
    statusUpdating: Boolean,
    modifier: Modifier = Modifier
) {
    val c = LocalAppColors.current
    val (statusColor, statusBg) = when (tournament.status) {
        TournamentStatus.UPCOMING  -> GoldPrimary to GoldContainer
        TournamentStatus.ONGOING   -> LiveRed to Color(0x1FFF4444)
        TournamentStatus.COMPLETED -> DoneGreen to EmeraldContainer
        TournamentStatus.CANCELLED -> c.textTertiary to c.surface2
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        statusColor.copy(alpha = 0.10f),
                        Color(0xFF0D1520),
                        c.bg
                    )
                )
            )
            .border(1.dp, statusColor.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            .padding(20.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatusBadge(label = tournament.status.label.uppercase(), color = statusColor, bg = statusBg)
                        if (statusUpdating) {
                            CircularProgressIndicator(
                                color = statusColor,
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        tournament.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = c.textPrimary,
                        lineHeight = 32.sp
                    )
                    if (tournament.organizerName.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "by ${tournament.organizerName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = c.textSecondary
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(statusColor.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                        .border(1.dp, statusColor.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.EmojiEvents, null, tint = statusColor, modifier = Modifier.size(30.dp))
                }
            }
            Spacer(Modifier.height(14.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TypeChip(tournament.tournamentType.label.substringBefore(" "))
                TypeChip(tournament.matchFormat.label)
                TypeChip(tournament.ballType.label)
            }
            Spacer(Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(Icons.Default.Schedule, null, tint = c.textTertiary, modifier = Modifier.size(12.dp))
                Text(
                    "${shortDate(tournament.startDate)}  –  ${shortDate(tournament.endDate)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = c.textSecondary,
                    fontSize = 11.sp
                )
            }
            if (tournament.venue.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.SportsCricket, null, tint = c.textTertiary, modifier = Modifier.size(12.dp))
                    Text(tournament.venue, style = MaterialTheme.typography.labelSmall, color = c.textSecondary, fontSize = 11.sp)
                }
            }
        }
    }
}

// ── Quick Stats Row ───────────────────────────────────────────────────────────

@Composable
private fun QuickStatsRow(
    teamCount: Int,
    maxTeams: Int,
    totalFixtures: Int,
    completedFixtures: Int,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        StatTile(
            value = "$teamCount/$maxTeams",
            label = "Teams",
            color = EmeraldPrimary,
            modifier = Modifier.weight(1f)
        )
        StatTile(
            value = "$completedFixtures/$totalFixtures",
            label = "Matches",
            color = GoldPrimary,
            modifier = Modifier.weight(1f)
        )
        StatTile(
            value = if (totalFixtures > 0) "${(completedFixtures * 100 / totalFixtures)}%" else "—",
            label = "Done",
            color = DoneGreen,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatTile(value: String, label: String, color: Color, modifier: Modifier = Modifier) {
    val c = LocalAppColors.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(c.surface)
            .border(1.dp, c.outline, RoundedCornerShape(12.dp))
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, fontWeight = FontWeight.Black, fontSize = 20.sp, color = color)
        Spacer(Modifier.height(2.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = c.textSecondary)
    }
}

// ── Live Fixtures Banner ──────────────────────────────────────────────────────

@Composable
private fun LiveFixturesBanner(
    fixtures: List<Fixture>,
    teamsMap: Map<String, TournamentTeam>,
    onFixtureClick: (Fixture) -> Unit,
    modifier: Modifier = Modifier
) {
    val c = LocalAppColors.current
    val transition = rememberInfiniteTransition(label = "live_pulse")
    val dotAlpha by transition.animateFloat(
        initialValue = 1f, targetValue = 0.2f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "dot"
    )
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0x1FFF4444))
            .border(1.dp, LiveRed.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(Modifier.size(8.dp).background(LiveRed.copy(alpha = dotAlpha), CircleShape))
            Text("LIVE NOW", style = MaterialTheme.typography.labelMedium, color = LiveRed, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            Text("· ${fixtures.size} match${if (fixtures.size > 1) "es" else ""}", style = MaterialTheme.typography.labelSmall, color = c.textSecondary)
        }
        fixtures.forEach { fixture ->
            val t1 = teamsMap[fixture.team1Id]?.name ?: "Team 1"
            val t2 = teamsMap[fixture.team2Id]?.name ?: "Team 2"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(c.surface)
                    .clickable { onFixtureClick(fixture) }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("$t1 vs $t2", style = MaterialTheme.typography.bodySmall, color = c.textPrimary, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Score", style = MaterialTheme.typography.labelSmall, color = LiveRed, fontWeight = FontWeight.Bold)
                    Icon(Icons.Default.ChevronRight, null, tint = LiveRed, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

// ── Navigation Card Grid ──────────────────────────────────────────────────────

@Composable
private fun NavCardGrid(
    state: TournamentDashboardUiState,
    onTeams: () -> Unit,
    onFixtures: () -> Unit,
    onPointsTable: () -> Unit,
    onKnockoutBracket: () -> Unit,
    onStats: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            NavCard(
                icon = Icons.Default.Group,
                title = "Teams",
                subtitle = "${state.teamCount} of ${state.maxTeams} registered",
                color = EmeraldPrimary,
                onClick = onTeams,
                modifier = Modifier.weight(1f)
            )
            NavCard(
                icon = Icons.Default.Schedule,
                title = "Fixtures",
                subtitle = "${state.completedFixtures}/${state.totalFixtures} matches",
                color = Color(0xFF64B5F6),
                onClick = onFixtures,
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (state.showPointsTable) {
                NavCard(
                    icon = Icons.Default.TableChart,
                    title = "Points Table",
                    subtitle = "Live standings",
                    color = GoldPrimary,
                    onClick = onPointsTable,
                    modifier = Modifier.weight(1f)
                )
            }
            if (state.showKnockoutBracket) {
                NavCard(
                    icon = Icons.Outlined.AccountTree,
                    title = "Bracket",
                    subtitle = "Knockout draw",
                    color = Color(0xFFCE93D8),
                    onClick = onKnockoutBracket,
                    modifier = Modifier.weight(1f)
                )
            }
            // Fill remaining if only one card in row
            if (state.showPointsTable && !state.showKnockoutBracket) {
                NavCard(
                    icon = Icons.Default.BarChart,
                    title = "Stats",
                    subtitle = "Top performers",
                    color = Color(0xFFFF8A65),
                    onClick = onStats,
                    modifier = Modifier.weight(1f)
                )
            } else if (!state.showPointsTable && !state.showKnockoutBracket) {
                NavCard(
                    icon = Icons.Default.BarChart,
                    title = "Stats",
                    subtitle = "Top performers",
                    color = Color(0xFFFF8A65),
                    onClick = onStats,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        if (state.showKnockoutBracket) {
            NavCard(
                icon = Icons.Default.BarChart,
                title = "Statistics",
                subtitle = "Top performers · Orange & Purple cap",
                color = Color(0xFFFF8A65),
                onClick = onStats,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun NavCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val c = LocalAppColors.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(c.surface)
            .border(1.dp, c.outline, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color.copy(alpha = 0.14f), RoundedCornerShape(10.dp))
                    .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(10.dp))
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = c.textPrimary)
            Spacer(Modifier.height(2.dp))
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = c.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

// ── Points Table Preview ──────────────────────────────────────────────────────

@Composable
private fun PointsTablePreview(
    entries: List<PointsEntry>,
    onViewAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val c = LocalAppColors.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(c.surface)
            .border(1.dp, c.outline, RoundedCornerShape(14.dp))
    ) {
        entries.forEachIndexed { idx, entry ->
            val pos = idx + 1
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Position badge
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .background(posBadgeBg(pos), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("$pos", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = posBadgeFg(pos, c.textTertiary))
                }
                // Team dot + name
                Box(
                    modifier = Modifier.size(20.dp).background(teamDotColor(entry.teamName), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(entry.teamName.firstOrNull()?.uppercase() ?: "?", fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, color = c.textPrimary)
                }
                Text(
                    entry.teamName,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = c.textPrimary,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                // Stats
                Text("${entry.matchesPlayed}G", style = MaterialTheme.typography.labelSmall, color = c.textTertiary)
                Text("${entry.won}W", style = MaterialTheme.typography.labelSmall, color = if (entry.won > 0) DoneGreen else c.textTertiary)
                Box(
                    modifier = Modifier
                        .background(EmeraldPrimary.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("${entry.points} pts", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = EmeraldPrimary)
                }
            }
            if (idx < entries.lastIndex) HorizontalDivider(color = c.divider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 14.dp))
        }
        HorizontalDivider(color = c.divider, thickness = 0.5.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onViewAll)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("View Full Table", style = MaterialTheme.typography.labelMedium, color = EmeraldPrimary, fontWeight = FontWeight.Bold)
            Icon(Icons.Default.ChevronRight, null, tint = EmeraldPrimary, modifier = Modifier.size(16.dp))
        }
    }
}

// ── Tournament Info Card ──────────────────────────────────────────────────────

@Composable
private fun TournamentInfoCard(
    tournament: com.cric.cricketscoring.domain.model.Tournament,
    modifier: Modifier = Modifier
) {
    val c = LocalAppColors.current
    val items = buildList {
        add("Format" to tournament.matchFormat.label)
        add("Overs" to "${tournament.totalOvers}")
        add("Ball Type" to tournament.ballType.label)
        add("Players/Team" to "${tournament.playersPerTeam}")
        add("Tournament Type" to tournament.tournamentType.label)
        if (tournament.venue.isNotBlank()) add("Venue" to tournament.venue)
        if (tournament.organizerContact.isNotBlank()) add("Contact" to tournament.organizerContact)
        if (tournament.entryFee > 0) add("Entry Fee" to "₹${tournament.entryFee.toInt()}")
        if (tournament.description.isNotBlank()) add("About" to tournament.description)
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(c.surface)
            .border(1.dp, c.outline, RoundedCornerShape(14.dp))
    ) {
        items.forEachIndexed { idx, (label, value) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(label, style = MaterialTheme.typography.bodySmall, color = c.textSecondary, modifier = Modifier.weight(0.4f))
                Text(value, style = MaterialTheme.typography.bodySmall, color = c.textPrimary, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(0.6f), textAlign = TextAlign.End, maxLines = 3)
            }
            if (idx < items.lastIndex) HorizontalDivider(color = c.divider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
        }
    }
}

// ── Section Header ────────────────────────────────────────────────────────────

@Composable
private fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.width(3.dp).height(14.dp).background(GoldPrimary, RoundedCornerShape(2.dp)))
        Text(title, style = MaterialTheme.typography.labelMedium, color = GoldPrimary, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
    }
}

// ── Status Badge ──────────────────────────────────────────────────────────────

@Composable
private fun StatusBadge(label: String, color: Color, bg: Color) {
    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(6.dp))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
    }
}

// ── Type Chip ─────────────────────────────────────────────────────────────────

@Composable
private fun TypeChip(label: String) {
    val c = LocalAppColors.current
    Box(
        modifier = Modifier
            .background(c.surface2, RoundedCornerShape(5.dp))
            .border(1.dp, c.outline, RoundedCornerShape(5.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = c.textSecondary, fontSize = 10.sp)
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private val dateFmt = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
private fun shortDate(ms: Long) = dateFmt.format(Date(ms))

private fun posBadgeBg(pos: Int) = when (pos) {
    1 -> Color(0x30FFD700); 2 -> Color(0x28C0C0C0); 3 -> Color(0x28CD7F32); else -> Color(0x18FFFFFF)
}
private fun posBadgeFg(pos: Int, textTertiary: Color) = when (pos) {
    1 -> Color(0xFFFFD700); 2 -> Color(0xFFC0C0C0); 3 -> Color(0xFFCD7F32); else -> textTertiary
}
private fun teamDotColor(name: String): Color {
    val palette = listOf(Color(0xFF1E3A5F), Color(0xFF2D1B4E), Color(0xFF1A3A2A), Color(0xFF3A1A1A), Color(0xFF1A2E3A))
    return palette[abs(name.hashCode()) % palette.size]
}
