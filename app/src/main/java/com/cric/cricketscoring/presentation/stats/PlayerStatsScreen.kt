package com.cric.cricketscoring.presentation.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cric.cricketscoring.domain.model.PlayerCareerStats
import com.cric.cricketscoring.ui.theme.CricketBlue
import com.cric.cricketscoring.ui.theme.CricketPurple
import com.cric.cricketscoring.ui.theme.CricketRed
import com.cric.cricketscoring.ui.theme.EmeraldContainer
import com.cric.cricketscoring.ui.theme.EmeraldDark
import com.cric.cricketscoring.ui.theme.EmeraldPrimary
import com.cric.cricketscoring.ui.theme.GoldPrimary
import com.cric.cricketscoring.ui.theme.LiveRed
import com.cric.cricketscoring.ui.theme.LocalAppColors
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerStatsScreen(
    onBack: () -> Unit,
    viewModel: PlayerStatsViewModel = hiltViewModel()
) {
    val c = LocalAppColors.current
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = c.bg,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Player Performance",
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp,
                            color = c.textPrimary
                        )
                        Text(
                            "Career stats across all matches",
                            fontSize = 10.sp,
                            color = c.textSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = c.textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = c.bg)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(c.bg)
                .padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Overall performance card
            state.overallStats?.let { overall ->
                item(key = "overall_card") {
                    OverallStatsCard(
                        stats = overall,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }

            // Search bar
            item(key = "search_bar") {
                SearchBar(
                    query = state.searchQuery,
                    onQueryChange = viewModel::setSearch,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            // Tab row
            item(key = "tab_row") {
                StatsTabRow(
                    selectedTab = state.selectedTab,
                    onTabSelected = viewModel::setTab,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            // Sort chips
            item(key = "sort_chips") {
                SortChipRow(
                    state = state,
                    onBattingSort = viewModel::setBattingSort,
                    onBowlingSort = viewModel::setBowlingSort,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // Player list
            when {
                state.isLoading -> {
                    item(key = "loading") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = EmeraldPrimary, strokeWidth = 2.dp)
                        }
                    }
                }
                state.displayList.isEmpty() -> {
                    item(key = "empty") {
                        EmptyStats(tab = state.selectedTab)
                    }
                }
                else -> {
                    itemsIndexed(
                        items = state.displayList,
                        key = { _, s -> s.player.name.trim().lowercase() }
                    ) { index, stats ->
                        PlayerStatCard(
                            rank = index + 1,
                            stats = stats,
                            tab = state.selectedTab,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)
                        )
                    }
                    if (state.hasMore) {
                        item(key = "load_more") {
                            LoadMoreItem(onVisible = viewModel::loadMore)
                        }
                    } else {
                        item(key = "end_of_list") {
                            EndOfListItem()
                        }
                    }
                }
            }
        }
    }
}

// ── Overall Stats Card ────────────────────────────────────────────────────────

@Composable
private fun OverallStatsCard(stats: OverallStats, modifier: Modifier = Modifier) {
    val c = LocalAppColors.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF0B2010), Color(0xFF0D1C30), Color(0xFF0A0E14))
                )
            )
            .border(1.dp, c.outline, RoundedCornerShape(18.dp))
    ) {
        // Header row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(listOf(EmeraldPrimary.copy(0.12f), Color.Transparent)),
                    RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    Modifier
                        .size(28.dp)
                        .background(
                            Brush.linearGradient(listOf(EmeraldPrimary, EmeraldDark)),
                            RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.SportsCricket,
                        null,
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    "OVERALL PERFORMANCE",
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    color = c.textPrimary,
                    letterSpacing = 0.8.sp
                )
            }
            Box(
                modifier = Modifier
                    .background(EmeraldContainer, RoundedCornerShape(6.dp))
                    .border(1.dp, EmeraldPrimary.copy(0.35f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    "${stats.totalMatches} Matches",
                    style = MaterialTheme.typography.labelSmall,
                    color = EmeraldPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        HorizontalDivider(color = c.outline.copy(alpha = 0.6f), thickness = 0.5.dp)

        // Primary big stats row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BigStatCell(
                value = formatBigNumber(stats.totalRuns),
                label = "Total Runs",
                color = EmeraldPrimary,
                modifier = Modifier.weight(1f)
            )
            VerticalDividerLine()
            BigStatCell(
                value = stats.totalWickets.toString(),
                label = "Wickets",
                color = LiveRed,
                modifier = Modifier.weight(1f)
            )
            VerticalDividerLine()
            BigStatCell(
                value = stats.highScoreDisplay,
                label = "Highest Score",
                color = GoldPrimary,
                subLabel = stats.highScorePlayer.firstName(),
                modifier = Modifier.weight(1f)
            )
        }

        HorizontalDivider(color = c.outline.copy(alpha = 0.6f), thickness = 0.5.dp)

        // Secondary stats row — Strike Rate · Economy · 4s · 6s
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            SecondaryStatCell(
                label = "Strike Rate",
                value = if (stats.totalBalls == 0) "—" else "%.1f".format(stats.strikeRate),
                color = CricketBlue,
                modifier = Modifier.weight(1f)
            )
            SecondaryStatCell(
                label = "Economy",
                value = if (stats.totalBallsBowled == 0) "—" else "%.1f".format(stats.economy),
                color = CricketPurple,
                modifier = Modifier.weight(1f)
            )
            SecondaryStatCell(
                label = "Fours",
                value = stats.totalFours.toString(),
                color = Color(0xFF00BCD4),
                modifier = Modifier.weight(1f)
            )
            SecondaryStatCell(
                label = "Sixes",
                value = stats.totalSixes.toString(),
                color = GoldPrimary,
                modifier = Modifier.weight(1f)
            )
        }

        HorizontalDivider(color = c.outline.copy(alpha = 0.6f), thickness = 0.5.dp)

        // Leaders row
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LeaderRow(
                icon = "🏏",
                role = "Top Scorer",
                name = stats.topScorerName.ifBlank { "—" },
                statLabel = "runs",
                statValue = stats.topScorerRuns.toString(),
                color = EmeraldPrimary
            )
            LeaderRow(
                icon = "🎯",
                role = "Top Wicket",
                name = stats.topWicketName.ifBlank { "—" },
                statLabel = "wkts",
                statValue = stats.topWickets.toString(),
                color = LiveRed
            )
        }
    }
}

@Composable
private fun BigStatCell(
    value: String,
    label: String,
    color: Color,
    subLabel: String = "",
    modifier: Modifier = Modifier
) {
    val c = LocalAppColors.current
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            value,
            fontWeight = FontWeight.Black,
            fontSize = 26.sp,
            color = color,
            textAlign = TextAlign.Center
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = c.textSecondary,
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
            letterSpacing = 0.3.sp
        )
        if (subLabel.isNotBlank()) {
            Text(
                subLabel,
                style = MaterialTheme.typography.labelSmall,
                color = color.copy(alpha = 0.7f),
                fontSize = 9.sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SecondaryStatCell(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val c = LocalAppColors.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.08f))
            .border(1.dp, color.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            value,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 16.sp,
            color = color
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = c.textSecondary,
            fontSize = 9.sp,
            letterSpacing = 0.2.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun LeaderRow(
    icon: String,
    role: String,
    name: String,
    statLabel: String,
    statValue: String,
    color: Color
) {
    val c = LocalAppColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(icon, fontSize = 16.sp)
        Text(
            role,
            style = MaterialTheme.typography.labelSmall,
            color = c.textSecondary,
            modifier = Modifier.width(70.dp),
            fontSize = 11.sp
        )
        Text(
            name,
            style = MaterialTheme.typography.labelMedium,
            color = c.textPrimary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontSize = 12.sp
        )
        Box(
            modifier = Modifier
                .background(color.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(
                "$statValue $statLabel",
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun VerticalDividerLine() {
    val c = LocalAppColors.current
    Box(
        modifier = Modifier
            .width(0.5.dp)
            .height(48.dp)
            .background(c.outline)
    )
}

private fun formatBigNumber(n: Int): String = when {
    n >= 1_000 -> "%.1fK".format(n / 1000.0).trimEnd('0').trimEnd('.')
    else -> n.toString()
}

private fun String.firstName(): String = split(" ").firstOrNull() ?: this

// ── Search Bar ────────────────────────────────────────────────────────────────

@Composable
private fun SearchBar(query: String, onQueryChange: (String) -> Unit, modifier: Modifier = Modifier) {
    val c = LocalAppColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(c.surface2)
            .border(1.dp, c.outline, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(Icons.Default.Search, null, tint = c.textSecondary, modifier = Modifier.size(18.dp))
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = c.textPrimary),
            cursorBrush = SolidColor(EmeraldPrimary),
            singleLine = true,
            decorationBox = { inner ->
                if (query.isEmpty()) {
                    Text("Search player…", color = c.textTertiary, style = MaterialTheme.typography.bodyMedium)
                }
                inner()
            }
        )
    }
}

// ── Tab Row ───────────────────────────────────────────────────────────────────

@Composable
private fun StatsTabRow(
    selectedTab: StatsTab,
    onTabSelected: (StatsTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val c = LocalAppColors.current
    val tabs = listOf(
        StatsTab.BATTING   to "Batting",
        StatsTab.BOWLING   to "Bowling",
        StatsTab.ALL_ROUND to "All-Round"
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
        tabs.forEach { (tab, label) ->
            val isSelected = selectedTab == tab
            val accent = when (tab) {
                StatsTab.BATTING   -> EmeraldPrimary
                StatsTab.BOWLING   -> CricketBlue
                StatsTab.ALL_ROUND -> GoldPrimary
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (isSelected) accent.copy(alpha = 0.14f) else Color.Transparent)
                    .border(
                        width = if (isSelected) 1.dp else 0.dp,
                        color = if (isSelected) accent.copy(alpha = 0.4f) else Color.Transparent,
                        shape = RoundedCornerShape(9.dp)
                    )
                    .clickable { onTabSelected(tab) }
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) accent else c.textSecondary
                )
            }
        }
    }
}

// ── Sort Chips ────────────────────────────────────────────────────────────────

@Composable
private fun SortChipRow(
    state: PlayerStatsUiState,
    onBattingSort: (BattingSort) -> Unit,
    onBowlingSort: (BowlingSort) -> Unit,
    modifier: Modifier = Modifier
) {
    val c = LocalAppColors.current
    val scrollState = rememberScrollState()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Sort by",
            style = MaterialTheme.typography.labelSmall,
            color = c.textTertiary,
            modifier = Modifier.padding(end = 2.dp)
        )
        when (state.selectedTab) {
            StatsTab.BATTING -> BattingSort.entries.forEach { sort ->
                SortChip(
                    label = sort.label,
                    selected = state.battingSort == sort,
                    onClick = { onBattingSort(sort) }
                )
            }
            else -> BowlingSort.entries.forEach { sort ->
                SortChip(
                    label = sort.label,
                    selected = state.bowlingSort == sort,
                    onClick = { onBowlingSort(sort) }
                )
            }
        }
    }
}

@Composable
private fun SortChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val c = LocalAppColors.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) EmeraldPrimary.copy(alpha = 0.18f) else c.surface2)
            .border(
                1.dp,
                if (selected) EmeraldPrimary.copy(alpha = 0.5f) else c.outline,
                RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) EmeraldPrimary else c.textSecondary,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

// ── Player Stat Card ──────────────────────────────────────────────────────────

@Composable
private fun PlayerStatCard(rank: Int, stats: PlayerCareerStats, tab: StatsTab, modifier: Modifier = Modifier) {
    val c = LocalAppColors.current
    val rankColor = when (rank) {
        1 -> GoldPrimary
        2 -> Color(0xFFC0C0C0)
        3 -> Color(0xFFCD7F32)
        else -> c.outline
    }
    val accentColor = when (tab) {
        StatsTab.BATTING   -> EmeraldPrimary
        StatsTab.BOWLING   -> CricketBlue
        StatsTab.ALL_ROUND -> GoldPrimary
    }
    val tabLabel = when (tab) {
        StatsTab.BATTING   -> "BAT"
        StatsTab.BOWLING   -> "BWL"
        StatsTab.ALL_ROUND -> "ALL"
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(c.surface)
            .border(
                width = if (rank <= 3) 1.5.dp else 1.dp,
                color = if (rank <= 3) rankColor.copy(alpha = 0.55f) else c.outline,
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        // ── Header ────────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(accentColor.copy(alpha = 0.10f), Color.Transparent)
                    ),
                    RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                )
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Rank
            if (rank <= 3) {
                Icon(Icons.Default.EmojiEvents, null, tint = rankColor, modifier = Modifier.size(22.dp))
            } else {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(c.surface3, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "#$rank",
                        fontSize = 9.sp,
                        color = c.textSecondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Avatar
            PlayerAvatar(name = stats.player.name, size = 44)

            // Name + matches
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stats.player.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = c.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(Modifier.size(5.dp).background(accentColor, CircleShape))
                    Text(
                        "${stats.matchesPlayed} ${if (stats.matchesPlayed == 1) "match" else "matches"}",
                        fontSize = 11.sp,
                        color = c.textSecondary
                    )
                }
            }

            // Tab badge
            Box(
                modifier = Modifier
                    .background(accentColor.copy(alpha = 0.14f), RoundedCornerShape(7.dp))
                    .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(7.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    tabLabel,
                    fontSize = 10.sp,
                    color = accentColor,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.8.sp
                )
            }
        }

        HorizontalDivider(color = c.outline.copy(alpha = 0.5f), thickness = 0.5.dp)

        // ── Primary Stats ─────────────────────────────────────────────────────
        when (tab) {
            StatsTab.BATTING -> BattingPrimaryStats(stats)
            StatsTab.BOWLING -> BowlingPrimaryStats(stats)
            StatsTab.ALL_ROUND -> AllRoundStats(stats)
        }

        // ── Secondary Chips ───────────────────────────────────────────────────
        if (tab != StatsTab.ALL_ROUND) {
            HorizontalDivider(color = c.outline.copy(alpha = 0.5f), thickness = 0.5.dp)
            when (tab) {
                StatsTab.BATTING -> BattingChips(stats)
                StatsTab.BOWLING -> BowlingChips(stats)
                else -> Unit
            }
        }
    }
}

// ── Primary stat blocks ───────────────────────────────────────────────────────

@Composable
private fun BattingPrimaryStats(stats: PlayerCareerStats) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        MainStatCell(value = stats.totalRuns.toString(), label = "RUNS",   color = EmeraldPrimary, modifier = Modifier.weight(1f))
        StatDivider()
        MainStatCell(value = stats.highScoreDisplay,      label = "HS",     color = GoldPrimary,    modifier = Modifier.weight(1f))
        StatDivider()
        MainStatCell(
            value = if (stats.battingInnings == stats.notOuts) "∞"
                    else "%.1f".format(stats.battingAverage),
            label = "AVG", color = CricketBlue, modifier = Modifier.weight(1f)
        )
        StatDivider()
        MainStatCell(
            value = if (stats.totalBalls == 0) "—" else "%.1f".format(stats.strikeRate),
            label = "SR", color = CricketPurple, modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun BowlingPrimaryStats(stats: PlayerCareerStats) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        MainStatCell(value = stats.wickets.toString(),    label = "WICKETS", color = LiveRed,      modifier = Modifier.weight(1f))
        StatDivider()
        MainStatCell(value = stats.bestBowlingDisplay,    label = "BEST",    color = GoldPrimary,  modifier = Modifier.weight(1f))
        StatDivider()
        MainStatCell(
            value = if (stats.ballsBowled == 0) "—" else "%.1f".format(stats.economy),
            label = "ECON", color = CricketBlue, modifier = Modifier.weight(1f)
        )
        StatDivider()
        MainStatCell(
            value = if (stats.wickets == 0) "—" else "%.1f".format(stats.bowlingAverage),
            label = "AVG", color = EmeraldPrimary, modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun AllRoundStats(stats: PlayerCareerStats) {
    val c = LocalAppColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Batting row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(EmeraldPrimary.copy(alpha = 0.07f))
                .border(1.dp, EmeraldPrimary.copy(alpha = 0.18f), RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Text("🏏", fontSize = 15.sp, modifier = Modifier.padding(end = 8.dp))
            Text(stats.totalRuns.toString(), fontWeight = FontWeight.Black, fontSize = 20.sp, color = EmeraldPrimary)
            Text(" runs", fontSize = 10.sp, color = c.textSecondary, modifier = Modifier.weight(1f).padding(top = 4.dp))
            AllRoundChip("HS", stats.highScoreDisplay,       EmeraldPrimary)
            Spacer(Modifier.width(6.dp))
            AllRoundChip("SR", if (stats.totalBalls == 0) "—" else "%.0f".format(stats.strikeRate), CricketBlue)
            Spacer(Modifier.width(6.dp))
            AllRoundChip("Avg", if (stats.battingInnings == stats.notOuts) "∞"
                               else "%.1f".format(stats.battingAverage),   GoldPrimary)
        }
        // Bowling row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(LiveRed.copy(alpha = 0.07f))
                .border(1.dp, LiveRed.copy(alpha = 0.18f), RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Text("🎯", fontSize = 15.sp, modifier = Modifier.padding(end = 8.dp))
            Text(stats.wickets.toString(), fontWeight = FontWeight.Black, fontSize = 20.sp, color = LiveRed)
            Text(" wkts", fontSize = 10.sp, color = c.textSecondary, modifier = Modifier.weight(1f).padding(top = 4.dp))
            AllRoundChip("Best", stats.bestBowlingDisplay, LiveRed)
            Spacer(Modifier.width(6.dp))
            AllRoundChip("Eco", if (stats.ballsBowled == 0) "—" else "%.1f".format(stats.economy), CricketBlue)
            Spacer(Modifier.width(6.dp))
            AllRoundChip("Avg", if (stats.wickets == 0) "—" else "%.1f".format(stats.bowlingAverage), GoldPrimary)
        }
    }
}

@Composable
private fun AllRoundChip(label: String, value: String, color: Color) {
    val c = LocalAppColors.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = color)
        Text(label, fontSize = 9.sp, color = c.textTertiary)
    }
}

// ── Secondary chips ───────────────────────────────────────────────────────────

@Composable
private fun BattingChips(stats: PlayerCareerStats) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            SmallStatChip("Innings", stats.battingInnings.toString(), Color(0xFF00BCD4), Modifier.weight(1f))
            SmallStatChip("Fours",   stats.fours.toString(),          EmeraldPrimary,   Modifier.weight(1f))
            SmallStatChip("Sixes",   stats.sixes.toString(),          GoldPrimary,      Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            SmallStatChip("50s",   stats.fifties.toString(),  CricketBlue,   Modifier.weight(1f))
            SmallStatChip("100s",  stats.hundreds.toString(), LiveRed,       Modifier.weight(1f))
            SmallStatChip("Ducks", stats.ducks.toString(),    CricketPurple, Modifier.weight(1f))
        }
    }
}

@Composable
private fun BowlingChips(stats: PlayerCareerStats) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            SmallStatChip("Overs",   stats.oversBowled,           Color(0xFF00BCD4), Modifier.weight(1f))
            SmallStatChip("Runs",    stats.runsConceded.toString(), CricketBlue,     Modifier.weight(1f))
            SmallStatChip("Maidens", stats.maidens.toString(),     EmeraldPrimary,   Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            SmallStatChip("4-Wkt",  stats.fourWicketHauls.toString(), GoldPrimary,   Modifier.weight(1f))
            SmallStatChip("5-Wkt",  stats.fiveWicketHauls.toString(), LiveRed,       Modifier.weight(1f))
            SmallStatChip("BowlSR", if (stats.wickets == 0) "—" else "%.1f".format(stats.bowlingStrikeRate),
                          CricketPurple, Modifier.weight(1f))
        }
    }
}

@Composable
private fun SmallStatChip(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    val c = LocalAppColors.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(9.dp))
            .background(color.copy(alpha = 0.08f))
            .border(1.dp, color.copy(alpha = 0.22f), RoundedCornerShape(9.dp))
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = color)
        Text(label, fontSize = 9.sp, color = c.textSecondary, letterSpacing = 0.2.sp)
    }
}

@Composable
private fun MainStatCell(value: String, label: String, color: Color, modifier: Modifier = Modifier) {
    val c = LocalAppColors.current
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            value,
            fontWeight = FontWeight.Black,
            fontSize = 24.sp,
            color = color,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = c.textSecondary,
            fontSize = 10.sp,
            letterSpacing = 0.5.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun StatDivider() {
    val c = LocalAppColors.current
    Box(
        modifier = Modifier
            .width(0.5.dp)
            .height(44.dp)
            .background(c.outline)
    )
}

// ── Player Avatar ─────────────────────────────────────────────────────────────

@Composable
private fun PlayerAvatar(name: String, size: Int) {
    val initials = name
        .split(" ", "-")
        .filter { it.isNotBlank() }
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")
        .ifBlank { name.take(2).uppercase() }

    val bgColors = listOf(
        Color(0xFF1E3A5F), Color(0xFF2D1B4E), Color(0xFF1A3A2A),
        Color(0xFF3A1A1A), Color(0xFF1A2E3A), Color(0xFF2A2A14)
    )
    val fgColors = listOf(
        CricketBlue, Color(0xFFAA00FF), EmeraldPrimary,
        CricketRed, Color(0xFF00BCD4), GoldPrimary
    )
    val idx = abs(name.hashCode()) % bgColors.size

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

// ── Pagination sentinels ──────────────────────────────────────────────────────

@Composable
private fun LoadMoreItem(onVisible: () -> Unit) {
    val c = LocalAppColors.current
    LaunchedEffect(Unit) { onVisible() }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = EmeraldPrimary,
                strokeWidth = 2.dp
            )
            Text("Loading more players…", fontSize = 12.sp, color = c.textSecondary)
        }
    }
}

@Composable
private fun EndOfListItem() {
    val c = LocalAppColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            Modifier
                .width(48.dp)
                .height(0.5.dp)
                .background(c.outline)
        )
        Text(
            "  All players loaded  ",
            fontSize = 11.sp,
            color = c.textTertiary
        )
        Box(
            Modifier
                .width(48.dp)
                .height(0.5.dp)
                .background(c.outline)
        )
    }
}

// ── Empty state ───────────────────────────────────────────────────────────────

@Composable
private fun EmptyStats(tab: StatsTab) {
    val c = LocalAppColors.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(EmeraldContainer, CircleShape)
                    .border(2.dp, EmeraldPrimary.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.SportsCricket,
                    null,
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(40.dp)
                )
            }
            Text(
                "No stats available",
                style = MaterialTheme.typography.titleMedium,
                color = c.textPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                when (tab) {
                    StatsTab.ALL_ROUND -> "No all-rounders found across your matches."
                    StatsTab.BOWLING -> "No bowling data recorded yet."
                    StatsTab.BATTING -> "No batting data recorded yet."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = c.textSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}
