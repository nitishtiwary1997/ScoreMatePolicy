package com.nitish.cricketscoringapp.presentation.tournament.team

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nitish.cricketscoringapp.domain.model.BattingStyle
import com.nitish.cricketscoringapp.domain.model.BowlingStyle
import com.nitish.cricketscoringapp.domain.model.PlayerRole
import com.nitish.cricketscoringapp.domain.model.TournamentPlayer
import com.nitish.cricketscoringapp.domain.model.TournamentTeam
import com.nitish.cricketscoringapp.ui.theme.CricketRed
import com.nitish.cricketscoringapp.ui.theme.DarkBg
import com.nitish.cricketscoringapp.ui.theme.DarkSurface
import com.nitish.cricketscoringapp.ui.theme.DarkSurface2
import com.nitish.cricketscoringapp.ui.theme.DividerColor
import com.nitish.cricketscoringapp.ui.theme.EmeraldContainer
import com.nitish.cricketscoringapp.ui.theme.EmeraldDark
import com.nitish.cricketscoringapp.ui.theme.EmeraldPrimary
import com.nitish.cricketscoringapp.ui.theme.GoldPrimary
import com.nitish.cricketscoringapp.ui.theme.OutlineColor
import com.nitish.cricketscoringapp.ui.theme.TextPrimary
import com.nitish.cricketscoringapp.ui.theme.TextSecondary
import com.nitish.cricketscoringapp.ui.theme.TextTertiary
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamDetailScreen(
    onBack: () -> Unit,
    onPlayerClick: (playerId: String) -> Unit,
    viewModel: TeamDetailViewModel = hiltViewModel()
) {
    val team by viewModel.team.collectAsState()
    val players by viewModel.players.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<TournamentPlayer?>(null) }
    val snackbarHost = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHost.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        containerColor = DarkBg,
        snackbarHost = { SnackbarHost(snackbarHost) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            team?.name ?: "Team",
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            "${players.size} players",
                            style = MaterialTheme.typography.labelSmall,
                            color = EmeraldPrimary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = TextSecondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBg)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = EmeraldPrimary,
                contentColor = Color.Black,
                shape = RoundedCornerShape(16.dp),
                icon = { Icon(Icons.Default.Add, null, modifier = Modifier.size(20.dp)) },
                text = { Text("Add Player", fontWeight = FontWeight.Bold, fontSize = 14.sp) }
            )
        }
    ) { padding ->
        if (players.isEmpty()) {
            PlayerEmptyState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                onAdd = { showAddDialog = true }
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DarkBg)
                    .padding(padding),
                contentPadding = PaddingValues(
                    start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Team header card
                item {
                    team?.let { TeamHeaderCard(it) }
                    Spacer(Modifier.height(4.dp))
                }

                // Players grouped by role
                val byRole = players.groupBy { it.role }
                PlayerRole.entries.forEach { role ->
                    val rolePlayers = byRole[role] ?: return@forEach
                    item(key = role.name) {
                        RoleHeader(role = role, count = rolePlayers.size)
                    }
                    items(rolePlayers, key = { it.id }) { player ->
                        PlayerCard(
                            player = player,
                            isCaptain = team?.captainPlayerId == player.id,
                            onClick = { onPlayerClick(player.id) },
                            onDelete = { deleteTarget = player },
                            onSetCaptain = { viewModel.setCaptain(player.id) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddPlayerDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { form ->
                viewModel.addPlayer(form)
                showAddDialog = false
            }
        )
    }

    deleteTarget?.let { p ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            containerColor = DarkSurface,
            titleContentColor = TextPrimary,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Delete, null, tint = CricketRed, modifier = Modifier.size(20.dp))
                    Text("Remove Player", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    "Remove ${p.name} from this team?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.deletePlayer(p.id); deleteTarget = null },
                    colors = ButtonDefaults.buttonColors(containerColor = CricketRed)
                ) { Text("Remove", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

// ── Team Header Card ──────────────────────────────────────────────────────────

@Composable
private fun TeamHeaderCard(team: TournamentTeam) {
    val (avatarBg, avatarFg) = teamColors(team.name)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurface)
            .border(1.dp, OutlineColor, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(avatarBg, RoundedCornerShape(16.dp))
                .border(1.5.dp, avatarFg.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(team.initials.take(2), fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = avatarFg)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(team.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
            if (team.homeGround.isNotBlank()) {
                Text(team.homeGround, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            if (team.shortName.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .background(avatarBg, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 1.dp)
                ) {
                    Text(team.shortName, style = MaterialTheme.typography.labelSmall, color = avatarFg, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                }
            }
        }
    }
}

// ── Role Header ───────────────────────────────────────────────────────────────

@Composable
private fun RoleHeader(role: PlayerRole, count: Int) {
    val color = when (role) {
        PlayerRole.BATSMAN       -> EmeraldPrimary
        PlayerRole.BOWLER        -> GoldPrimary
        PlayerRole.ALL_ROUNDER   -> Color(0xFF64B5F6)
        PlayerRole.WICKET_KEEPER -> Color(0xFFCE93D8)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(Modifier.width(3.dp).height(14.dp).background(color, RoundedCornerShape(2.dp)))
        Text(
            role.label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )
        Box(
            modifier = Modifier
                .background(color.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 1.dp)
        ) {
            Text(count.toString(), style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold, fontSize = 10.sp)
        }
    }
}

// ── Player Card ───────────────────────────────────────────────────────────────

@Composable
private fun PlayerCard(
    player: TournamentPlayer,
    isCaptain: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onSetCaptain: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val roleColor = when (player.role) {
        PlayerRole.BATSMAN       -> EmeraldPrimary
        PlayerRole.BOWLER        -> GoldPrimary
        PlayerRole.ALL_ROUNDER   -> Color(0xFF64B5F6)
        PlayerRole.WICKET_KEEPER -> Color(0xFFCE93D8)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .border(1.dp, if (isCaptain) GoldPrimary.copy(alpha = 0.4f) else OutlineColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, top = 12.dp, end = 4.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Jersey number badge
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(DarkSurface2, CircleShape)
                    .border(1.dp, OutlineColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (player.jerseyNumber > 0) "#${player.jerseyNumber}" else "—",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = roleColor
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        player.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (isCaptain) {
                        Box(
                            modifier = Modifier
                                .background(GoldPrimary.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .border(1.dp, GoldPrimary.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text("C", style = MaterialTheme.typography.labelSmall, color = GoldPrimary, fontWeight = FontWeight.Black, fontSize = 9.sp)
                        }
                    }
                }
                Spacer(Modifier.height(2.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        player.role.shortLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = roleColor,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp
                    )
                    Text("·", color = TextTertiary, fontSize = 10.sp)
                    Text(
                        player.battingStyle.label.replace("Right Hand", "RHB").replace("Left Hand", "LHB"),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                    if (player.canBowl && player.bowlingStyle != BowlingStyle.NONE) {
                        Text("·", color = TextTertiary, fontSize = 10.sp)
                        Text(
                            player.bowlingStyle.label.take(12),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            fontSize = 10.sp,
                            maxLines = 1
                        )
                    }
                }
            }

            Box {
                Row {
                    IconButton(onClick = { onSetCaptain() }, modifier = Modifier.size(36.dp)) {
                        Icon(
                            if (isCaptain) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            "Set Captain",
                            tint = if (isCaptain) GoldPrimary else TextTertiary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = { onDelete() }, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Delete, "Remove", tint = TextTertiary, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

// ── Add Player Dialog ─────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddPlayerDialog(
    onDismiss: () -> Unit,
    onConfirm: (AddPlayerForm) -> Unit
) {
    var form by remember { mutableStateOf(AddPlayerForm()) }
    var nameError by remember { mutableStateOf(false) }
    var bowlingExpanded by remember { mutableStateOf(false) }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = EmeraldPrimary,
        unfocusedBorderColor = OutlineColor,
        focusedLabelColor = EmeraldPrimary,
        unfocusedLabelColor = TextSecondary,
        cursorColor = EmeraldPrimary,
        focusedTextColor = TextPrimary,
        unfocusedTextColor = TextPrimary,
        errorBorderColor = CricketRed
    )
    val chipColors = FilterChipDefaults.filterChipColors(
        selectedContainerColor = EmeraldDark,
        selectedLabelColor = EmeraldPrimary,
        containerColor = DarkSurface2,
        labelColor = TextSecondary
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        titleContentColor = TextPrimary,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Person, null, tint = EmeraldPrimary, modifier = Modifier.size(20.dp))
                Text("Add Player", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Name + Jersey
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = form.name,
                        onValueChange = { form = form.copy(name = it); nameError = false },
                        label = { Text("Name *") },
                        singleLine = true,
                        isError = nameError,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        colors = fieldColors,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = form.jerseyNumber,
                        onValueChange = { if (it.length <= 3) form = form.copy(jerseyNumber = it.filter { c -> c.isDigit() }) },
                        label = { Text("#") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = fieldColors,
                        modifier = Modifier.width(72.dp)
                    )
                }

                // Role
                Text("Role", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    PlayerRole.entries.forEach { role ->
                        FilterChip(
                            selected = form.role == role,
                            onClick = { form = form.copy(role = role, bowlingStyle = if (role == PlayerRole.BATSMAN) BowlingStyle.NONE else form.bowlingStyle) },
                            label = { Text(role.label, fontSize = 11.sp) },
                            colors = chipColors
                        )
                    }
                }

                // Batting Style
                Text("Batting", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BattingStyle.entries.forEach { style ->
                        FilterChip(
                            selected = form.battingStyle == style,
                            onClick = { form = form.copy(battingStyle = style) },
                            label = { Text(style.label.replace("Right Hand", "RHB").replace("Left Hand", "LHB"), fontSize = 11.sp) },
                            colors = chipColors
                        )
                    }
                }

                // Bowling Style (only if can bowl)
                if (form.role != PlayerRole.BATSMAN) {
                    Text("Bowling Style", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    Box {
                        OutlinedTextField(
                            value = form.bowlingStyle.label,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Style") },
                            colors = fieldColors,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { bowlingExpanded = true }
                        )
                        DropdownMenu(
                            expanded = bowlingExpanded,
                            onDismissRequest = { bowlingExpanded = false }
                        ) {
                            BowlingStyle.entries.forEach { style ->
                                DropdownMenuItem(
                                    text = { Text(style.label, color = TextPrimary) },
                                    onClick = { form = form.copy(bowlingStyle = style); bowlingExpanded = false }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (form.name.isBlank()) { nameError = true; return@Button }
                    onConfirm(form)
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.Black)
            ) { Text("Add", fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        }
    )
}

// ── Player Empty State ────────────────────────────────────────────────────────

@Composable
private fun PlayerEmptyState(modifier: Modifier = Modifier, onAdd: () -> Unit) {
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
            Icon(Icons.Default.Group, null, tint = EmeraldPrimary, modifier = Modifier.size(48.dp))
        }
        Spacer(Modifier.height(20.dp))
        Text("No Players Yet", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(
            "Add players to the team. Tap the star icon to set the captain.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onAdd,
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.Black),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 28.dp, vertical = 12.dp)
        ) {
            Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Add First Player", fontWeight = FontWeight.Bold)
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private val avatarBgPalette = listOf(
    Color(0xFF1E3A5F), Color(0xFF2D1B4E), Color(0xFF1A3A2A),
    Color(0xFF3A1A1A), Color(0xFF1A2E3A), Color(0xFF2A2A14), Color(0xFF2A1A2A)
)
private val avatarFgPalette = listOf(
    Color(0xFF64B5F6), Color(0xFFCE93D8), EmeraldPrimary,
    Color(0xFFEF9A9A), Color(0xFF80DEEA), GoldPrimary, Color(0xFFF48FB1)
)

private fun teamColors(name: String): Pair<Color, Color> {
    val idx = abs(name.hashCode()) % avatarBgPalette.size
    return avatarBgPalette[idx] to avatarFgPalette[idx]
}
