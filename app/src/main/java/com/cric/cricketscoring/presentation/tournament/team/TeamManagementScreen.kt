package com.cric.cricketscoring.presentation.tournament.team

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cric.cricketscoring.domain.model.TournamentTeam
import com.cric.cricketscoring.ui.theme.*
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamManagementScreen(
    onBack: () -> Unit,
    onTeamClick: (teamId: String) -> Unit,
    viewModel: TeamManagementViewModel = hiltViewModel()
) {
    val c = LocalAppColors.current
    val teams by viewModel.teams.collectAsState()
    val tournament by viewModel.tournament.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<TournamentTeam?>(null) }
    val snackbarHost = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHost.showSnackbar(it)
            viewModel.clearError()
        }
    }

    val maxTeams = tournament?.maxTeams ?: 16
    val canAddMore = teams.size < maxTeams

    Scaffold(
        containerColor = c.bg,
        snackbarHost = { SnackbarHost(snackbarHost) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Teams",
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
                    Box(
                        modifier = Modifier
                            .background(EmeraldContainer, RoundedCornerShape(8.dp))
                            .border(1.dp, EmeraldPrimary.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "${teams.size}/$maxTeams",
                            style = MaterialTheme.typography.labelMedium,
                            color = EmeraldPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = c.bg)
            )
        },
        floatingActionButton = {
            if (canAddMore) {
                ExtendedFloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = EmeraldPrimary,
                    contentColor = Color.Black,
                    shape = RoundedCornerShape(16.dp),
                    icon = { Icon(Icons.Default.Add, null, modifier = Modifier.size(20.dp)) },
                    text = { Text("Add Team", fontWeight = FontWeight.Bold, fontSize = 14.sp) }
                )
            }
        }
    ) { padding ->
        if (teams.isEmpty()) {
            TeamEmptyState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                onAdd = { showAddDialog = true }
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(c.bg)
                    .padding(padding),
                contentPadding = PaddingValues(
                    start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(teams, key = { it.id }) { team ->
                    TeamCard(
                        team = team,
                        onClick = { onTeamClick(team.id) },
                        onDelete = { deleteTarget = team }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddTeamDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, shortName, ground ->
                viewModel.addTeam(name, shortName, ground)
                showAddDialog = false
            }
        )
    }

    deleteTarget?.let { t ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            containerColor = c.surface,
            titleContentColor = c.textPrimary,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Delete, null, tint = CricketRed, modifier = Modifier.size(20.dp))
                    Text("Remove Team", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    "Remove \"${t.name}\" and all its players from this tournament?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = c.textSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.deleteTeam(t.id); deleteTarget = null },
                    colors = ButtonDefaults.buttonColors(containerColor = CricketRed)
                ) { Text("Remove", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text("Cancel", color = c.textSecondary)
                }
            }
        )
    }
}

// ── Team Card ─────────────────────────────────────────────────────────────────

@Composable
private fun TeamCard(
    team: TournamentTeam,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val c = LocalAppColors.current
    val (avatarBg, avatarFg) = teamColors(team.name)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(c.surface)
            .border(1.dp, c.outline, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, top = 14.dp, end = 10.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Team avatar
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(avatarBg, RoundedCornerShape(14.dp))
                    .border(1.5.dp, avatarFg.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    team.initials.take(2),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = avatarFg
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    team.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = c.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (team.homeGround.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        team.homeGround,
                        style = MaterialTheme.typography.bodySmall,
                        color = c.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (team.shortName.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .background(avatarBg, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 1.dp)
                    ) {
                        Text(
                            team.shortName,
                            style = MaterialTheme.typography.labelSmall,
                            color = avatarFg,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        )
                    }
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    "Remove team",
                    tint = c.textTertiary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// ── Add Team Dialog ───────────────────────────────────────────────────────────

@Composable
private fun AddTeamDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, shortName: String, homeGround: String) -> Unit
) {
    val c = LocalAppColors.current
    var name by remember { mutableStateOf("") }
    var shortName by remember { mutableStateOf("") }
    var ground by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = c.surface,
        titleContentColor = c.textPrimary,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Shield, null, tint = EmeraldPrimary, modifier = Modifier.size(20.dp))
                Text("Add Team", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                val fieldColors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EmeraldPrimary,
                    unfocusedBorderColor = c.outline,
                    focusedLabelColor = EmeraldPrimary,
                    unfocusedLabelColor = c.textSecondary,
                    cursorColor = EmeraldPrimary,
                    focusedTextColor = c.textPrimary,
                    unfocusedTextColor = c.textPrimary,
                    errorBorderColor = CricketRed
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; nameError = false },
                    label = { Text("Team Name *") },
                    singleLine = true,
                    isError = nameError,
                    supportingText = if (nameError) {{ Text("Required", color = CricketRed) }} else null,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = shortName,
                    onValueChange = { if (it.length <= 4) shortName = it.uppercase() },
                    label = { Text("Short Name (max 4)") },
                    placeholder = { Text("e.g. MUM", color = c.textTertiary) },
                    singleLine = true,
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = ground,
                    onValueChange = { ground = it },
                    label = { Text("Home Ground") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) { nameError = true; return@Button }
                    onConfirm(name, shortName, ground)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldPrimary,
                    contentColor = Color.Black
                )
            ) { Text("Add", fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = c.textSecondary) }
        }
    )
}

// ── Empty State ───────────────────────────────────────────────────────────────

@Composable
private fun TeamEmptyState(modifier: Modifier = Modifier, onAdd: () -> Unit) {
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
            Icon(Icons.Default.Group, null, tint = EmeraldPrimary, modifier = Modifier.size(48.dp))
        }
        Spacer(Modifier.height(20.dp))
        Text("No Teams Yet", style = MaterialTheme.typography.titleMedium, color = c.textPrimary, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(
            "Add teams to the tournament. Each team can then have players registered.",
            style = MaterialTheme.typography.bodyMedium,
            color = c.textSecondary,
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
            Text("Add First Team", fontWeight = FontWeight.Bold)
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
