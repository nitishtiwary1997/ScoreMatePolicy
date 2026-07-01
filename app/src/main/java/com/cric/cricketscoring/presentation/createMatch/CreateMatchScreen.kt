package com.cric.cricketscoring.presentation.createMatch

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cric.cricketscoring.domain.model.SavedTeam
import com.cric.cricketscoring.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateMatchScreen(
    onMatchCreated: (matchId: String) -> Unit,
    onBack: () -> Unit,
    viewModel: CreateMatchViewModel = hiltViewModel()
) {
    val c = LocalAppColors.current
    val state by viewModel.uiState.collectAsState()

    var showLoadDialog by remember { mutableStateOf(false) }
    var pickedTeam by remember { mutableStateOf<SavedTeam?>(null) }

    LaunchedEffect(state.createdMatchId) {
        state.createdMatchId?.let { onMatchCreated(it) }
    }

    Scaffold(
        containerColor = c.bg,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "New Match",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = c.textPrimary
                        )
                        Text(
                            "Configure match settings",
                            fontSize = 11.sp,
                            color = c.textSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = EmeraldPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = c.bg,
                    titleContentColor = c.textPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(c.bg)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Match Settings Card ─────────────────────────────────────────
            SectionCard(title = "Match Settings", emoji = "🏏") {
                DarkOutlinedTextField(
                    value = state.team1Name,
                    onValueChange = viewModel::onTeam1NameChange,
                    label = "Team 1 Name",
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    )
                )
                DarkOutlinedTextField(
                    value = state.team2Name,
                    onValueChange = viewModel::onTeam2NameChange,
                    label = "Team 2 Name",
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    )
                )
                DarkOutlinedTextField(
                    value = state.totalOvers,
                    onValueChange = viewModel::onOversChange,
                    label = "Total Overs",
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    )
                )
                DarkOutlinedTextField(
                    value = state.playersPerTeam,
                    onValueChange = viewModel::onPlayersPerTeamChange,
                    label = "Players per Team",
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    )
                )
            }

            // ── Team Selector ───────────────────────────────────────────────
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf(
                    1 to (state.team1Name.ifBlank { "Team 1" }),
                    2 to (state.team2Name.ifBlank { "Team 2" })
                ).forEach { (team, name) ->
                    val selected = state.addingForTeam == team
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (selected)
                                    Brush.horizontalGradient(listOf(EmeraldPrimary, EmeraldDark))
                                else
                                    Brush.horizontalGradient(listOf(c.surface, c.surface))
                            )
                            .border(
                                1.dp,
                                if (selected) EmeraldPrimary else c.outline,
                                RoundedCornerShape(12.dp)
                            )
                    ) {
                        TextButton(
                            onClick = { viewModel.onAddingForTeamChange(team) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                name,
                                color = if (selected) Color.Black else c.textSecondary,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // ── Players Card ────────────────────────────────────────────────
            val teamName = if (state.addingForTeam == 1) state.team1Name.ifBlank { "Team 1" }
                           else state.team2Name.ifBlank { "Team 2" }
            val currentTeamName = if (state.addingForTeam == 1) state.team1Name.trim()
                                  else state.team2Name.trim()
            val players = if (state.addingForTeam == 1) state.team1Players else state.team2Players

            SectionCard(title = "$teamName Players", subtitle = "Minimum 2 required", emoji = "👥") {

                // Load saved team button
                if (state.savedTeams.isNotEmpty()) {
                    OutlinedButton(
                        onClick = { showLoadDialog = true },
                        modifier = Modifier.fillMaxWidth().height(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = EmeraldPrimary,
                            containerColor = EmeraldContainer
                        )
                    ) {
                        Icon(Icons.Default.Group, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Load Saved Team", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                // Add player row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DarkOutlinedTextField(
                        value = state.newPlayerName,
                        onValueChange = viewModel::onNewPlayerNameChange,
                        label = "Player name",
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Done
                        )
                    )
                    FilledIconButton(
                        onClick = viewModel::addPlayer,
                        enabled = state.newPlayerName.isNotBlank(),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = EmeraldPrimary,
                            contentColor = Color.Black,
                            disabledContainerColor = c.surface2
                        )
                    ) {
                        Icon(Icons.Default.Add, "Add player")
                    }
                }

                // Player list
                if (players.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(c.surface2, RoundedCornerShape(10.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No players added yet",
                            style = MaterialTheme.typography.bodySmall,
                            color = c.textTertiary
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        players.forEachIndexed { index, name ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(c.surface2, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .background(EmeraldContainer, RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "${index + 1}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = EmeraldPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text(
                                        name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = c.textPrimary
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.removePlayer(state.addingForTeam, index) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        "Remove",
                                        tint = CricketRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Save team row — shown when there are players and team name is filled
                if (players.isNotEmpty() && currentTeamName.isNotBlank()) {
                    HorizontalDivider(color = c.divider)
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (state.teamSaved) {
                            Text(
                                "✓ Saved!",
                                color = EmeraldPrimary,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Spacer(Modifier.width(1.dp))
                        }
                        OutlinedButton(
                            onClick = viewModel::saveCurrentTeam,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.5f)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = EmeraldPrimary,
                                containerColor = c.surface
                            )
                        ) {
                            Icon(Icons.Default.Bookmark, null, Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Save Team", fontSize = 12.sp)
                        }
                    }
                }
            }

            // Error
            state.error?.let {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CricketRedDim, RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⚠", fontSize = 16.sp)
                    Text(it, color = CricketRed, style = MaterialTheme.typography.bodySmall)
                }
            }

            // CTA Button
            Button(
                onClick = viewModel::createMatch,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                enabled = state.canCreate && !state.isCreating,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldPrimary,
                    contentColor = Color.Black,
                    disabledContainerColor = c.surface2,
                    disabledContentColor = c.textTertiary
                )
            ) {
                if (state.isCreating) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp, color = Color.Black)
                } else {
                    Text("Next: Toss →", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }

            Spacer(Modifier.height(16.dp))
        }

        // Dialogs
        if (showLoadDialog) {
            SavedTeamListDialog(
                savedTeams = state.savedTeams,
                onTeamSelected = { team ->
                    pickedTeam = team
                    showLoadDialog = false
                },
                onDeleteTeam = { teamName ->
                    viewModel.deleteSavedTeam(teamName)
                },
                onDismiss = { showLoadDialog = false }
            )
        }
        pickedTeam?.let { team ->
            PlayerPickerDialog(
                team = team,
                onConfirm = { names ->
                    viewModel.loadSavedTeamPlayers(names)
                    pickedTeam = null
                },
                onDismiss = { pickedTeam = null }
            )
        }
    }
}

// ── Saved Team Dialogs ────────────────────────────────────────────────────────

@Composable
private fun SavedTeamListDialog(
    savedTeams: List<SavedTeam>,
    onTeamSelected: (SavedTeam) -> Unit,
    onDeleteTeam: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val c = LocalAppColors.current
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = c.surface,
        titleContentColor = c.textPrimary,
        title = { Text("Saved Teams", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                savedTeams.forEach { team ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(c.surface2)
                            .clickable { onTeamSelected(team) }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.Group,
                                null,
                                Modifier.size(16.dp),
                                tint = EmeraldPrimary
                            )
                            Column {
                                Text(team.name, color = c.textPrimary, fontWeight = FontWeight.SemiBold)
                                Text(
                                    "${team.playerNames.size} players",
                                    color = c.textSecondary,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                        IconButton(
                            onClick = { onDeleteTeam(team.name) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                "Delete team",
                                tint = CricketRed,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = c.textSecondary) }
        }
    )
}

@Composable
private fun PlayerPickerDialog(
    team: SavedTeam,
    onConfirm: (List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    val c = LocalAppColors.current
    val checked = remember {
        mutableStateMapOf<String, Boolean>().also { m -> team.playerNames.forEach { m[it] = true } }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = c.surface,
        titleContentColor = c.textPrimary,
        title = {
            Column {
                Text(team.name, fontWeight = FontWeight.Bold)
                Text(
                    "Select players for this match",
                    style = MaterialTheme.typography.labelSmall,
                    color = c.textSecondary
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                team.playerNames.forEach { name ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (checked[name] == true) EmeraldContainer else c.surface2)
                            .clickable { checked[name] = !(checked[name] ?: true) }
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Checkbox(
                            checked = checked[name] ?: true,
                            onCheckedChange = { checked[name] = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = EmeraldPrimary,
                                uncheckedColor = c.textSecondary,
                                checkmarkColor = Color.Black
                            )
                        )
                        Text(name, color = c.textPrimary, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(team.playerNames.filter { checked[it] == true }) },
                enabled = checked.values.any { it },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.Black)
            ) {
                Text("Load Players", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = c.textSecondary) }
        }
    )
}

// ── Reusable dark themed components ──────────────────────────────────────────

@Composable
private fun SectionCard(
    title: String,
    emoji: String,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val c = LocalAppColors.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = c.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, c.outline)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(emoji, fontSize = 18.sp)
                Column {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleSmall,
                        color = c.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    if (subtitle != null) {
                        Text(subtitle, style = MaterialTheme.typography.labelSmall, color = c.textSecondary)
                    }
                }
            }
            HorizontalDivider(color = c.divider)
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DarkOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    val c = LocalAppColors.current
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = c.textSecondary) },
        modifier = modifier,
        keyboardOptions = keyboardOptions,
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = c.textPrimary,
            unfocusedTextColor = c.textPrimary,
            focusedBorderColor = EmeraldPrimary,
            unfocusedBorderColor = c.outline,
            focusedLabelColor = EmeraldPrimary,
            unfocusedLabelColor = c.textSecondary,
            cursorColor = EmeraldPrimary,
            focusedContainerColor = c.surface2,
            unfocusedContainerColor = c.surface2
        )
    )
}
