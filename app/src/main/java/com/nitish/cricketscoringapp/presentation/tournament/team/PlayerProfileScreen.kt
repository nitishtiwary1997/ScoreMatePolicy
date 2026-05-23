package com.nitish.cricketscoringapp.presentation.tournament.team

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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsCricket
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nitish.cricketscoringapp.domain.model.BowlingStyle
import com.nitish.cricketscoringapp.domain.model.PlayerRole
import com.nitish.cricketscoringapp.domain.model.TournamentPlayer
import com.nitish.cricketscoringapp.domain.repository.TournamentRepository
import com.nitish.cricketscoringapp.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
class PlayerProfileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: TournamentRepository
) : ViewModel() {

    private val playerId: String = checkNotNull(savedStateHandle["playerId"])

    private val _player = MutableStateFlow<TournamentPlayer?>(null)
    val player: StateFlow<TournamentPlayer?> = _player.asStateFlow()

    private val _team = MutableStateFlow<com.nitish.cricketscoringapp.domain.model.TournamentTeam?>(null)
    val team: StateFlow<com.nitish.cricketscoringapp.domain.model.TournamentTeam?> = _team.asStateFlow()

    init {
        viewModelScope.launch {
            val p = repository.getPlayerByIdSync(playerId)
            _player.value = p
            if (p != null) {
                _team.value = repository.getTeamByIdSync(p.teamId)
            }
        }
    }
}

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerProfileScreen(
    onBack: () -> Unit,
    viewModel: PlayerProfileViewModel = hiltViewModel()
) {
    val c = LocalAppColors.current
    val player by viewModel.player.collectAsState()
    val team by viewModel.team.collectAsState()

    Scaffold(
        containerColor = c.bg,
        topBar = {
            TopAppBar(
                title = { Text("Player Profile", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = c.textPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = c.textSecondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = c.bg)
            )
        }
    ) { padding ->
        val p = player
        if (p == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Loading…", color = c.textSecondary)
            }
            return@Scaffold
        }

        val roleColor = when (p.role) {
            PlayerRole.BATSMAN       -> EmeraldPrimary
            PlayerRole.BOWLER        -> GoldPrimary
            PlayerRole.ALL_ROUNDER   -> Color(0xFF64B5F6)
            PlayerRole.WICKET_KEEPER -> Color(0xFFCE93D8)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(c.bg)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Hero card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(roleColor.copy(alpha = 0.15f), c.surface)
                        )
                    )
                    .border(1.dp, roleColor.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .background(
                                Brush.radialGradient(listOf(roleColor.copy(0.3f), roleColor.copy(0.05f))),
                                CircleShape
                            )
                            .border(2.dp, roleColor.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            p.name.take(1).uppercase(),
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Black,
                            color = roleColor
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(p.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = c.textPrimary, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RoleBadge(role = p.role, color = roleColor)
                        if (p.jerseyNumber > 0) {
                            Box(
                                modifier = Modifier
                                    .background(c.surface.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                    .border(1.dp, c.outline, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    "#${p.jerseyNumber}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = c.textSecondary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    if (team != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(team!!.name, style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
                    }
                }
            }

            // Info section
            InfoSection(
                items = buildList {
                    add("Batting Style" to p.battingStyle.label)
                    if (p.canBowl || p.bowlingStyle != BowlingStyle.NONE) {
                        add("Bowling Style" to p.bowlingStyle.label)
                    }
                    add("Role" to p.role.label)
                }
            )

            // Stats placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(c.surface)
                    .border(1.dp, c.outline, RoundedCornerShape(14.dp))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.SportsCricket, null, tint = EmeraldPrimary.copy(0.4f), modifier = Modifier.size(32.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("Tournament stats available after matches", style = MaterialTheme.typography.bodySmall, color = c.textSecondary, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

// ── Info Section ──────────────────────────────────────────────────────────────

@Composable
private fun InfoSection(items: List<Pair<String, String>>) {
    val c = LocalAppColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(c.surface)
            .border(1.dp, c.outline, RoundedCornerShape(14.dp))
    ) {
        items.forEachIndexed { index, (label, value) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(label, style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
                Text(value, style = MaterialTheme.typography.bodyMedium, color = c.textPrimary, fontWeight = FontWeight.SemiBold)
            }
            if (index < items.lastIndex) {
                HorizontalDivider(color = c.divider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
            }
        }
    }
}

// ── Role Badge ────────────────────────────────────────────────────────────────

@Composable
private fun RoleBadge(role: PlayerRole, color: Color) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 3.dp)
    ) {
        Text(
            role.shortLabel,
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}
