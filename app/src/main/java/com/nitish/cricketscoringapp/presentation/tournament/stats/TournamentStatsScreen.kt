package com.nitish.cricketscoringapp.presentation.tournament.stats

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.nitish.cricketscoringapp.domain.model.PlayerStatLine

private val DarkBg        = Color(0xFF0A0E14)
private val DarkSurface   = Color(0xFF131920)
private val DarkSurface2  = Color(0xFF1A2230)
private val EmeraldPrimary = Color(0xFF00C853)
private val GoldPrimary   = Color(0xFFFFD740)
private val OrangeCap     = Color(0xFFFF6D00)
private val PurpleCap     = Color(0xFF9C27B0)
private val TextPrimary   = Color(0xFFF0F4FF)
private val TextSecondary = Color(0xFF8A9BB5)
private val OutlineColor  = Color(0xFF2A3A52)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentStatsScreen(
    onBack: () -> Unit,
    viewModel: TournamentStatsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = { Text("Tournament Statistics", color = TextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = TextSecondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = EmeraldPrimary)
                }
            }
            uiState.error != null -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text(uiState.error ?: "Error", color = TextSecondary, textAlign = TextAlign.Center)
                }
            }
            uiState.stats != null -> {
                val stats = uiState.stats!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Orange Cap + Purple Cap heroes
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CapHeroCard(
                            label = "Orange Cap",
                            subtitle = "Top Run Scorer",
                            capColor = OrangeCap,
                            player = stats.orangeCap,
                            modifier = Modifier.weight(1f)
                        )
                        CapHeroCard(
                            label = "Purple Cap",
                            subtitle = "Top Wicket Taker",
                            capColor = PurpleCap,
                            player = stats.purpleCap,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Leaderboard sections
                    if (stats.mostRuns.isNotEmpty()) {
                        LeaderboardSection(
                            title = "Most Runs",
                            valueLabel = "Runs",
                            accentColor = OrangeCap,
                            entries = stats.mostRuns
                        )
                    }
                    if (stats.mostWickets.isNotEmpty()) {
                        LeaderboardSection(
                            title = "Most Wickets",
                            valueLabel = "Wkts",
                            accentColor = PurpleCap,
                            entries = stats.mostWickets
                        )
                    }
                    if (stats.highestScore.isNotEmpty()) {
                        LeaderboardSection(
                            title = "Highest Score",
                            valueLabel = "Score",
                            accentColor = GoldPrimary,
                            entries = stats.highestScore
                        )
                    }
                    if (stats.bestBowling.isNotEmpty()) {
                        LeaderboardSection(
                            title = "Best Bowling",
                            valueLabel = "Figures",
                            accentColor = Color(0xFFE91E63),
                            entries = stats.bestBowling
                        )
                    }
                    if (stats.bestStrikeRate.isNotEmpty()) {
                        LeaderboardSection(
                            title = "Best Strike Rate",
                            valueLabel = "SR",
                            accentColor = Color(0xFF00BCD4),
                            entries = stats.bestStrikeRate
                        )
                    }
                    if (stats.bestEconomy.isNotEmpty()) {
                        LeaderboardSection(
                            title = "Best Economy",
                            valueLabel = "Econ",
                            accentColor = EmeraldPrimary,
                            entries = stats.bestEconomy
                        )
                    }
                    if (stats.mostFifties.isNotEmpty()) {
                        LeaderboardSection(
                            title = "Most Fifties",
                            valueLabel = "50s",
                            accentColor = Color(0xFF8BC34A),
                            entries = stats.mostFifties
                        )
                    }
                    if (stats.mostHundreds.isNotEmpty()) {
                        LeaderboardSection(
                            title = "Most Hundreds",
                            valueLabel = "100s",
                            accentColor = GoldPrimary,
                            entries = stats.mostHundreds
                        )
                    }

                    if (stats.mostRuns.isEmpty() && stats.mostWickets.isEmpty()) {
                        StatsEmptyState()
                    }

                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun CapHeroCard(
    label: String,
    subtitle: String,
    capColor: Color,
    player: PlayerStatLine?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    listOf(capColor.copy(alpha = 0.3f), DarkSurface)
                )
            )
            .border(1.dp, capColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Cap badge
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(capColor.copy(alpha = 0.2f))
                    .border(2.dp, capColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                val initials = player?.playerName?.take(2)?.uppercase() ?: "--"
                Text(initials, color = capColor, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
            }
            Text(label, color = capColor, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
            if (player != null) {
                Text(
                    player.playerName,
                    color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp,
                    maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center
                )
                Text(player.teamName, color = TextSecondary, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(player.primaryValue, color = capColor, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                if (player.secondaryValue.isNotBlank()) {
                    Text(player.secondaryValue, color = TextSecondary, fontSize = 11.sp)
                }
            } else {
                Text(subtitle, color = TextSecondary, fontSize = 11.sp, textAlign = TextAlign.Center)
                Text("No data yet", color = TextSecondary, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun LeaderboardSection(
    title: String,
    valueLabel: String,
    accentColor: Color,
    entries: List<PlayerStatLine>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
    ) {
        // Section header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(accentColor.copy(alpha = 0.1f))
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.width(3.dp).height(16.dp).clip(RoundedCornerShape(1.5.dp)).background(accentColor))
                Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Text(valueLabel, color = accentColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        entries.forEachIndexed { index, entry ->
            LeaderboardRow(
                rank = index + 1,
                entry = entry,
                accentColor = accentColor,
                isLast = index == entries.lastIndex
            )
        }
    }
}

@Composable
private fun LeaderboardRow(
    rank: Int,
    entry: PlayerStatLine,
    accentColor: Color,
    isLast: Boolean
) {
    val rankColor = when (rank) {
        1 -> GoldPrimary
        2 -> Color(0xFFB0BEC5)
        3 -> Color(0xFFCD7F32)
        else -> TextSecondary
    }
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Rank badge
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(if (rank <= 3) rankColor.copy(alpha = 0.15f) else DarkSurface2),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "$rank",
                    color = rankColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Player avatar initials
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.1f))
                    .border(1.dp, accentColor.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    entry.playerName.take(2).uppercase(),
                    color = accentColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Name + team
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    entry.playerName,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (entry.teamName.isNotBlank()) {
                    Text(entry.teamName, color = TextSecondary, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }

            // Primary + secondary value
            Column(horizontalAlignment = Alignment.End) {
                Text(entry.primaryValue, color = accentColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                if (entry.secondaryValue.isNotBlank()) {
                    Text(entry.secondaryValue, color = TextSecondary, fontSize = 10.sp)
                }
            }
        }
        if (!isLast) {
            HorizontalDivider(thickness = 0.5.dp, color = OutlineColor)
        }
    }
}

@Composable
private fun StatsEmptyState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurface)
            .padding(40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("No Stats Yet", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(
                "Stats will appear here once matches are completed in this tournament.",
                color = TextSecondary, fontSize = 13.sp, textAlign = TextAlign.Center
            )
        }
    }
}
