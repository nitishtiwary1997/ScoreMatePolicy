package com.cric.cricketscoring.presentation.toss

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cric.cricketscoring.domain.model.Player
import com.cric.cricketscoring.domain.model.TossChoice
import com.cric.cricketscoring.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TossScreen(
    onMatchStarted: (matchId: String) -> Unit,
    onBack: () -> Unit,
    viewModel: TossViewModel = hiltViewModel()
) {
    val c = LocalAppColors.current
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.startedMatchId) {
        state.startedMatchId?.let { onMatchStarted(it) }
    }

    Scaffold(
        containerColor = c.bg,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Toss & Setup",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = c.textPrimary
                        )
                        Text(
                            "Configure your match",
                            fontSize = 11.sp,
                            color = c.textSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = EmeraldPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = c.bg)
            )
        }
    ) { padding ->
        val match = state.match ?: return@Scaffold

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(c.bg)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Step indicator
            StepIndicator(currentStep = state.step)

            when (state.step) {
                TossStep.TOSS_SETUP -> TossSetupStep(
                    state = state,
                    team1Name = match.team1Name,
                    team2Name = match.team2Name,
                    onTeamSelected = viewModel::onTossWonByTeamChange,
                    onChoiceSelected = viewModel::onTossChoiceChange,
                    onNext = viewModel::onTossConfirmed
                )
                TossStep.SELECT_OPENERS -> SelectOpenersStep(
                    state = state,
                    onOpener1Change = viewModel::onOpener1Change,
                    onOpener2Change = viewModel::onOpener2Change,
                    onNext = viewModel::onOpenersConfirmed
                )
                TossStep.SELECT_BOWLER -> SelectBowlerStep(
                    state = state,
                    onBowlerChange = viewModel::onBowlerChange,
                    onStartMatch = viewModel::startMatch
                )
            }
        }
    }
}

@Composable
private fun StepIndicator(currentStep: TossStep) {
    val c = LocalAppColors.current
    val steps = listOf("Toss", "Openers", "Bowler")
    val currentIndex = when (currentStep) {
        TossStep.TOSS_SETUP      -> 0
        TossStep.SELECT_OPENERS  -> 1
        TossStep.SELECT_BOWLER   -> 2
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, label ->
            val active   = index == currentIndex
            val done     = index < currentIndex
            val bgColor  = when {
                active -> EmeraldPrimary
                done   -> EmeraldDark
                else   -> c.surface2
            }
            val textColor = when {
                active || done -> Color.Black
                else           -> c.textTertiary
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(bgColor, RoundedCornerShape(8.dp))
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "${index + 1}. $label",
                    fontSize = 12.sp,
                    fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                    color = if (active || done) Color.Black else c.textTertiary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TossSetupStep(
    state: TossUiState,
    team1Name: String,
    team2Name: String,
    onTeamSelected: (Int) -> Unit,
    onChoiceSelected: (TossChoice) -> Unit,
    onNext: () -> Unit
) {
    val c = LocalAppColors.current
    TossCard(title = "Toss Result", emoji = "🪙") {
        Text("Who won the toss?", style = MaterialTheme.typography.labelMedium, color = c.textSecondary)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf(1 to team1Name, 2 to team2Name).forEach { (team, name) ->
                val selected = state.tossWonByTeam == team
                TossOptionChip(
                    label = name,
                    selected = selected,
                    modifier = Modifier.weight(1f),
                    onClick = { onTeamSelected(team) }
                )
            }
        }

        Spacer(Modifier.height(4.dp))
        Text("Elected to:", style = MaterialTheme.typography.labelMedium, color = c.textSecondary)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf(TossChoice.BAT to "🏏 Bat", TossChoice.BOWL to "⚾ Bowl").forEach { (choice, label) ->
                val selected = state.tossChoice == choice
                TossOptionChip(
                    label = label,
                    selected = selected,
                    modifier = Modifier.weight(1f),
                    onClick = { onChoiceSelected(choice) }
                )
            }
        }

        val battingTeamName = if (state.battingTeamNumber == 1) team1Name else team2Name
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(EmeraldContainer, RoundedCornerShape(10.dp))
                .border(1.dp, EmeraldPrimary.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                .padding(12.dp)
        ) {
            Text(
                "🏏 $battingTeamName will bat first",
                style = MaterialTheme.typography.bodyMedium,
                color = EmeraldLight,
                fontWeight = FontWeight.Bold
            )
        }

        PrimaryActionButton(
            text = "Select Openers →",
            onClick = onNext,
            enabled = true
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectOpenersStep(
    state: TossUiState,
    onOpener1Change: (String) -> Unit,
    onOpener2Change: (String) -> Unit,
    onNext: () -> Unit
) {
    TossCard(title = "Opening Batsmen", emoji = "🏏") {
        DarkPlayerDropdown(
            label = "Striker (facing first ball)",
            players = state.battingPlayers,
            selectedId = state.opener1Id,
            excludeId = state.opener2Id,
            onSelected = onOpener1Change
        )
        DarkPlayerDropdown(
            label = "Non-Striker",
            players = state.battingPlayers,
            selectedId = state.opener2Id,
            excludeId = state.opener1Id,
            onSelected = onOpener2Change
        )
        PrimaryActionButton(
            text = "Select Opening Bowler →",
            onClick = onNext,
            enabled = state.canProceedOpeners
        )
    }
}

@Composable
private fun SelectBowlerStep(
    state: TossUiState,
    onBowlerChange: (String) -> Unit,
    onStartMatch: () -> Unit
) {
    TossCard(title = "Opening Bowler", emoji = "⚾") {
        DarkPlayerDropdown(
            label = "Opening Bowler",
            players = state.bowlingPlayers,
            selectedId = state.bowlerId,
            onSelected = onBowlerChange
        )
        PrimaryActionButton(
            text = if (state.isStarting) "Starting…" else "🚀 Start Match",
            onClick = onStartMatch,
            enabled = state.canStartMatch && !state.isStarting,
            isLoading = state.isStarting
        )
    }
}

// ── Shared composables ────────────────────────────────────────────────────────

@Composable
private fun TossCard(
    title: String,
    emoji: String,
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
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(emoji, fontSize = 20.sp)
                Text(title, style = MaterialTheme.typography.titleMedium, color = c.textPrimary, fontWeight = FontWeight.Bold)
            }
            HorizontalDivider(color = c.divider)
            content()
        }
    }
}

@Composable
private fun TossOptionChip(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val c = LocalAppColors.current
    val bg = if (selected)
        Brush.horizontalGradient(listOf(EmeraldPrimary, EmeraldDark))
    else
        Brush.horizontalGradient(listOf(c.surface2, c.surface2))

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .border(1.dp, if (selected) EmeraldPrimary else c.outline, RoundedCornerShape(10.dp))
    ) {
        TextButton(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(
                label,
                color = if (selected) Color.Black else c.textSecondary,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun PrimaryActionButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    val c = LocalAppColors.current
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        enabled = enabled,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = EmeraldPrimary,
            contentColor = Color.Black,
            disabledContainerColor = c.surface2,
            disabledContentColor = c.textTertiary
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp, color = Color.Black)
        } else {
            Text(text, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DarkPlayerDropdown(
    label: String,
    players: List<Player>,
    selectedId: String,
    excludeId: String = "",
    onSelected: (String) -> Unit
) {
    val c = LocalAppColors.current
    var expanded by remember { mutableStateOf(false) }
    val available = players.filter { it.id != excludeId }
    val selectedName = players.find { it.id == selectedId }?.name ?: "Select player"

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            label = { Text(label, color = c.textSecondary) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
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
                unfocusedContainerColor = c.surface2,
                focusedTrailingIconColor = EmeraldPrimary,
                unfocusedTrailingIconColor = c.textSecondary
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = c.surface2
        ) {
            available.forEach { player ->
                DropdownMenuItem(
                    text = { Text(player.name, color = c.textPrimary) },
                    onClick = {
                        onSelected(player.id)
                        expanded = false
                    },
                    colors = MenuItemColors(
                        textColor = c.textPrimary,
                        leadingIconColor = EmeraldPrimary,
                        trailingIconColor = EmeraldPrimary,
                        disabledTextColor = c.textTertiary,
                        disabledLeadingIconColor = c.textTertiary,
                        disabledTrailingIconColor = c.textTertiary
                    )
                )
            }
        }
    }
}

// Keep the original PlayerDropdown for backward compat with TossScreen's public API
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerDropdown(
    label: String,
    players: List<Player>,
    selectedId: String,
    excludeId: String = "",
    onSelected: (String) -> Unit
) = DarkPlayerDropdown(label, players, selectedId, excludeId, onSelected)
