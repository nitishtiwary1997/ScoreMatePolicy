package com.nitish.cricketscoringapp.presentation.home

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.nitish.cricketscoringapp.domain.model.Match
import com.nitish.cricketscoringapp.domain.model.MatchStatus
import com.nitish.cricketscoringapp.ui.theme.*
import java.util.Calendar
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNewMatch: () -> Unit,
    onMatchClick: (matchId: String, status: String) -> Unit,
    onSignOut: () -> Unit,
    onPlayerStats: () -> Unit = {},
    onTournaments: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val c = LocalAppColors.current
    val state by viewModel.uiState.collectAsState()
    var showSignOutDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }

    val liveMatches = remember(state.matches) {
        state.matches.filter { it.status == MatchStatus.INNINGS_1 || it.status == MatchStatus.INNINGS_2 }
    }
    val tossMatches = remember(state.matches) {
        state.matches.filter { it.status == MatchStatus.TOSS }
    }
    val completedMatches = remember(state.matches) {
        state.matches.filter { it.status == MatchStatus.COMPLETED }
    }
    val filteredMatches = remember(state.matches, selectedTab) {
        when (selectedTab) {
            1 -> liveMatches + tossMatches
            2 -> completedMatches
            else -> state.matches.sortedByDescending { it.createdAt }
        }
    }

    Scaffold(
        containerColor = c.bg,
        topBar = {
            DashboardTopBar(
                userName = state.userName,
                userEmail = state.userEmail,
                isGuest = state.isGuest,
                onSignOut = { showSignOutDialog = true },
                onPlayerStats = onPlayerStats
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNewMatch,
                containerColor = EmeraldPrimary,
                contentColor = Color.Black,
                shape = RoundedCornerShape(16.dp),
                icon = { Icon(Icons.Default.Add, null, modifier = Modifier.size(20.dp)) },
                text = { Text("New Match", fontWeight = FontWeight.Bold, fontSize = 14.sp) }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(c.bg)
                .padding(padding),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item {
                GreetingBanner(
                    userName = if (state.isGuest) "Guest" else state.userName.ifBlank {
                        state.userEmail.substringBefore('@').replaceFirstChar { it.uppercase() }
                    },
                    isGuest = state.isGuest,
                    onSignIn = onSignOut
                )
            }

            item {
                StatsRow(
                    total = state.matches.size,
                    live = liveMatches.size,
                    completed = completedMatches.size,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            item {
                TournamentEntryCard(
                    onClick = onTournaments,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            if (liveMatches.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "LIVE NOW",
                        count = liveMatches.size,
                        accentColor = LiveRed,
                        showLiveDot = true,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 10.dp)
                    )
                }
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(liveMatches, key = { it.id }) { match ->
                            LiveMatchCard(
                                match = match,
                                onClick = { onMatchClick(match.id, match.status.name) }
                            )
                        }
                    }
                }
            }

            item {
                SectionHeader(
                    title = "MY MATCHES",
                    count = state.matches.size,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 0.dp)
                )
            }

            if (state.matches.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(12.dp))
                    MatchFilterTabs(
                        selectedTab = selectedTab,
                        liveCount = liveMatches.size + tossMatches.size,
                        completedCount = completedMatches.size,
                        onTabSelected = { selectedTab = it },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                }

                if (filteredMatches.isEmpty()) {
                    item {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No matches in this category",
                                style = MaterialTheme.typography.bodyMedium,
                                color = c.textSecondary
                            )
                        }
                    }
                } else {
                    items(filteredMatches, key = { it.id }) { match ->
                        CricbuzzMatchCard(
                            match = match,
                            onClick = { onMatchClick(match.id, match.status.name) },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)
                        )
                    }
                }
            } else {
                item { EmptyState() }
            }
        }

        if (showSignOutDialog) {
            SignOutDialog(
                userName = state.userName.ifBlank { state.userEmail },
                isGuest = state.isGuest,
                onConfirm = {
                    showSignOutDialog = false
                    if (!state.isGuest) viewModel.signOut()
                    onSignOut()
                },
                onDismiss = { showSignOutDialog = false }
            )
        }
    }
}

// ── Top App Bar ───────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardTopBar(
    userName: String,
    userEmail: String,
    isGuest: Boolean,
    onSignOut: () -> Unit,
    onPlayerStats: () -> Unit = {}
) {
    val c = LocalAppColors.current
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            brush = Brush.linearGradient(listOf(EmeraldPrimary, EmeraldDark)),
                            shape = RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.SportsCricket,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        "Cricket Scorer",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = c.textPrimary
                    )
                    Text(
                        if (isGuest) "Guest mode" else "Live Match Tracker",
                        fontSize = 10.sp,
                        color = if (isGuest) GoldPrimary else EmeraldPrimary,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        },
        actions = {
            IconButton(onClick = onPlayerStats) {
                Icon(
                    Icons.Default.BarChart,
                    contentDescription = "Player Stats",
                    tint = EmeraldPrimary
                )
            }
            if (isGuest) {
                // Guest: show a "G" avatar + login icon
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(GoldContainer, CircleShape)
                        .border(1.5.dp, GoldPrimary.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("G", color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Spacer(Modifier.width(4.dp))
                IconButton(onClick = onSignOut) {
                    Icon(Icons.Default.Login, "Sign In", tint = EmeraldPrimary)
                }
            } else {
                val initial = (userName.firstOrNull() ?: userEmail.firstOrNull() ?: 'U').uppercaseChar()
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(EmeraldContainer, CircleShape)
                        .border(1.5.dp, EmeraldPrimary.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        initial.toString(),
                        color = EmeraldPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
                Spacer(Modifier.width(4.dp))
                IconButton(onClick = onSignOut) {
                    Icon(Icons.Default.Logout, "Sign Out", tint = c.textSecondary)
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = c.bg,
            titleContentColor = c.textPrimary
        )
    )
}

// ── Greeting Banner ───────────────────────────────────────────────────────────

@Composable
private fun GreetingBanner(userName: String, isGuest: Boolean = false, onSignIn: () -> Unit = {}) {
    val c = LocalAppColors.current
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when {
            hour < 5  -> "Good Night"
            hour < 12 -> "Good Morning"
            hour < 17 -> "Good Afternoon"
            hour < 21 -> "Good Evening"
            else      -> "Good Night"
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF0D2818), Color(0xFF101E30), c.bg)
                )
            )
            .border(1.dp, c.outline, RoundedCornerShape(18.dp))
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "$greeting,",
                    style = MaterialTheme.typography.bodyMedium,
                    color = c.textSecondary
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    userName.ifBlank { "Cricketer" },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = c.textPrimary
                )
                Spacer(Modifier.height(8.dp))
                if (isGuest) {
                    TextButton(
                        onClick = onSignIn,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            "Sign in to sync your data →",
                            style = MaterialTheme.typography.bodySmall,
                            color = GoldPrimary
                        )
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            Modifier
                                .size(6.dp)
                                .background(EmeraldPrimary, CircleShape)
                        )
                        Text(
                            "Ready to score today?",
                            style = MaterialTheme.typography.bodySmall,
                            color = EmeraldPrimary
                        )
                    }
                }
            }
            Icon(
                Icons.Default.SportsCricket,
                contentDescription = null,
                tint = if (isGuest) GoldPrimary.copy(alpha = 0.18f) else EmeraldPrimary.copy(alpha = 0.18f),
                modifier = Modifier.size(72.dp)
            )
        }
    }
}

// ── Stats Row ─────────────────────────────────────────────────────────────────

@Composable
private fun StatsRow(
    total: Int,
    live: Int,
    completed: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatChip(label = "Total", value = "$total", color = CricketBlue, modifier = Modifier.weight(1f))
        StatChip(label = "Live", value = "$live", color = LiveRed, modifier = Modifier.weight(1f))
        StatChip(label = "Done", value = "$completed", color = DoneGreen, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun StatChip(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    val c = LocalAppColors.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(c.surface2)
            .border(1.dp, c.outline, RoundedCornerShape(14.dp))
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                value,
                fontWeight = FontWeight.Black,
                fontSize = 24.sp,
                color = color
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = c.textSecondary,
                letterSpacing = 0.5.sp
            )
        }
    }
}

// ── Section Header ────────────────────────────────────────────────────────────

@Composable
private fun SectionHeader(
    title: String,
    count: Int,
    modifier: Modifier = Modifier,
    accentColor: Color = EmeraldPrimary,
    showLiveDot: Boolean = false
) {
    val c = LocalAppColors.current
    val infiniteTransition = rememberInfiniteTransition(label = "section_pulse")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_alpha"
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (showLiveDot) {
                Box(
                    Modifier
                        .size(8.dp)
                        .background(LiveRed.copy(alpha = dotAlpha), CircleShape)
                )
            } else {
                Box(
                    Modifier
                        .width(3.dp)
                        .height(16.dp)
                        .background(accentColor, RoundedCornerShape(2.dp))
                )
            }
            Text(
                title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Black,
                color = c.textPrimary,
                letterSpacing = 1.sp
            )
        }
        if (count > 0) {
            Box(
                modifier = Modifier
                    .background(accentColor.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                    .border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    count.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = accentColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ── Match Filter Tabs ─────────────────────────────────────────────────────────

@Composable
private fun MatchFilterTabs(
    selectedTab: Int,
    liveCount: Int,
    completedCount: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val c = LocalAppColors.current
    val tabs = listOf(
        "All",
        if (liveCount > 0) "Active ($liveCount)" else "Active",
        "Completed"
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(c.surface)
            .border(1.dp, c.outline, RoundedCornerShape(12.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        tabs.forEachIndexed { index, label ->
            val isSelected = selectedTab == index
            val accentColor = when (index) {
                1 -> LiveRed
                2 -> DoneGreen
                else -> EmeraldPrimary
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(9.dp))
                    .background(
                        if (isSelected) accentColor.copy(alpha = 0.14f) else Color.Transparent
                    )
                    .border(
                        width = if (isSelected) 1.dp else 0.dp,
                        color = if (isSelected) accentColor.copy(alpha = 0.35f) else Color.Transparent,
                        shape = RoundedCornerShape(9.dp)
                    )
                    .clickable { onTabSelected(index) }
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) accentColor else c.textSecondary,
                    maxLines = 1
                )
            }
        }
    }
}

// ── Live Match Card (horizontal scroll section) ───────────────────────────────

@Composable
private fun LiveMatchCard(match: Match, onClick: () -> Unit) {
    val c = LocalAppColors.current
    OutlinedCard(
        modifier = Modifier
            .width(260.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.outlinedCardColors(containerColor = c.surface2),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, LiveRed.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LiveBadge()
                Text(
                    "${match.totalOvers} Ov · T${match.playersPerTeam}",
                    style = MaterialTheme.typography.labelSmall,
                    color = c.textSecondary,
                    fontSize = 10.sp
                )
            }
            Spacer(Modifier.height(14.dp))
            TeamRow(teamName = match.team1Name)
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(Modifier.width(38.dp))
                Box(Modifier.weight(1f).height(0.5.dp).background(c.divider))
                Text(
                    "  vs  ",
                    style = MaterialTheme.typography.labelSmall,
                    color = c.textTertiary,
                    fontSize = 10.sp
                )
                Box(Modifier.weight(1f).height(0.5.dp).background(c.divider))
            }
            Spacer(Modifier.height(6.dp))
            TeamRow(teamName = match.team2Name)
            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = c.divider, thickness = 0.5.dp)
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    inningsLabel(match),
                    style = MaterialTheme.typography.labelSmall,
                    color = c.textSecondary,
                    fontSize = 10.sp,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        "Score",
                        style = MaterialTheme.typography.labelSmall,
                        color = LiveRed,
                        fontWeight = FontWeight.SemiBold
                    )
                    Icon(
                        Icons.Default.ChevronRight,
                        null,
                        tint = LiveRed,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

// ── Cricbuzz Match Card (vertical list) ───────────────────────────────────────

@Composable
private fun CricbuzzMatchCard(match: Match, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = LocalAppColors.current
    val (accentColor, statusLabel, bgContainer) = when (match.status) {
        MatchStatus.INNINGS_1, MatchStatus.INNINGS_2 -> Triple(LiveRed, "LIVE", CricketRedDim)
        MatchStatus.TOSS     -> Triple(GoldPrimary, "TOSS", GoldContainer)
        MatchStatus.COMPLETED -> Triple(DoneGreen, "DONE", EmeraldContainer)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(c.surface)
            .border(1.dp, c.outline, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            // Left accent stripe
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(accentColor, accentColor.copy(alpha = 0.25f))
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 14.dp, top = 14.dp, end = 14.dp, bottom = 12.dp)
            ) {
                // Header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatusPill(label = statusLabel, color = accentColor, bgColor = bgContainer)
                        Box(
                            modifier = Modifier
                                .background(c.surface3, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "${match.totalOvers}-over · ${match.playersPerTeam}a-side",
                                style = MaterialTheme.typography.labelSmall,
                                color = c.textSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }
                    Text(
                        formatDate(match.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = c.textTertiary,
                        fontSize = 10.sp
                    )
                }

                Spacer(Modifier.height(14.dp))

                // Teams
                TeamRow(teamName = match.team1Name)
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(Modifier.width(38.dp))
                    Box(Modifier.weight(1f).height(0.5.dp).background(c.divider))
                    Text(
                        "  vs  ",
                        style = MaterialTheme.typography.labelSmall,
                        color = c.textTertiary,
                        fontSize = 10.sp
                    )
                    Box(Modifier.weight(1f).height(0.5.dp).background(c.divider))
                }
                Spacer(Modifier.height(6.dp))
                TeamRow(teamName = match.team2Name)

                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = c.divider, thickness = 0.5.dp)
                Spacer(Modifier.height(8.dp))

                // Footer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        matchFooterText(match),
                        style = MaterialTheme.typography.labelSmall,
                        color = c.textSecondary,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 11.sp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            actionText(match.status),
                            style = MaterialTheme.typography.labelSmall,
                            color = accentColor,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        )
                        Icon(
                            Icons.Default.ChevronRight,
                            null,
                            tint = accentColor,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

// ── Team Row ──────────────────────────────────────────────────────────────────

@Composable
private fun TeamRow(teamName: String) {
    val c = LocalAppColors.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        TeamAvatar(teamName = teamName, size = 28)
        Text(
            teamName,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = c.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

// ── Team Avatar ───────────────────────────────────────────────────────────────

@Composable
private fun TeamAvatar(teamName: String, size: Int) {
    val initials = teamName
        .split(" ", "-", "_")
        .filter { it.isNotBlank() }
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")
        .ifBlank { teamName.take(2).uppercase() }

    val bgColors = listOf(
        Color(0xFF1E3A5F), Color(0xFF2D1B4E), Color(0xFF1A3A2A),
        Color(0xFF3A1A1A), Color(0xFF1A2E3A), Color(0xFF2A2A14)
    )
    val fgColors = listOf(
        CricketBlue, Color(0xFFAA00FF), EmeraldPrimary,
        CricketRed, Color(0xFF00BCD4), GoldPrimary
    )
    val idx = abs(teamName.hashCode()) % bgColors.size

    Box(
        modifier = Modifier
            .size(size.dp)
            .background(bgColors[idx], RoundedCornerShape((size / 4).dp))
            .border(1.dp, fgColors[idx].copy(alpha = 0.5f), RoundedCornerShape((size / 4).dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            initials.take(2),
            fontSize = (size / 3.2).sp,
            fontWeight = FontWeight.ExtraBold,
            color = fgColors[idx]
        )
    }
}

// ── Live Badge ────────────────────────────────────────────────────────────────

@Composable
private fun LiveBadge() {
    val infiniteTransition = rememberInfiniteTransition(label = "live_badge")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(tween(750), RepeatMode.Reverse),
        label = "badge_alpha"
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .background(CricketRedDim, RoundedCornerShape(6.dp))
            .border(1.dp, LiveRed.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
            .padding(horizontal = 7.dp, vertical = 4.dp)
    ) {
        Box(
            Modifier
                .size(5.dp)
                .background(LiveRed.copy(alpha = alpha), CircleShape)
        )
        Text(
            "LIVE",
            style = MaterialTheme.typography.labelSmall,
            color = LiveRed,
            fontWeight = FontWeight.Black,
            fontSize = 10.sp,
            letterSpacing = 0.5.sp
        )
    }
}

// ── Status Pill ───────────────────────────────────────────────────────────────

@Composable
private fun StatusPill(label: String, color: Color, bgColor: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(6.dp))
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        if (label == "LIVE") {
            Box(Modifier.size(5.dp).background(LiveRed, CircleShape))
        }
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            letterSpacing = 0.8.sp
        )
    }
}

// ── Empty State ───────────────────────────────────────────────────────────────

@Composable
private fun EmptyState() {
    val c = LocalAppColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .background(EmeraldContainer, CircleShape)
                .border(2.dp, EmeraldPrimary.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.SportsCricket,
                contentDescription = null,
                tint = EmeraldPrimary,
                modifier = Modifier.size(48.dp)
            )
        }
        Spacer(Modifier.height(20.dp))
        Text(
            "No matches yet",
            style = MaterialTheme.typography.titleMedium,
            color = c.textPrimary,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Tap + New Match to start scoring\nyour first cricket match.",
            style = MaterialTheme.typography.bodyMedium,
            color = c.textSecondary,
            textAlign = TextAlign.Center
        )
    }
}

// ── Sign Out Dialog ───────────────────────────────────────────────────────────

@Composable
private fun SignOutDialog(
    userName: String,
    isGuest: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val c = LocalAppColors.current
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = c.surface,
        titleContentColor = c.textPrimary,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    if (isGuest) Icons.Default.Login else Icons.Default.Logout,
                    null,
                    tint = if (isGuest) EmeraldPrimary else CricketRed,
                    modifier = Modifier.size(20.dp)
                )
                Text(if (isGuest) "Sign In" else "Sign Out", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (isGuest) {
                    Text(
                        "You're using the app as a guest. Sign in with Google to sync your matches across devices and keep your data safe.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = c.textSecondary
                    )
                } else {
                    if (userName.isNotBlank()) {
                        Text("Signed in as", style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
                        Text(
                            userName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = c.textPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(4.dp))
                    }
                    Text(
                        "Are you sure you want to sign out?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = c.textSecondary
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isGuest) EmeraldPrimary else CricketRed,
                    contentColor = Color.White
                )
            ) {
                Text(if (isGuest) "Sign In" else "Sign Out", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = c.textSecondary)
            }
        }
    )
}

// ── Tournament Entry Card ─────────────────────────────────────────────────────

@Composable
private fun TournamentEntryCard(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = LocalAppColors.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF2A1A00), Color(0xFF1C1A0A), c.bg)
                )
            )
            .border(1.dp, GoldPrimary.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            Brush.linearGradient(listOf(GoldPrimary, Color(0xFFE65100))),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column {
                    Text(
                        "Tournaments",
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = GoldPrimary
                    )
                    Text(
                        "Manage league & knockout tournaments",
                        style = MaterialTheme.typography.bodySmall,
                        color = c.textSecondary,
                        fontSize = 11.sp
                    )
                }
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = GoldPrimary.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun inningsLabel(match: Match): String = when (match.status) {
    MatchStatus.INNINGS_1 -> "${match.battingTeamName(1)} batting · Inn 1"
    MatchStatus.INNINGS_2 -> "${match.battingTeamName(2)} batting · Inn 2"
    else -> ""
}

private fun matchFooterText(match: Match): String = when (match.status) {
    MatchStatus.TOSS      -> "Toss to be decided · ${match.playersPerTeam} players/side"
    MatchStatus.INNINGS_1 -> "${match.battingTeamName(1)} batting in 1st innings"
    MatchStatus.INNINGS_2 -> "${match.battingTeamName(2)} batting in 2nd innings"
    MatchStatus.COMPLETED -> "Match completed · ${match.playersPerTeam} players/side"
}

private fun actionText(status: MatchStatus): String = when (status) {
    MatchStatus.TOSS                             -> "Toss"
    MatchStatus.INNINGS_1, MatchStatus.INNINGS_2 -> "Score"
    MatchStatus.COMPLETED                        -> "Summary"
}

private fun formatDate(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    return when {
        diff < 60_000L        -> "Just now"
        diff < 3_600_000L     -> "${diff / 60_000}m ago"
        diff < 86_400_000L    -> "${diff / 3_600_000}h ago"
        diff < 604_800_000L   -> "${diff / 86_400_000}d ago"
        else -> {
            val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
            val months = arrayOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
            "${cal.get(Calendar.DAY_OF_MONTH)} ${months[cal.get(Calendar.MONTH)]}"
        }
    }
}
