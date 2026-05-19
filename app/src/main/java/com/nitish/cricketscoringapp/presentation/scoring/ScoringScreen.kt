package com.nitish.cricketscoringapp.presentation.scoring

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.nitish.cricketscoringapp.domain.model.*
import com.nitish.cricketscoringapp.presentation.toss.DarkPlayerDropdown
import com.nitish.cricketscoringapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoringScreen(
    onMatchComplete: (matchId: String) -> Unit,
    onBack: () -> Unit,
    viewModel: ScoringViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    val match = state.match
    val score = state.currentScore

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = {
                    if (match != null && score != null) {
                        Column {
                            Text(
                                "${match.team1Name} vs ${match.team2Name}",
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Innings ${state.currentInnings} · ${match.totalOvers} overs",
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldPrimary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = EmeraldPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::showAddPlayerDialog) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Add Player", tint = EmeraldPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBg)
            )
        }
    ) { padding ->
        if (state.isLoading || match == null || score == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = EmeraldPrimary)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBg)
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            ScoreHeader(score)
            Spacer(Modifier.height(8.dp))
            BatsmenPanel(score)
            Spacer(Modifier.height(6.dp))
            BowlerPanel(score)
            Spacer(Modifier.height(6.dp))
            CurrentOverPanel(state.currentBalls, score.completedOvers)
            Spacer(Modifier.height(10.dp))
            val bowlingTeamNum = if (match.battingTeamNumber(state.currentInnings) == 1) 2 else 1
            val bowlingTeamPlayers = state.players.filter { it.team == bowlingTeamNum }
            ScoringButtons(
                activeBatsmen = score.batsmen.filter { !it.isOut },
                bowlingTeamPlayers = bowlingTeamPlayers,
                currentBowlerId = score.currentBowlerId,
                onRun    = { runs -> viewModel.recordBall(runs) },
                onExtra  = { type, runs -> viewModel.recordBall(runs, extraType = type) },
                onWicket = { type, dismissedId, fIds ->
                    viewModel.recordBall(0, isWicket = true, wicketType = type, dismissedPlayerId = dismissedId, fielderIds = fIds)
                },
                onUndo   = viewModel::undoLastBall
            )
            Spacer(Modifier.height(24.dp))
        }

        // Dialogs
        when (val dialog = state.dialog) {
            is ScoringDialog.SelectBatsman -> PlayerSelectionDialog(
                title = "New Batsman",
                players = dialog.available,
                onSelected = viewModel::onSelectBatsman,
                onDismiss = {}
            )
            is ScoringDialog.SelectBowler -> PlayerSelectionDialog(
                title = "Select Bowler for Next Over",
                players = dialog.available,
                onSelected = viewModel::onSelectBowler,
                onDismiss = {}
            )
            is ScoringDialog.InningsComplete -> StartInnings2Dialog(
                match = match,
                players = state.players,
                innings1Score = state.innings1Score,
                onStart = viewModel::onStartInnings2
            )
            is ScoringDialog.MatchComplete -> MatchCompleteDialog(
                result = dialog.result,
                onDone = { onMatchComplete(match.id) }
            )
            ScoringDialog.AddPlayer -> AddPlayerDialog(
                team1Name = match.team1Name,
                team2Name = match.team2Name,
                onAdd     = { name, team -> viewModel.addPlayer(name, team) },
                onDismiss = viewModel::dismissAddPlayerDialog
            )
            ScoringDialog.None -> {}
        }
    }
}

// ── Score Header ──────────────────────────────────────────────────────────────

@Composable
private fun ScoreHeader(score: InningsScore) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(listOf(DarkSurface3, DarkSurface))
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = score.battingTeamName,
                style = MaterialTheme.typography.labelMedium,
                color = EmeraldPrimary,
                letterSpacing = 1.5.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${score.totalRuns}/${score.wickets}",
                fontSize = 56.sp,
                fontWeight = FontWeight.Black,
                color = TextPrimary
            )
            Text(
                text = "Overs: ${score.oversDisplay} / ${score.totalOvers}",
                style = MaterialTheme.typography.titleSmall,
                color = TextSecondary
            )
            score.target?.let { _ ->
                Spacer(Modifier.height(8.dp))
                val rr = score.requiredRuns ?: 0
                Box(
                    modifier = Modifier
                        .background(GoldContainer, RoundedCornerShape(20.dp))
                        .border(1.dp, GoldPrimary.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Need $rr from ${score.requiredBallsRemaining} balls · RRR: ${"%.2f".format(score.requiredRunRate ?: 0f)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = GoldPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ── Batsmen Panel ─────────────────────────────────────────────────────────────

@Composable
private fun BatsmenPanel(score: InningsScore) {
    val activeBatsmen = score.batsmen.filter { !it.isOut }
    DarkScoreCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
        Row(Modifier.fillMaxWidth()) {
            Text("Batsman", Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontWeight = FontWeight.Bold)
            listOf("R", "B", "4s", "6s").forEach {
                Text(it, Modifier.width(34.dp), textAlign = TextAlign.End, style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontWeight = FontWeight.Bold)
            }
            Text("SR", Modifier.width(52.dp), textAlign = TextAlign.End, style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontWeight = FontWeight.Bold)
        }
        HorizontalDivider(color = DividerColor, modifier = Modifier.padding(vertical = 4.dp))
        activeBatsmen.forEach { b ->
            Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    if (b.isOnStrike) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(EmeraldPrimary, CircleShape)
                        )
                        Spacer(Modifier.width(6.dp))
                    } else {
                        Spacer(Modifier.width(12.dp))
                    }
                    Text(b.player.name, style = MaterialTheme.typography.bodySmall, color = TextPrimary, fontWeight = if (b.isOnStrike) FontWeight.Bold else FontWeight.Normal, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Text("${b.runs}", Modifier.width(34.dp), textAlign = TextAlign.End, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = if (b.runs >= 50) GoldPrimary else TextPrimary)
                Text("${b.balls}", Modifier.width(34.dp), textAlign = TextAlign.End, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Text("${b.fours}", Modifier.width(34.dp), textAlign = TextAlign.End, style = MaterialTheme.typography.bodySmall, color = CricketBlue)
                Text("${b.sixes}", Modifier.width(34.dp), textAlign = TextAlign.End, style = MaterialTheme.typography.bodySmall, color = CricketPurple)
                Text(
                    text = "${"%.1f".format(b.strikeRate)}",
                    modifier = Modifier.width(52.dp),
                    textAlign = TextAlign.End,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Clip
                )
            }
        }
    }
}

// ── Bowler Panel ──────────────────────────────────────────────────────────────

@Composable
private fun BowlerPanel(score: InningsScore) {
    val currentBowler = score.bowlers.find { it.player.id == score.currentBowlerId } ?: return
    DarkScoreCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(EmeraldContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("⚾", fontSize = 14.sp)
                }
                Column {
                    Text(currentBowler.player.name, fontWeight = FontWeight.Bold, color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
                    Text("Bowling", style = MaterialTheme.typography.labelSmall, color = EmeraldPrimary)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                StatItem("O",   currentBowler.oversDisplay)
                StatItem("R",   "${currentBowler.runs}")
                StatItem("W",   "${currentBowler.wickets}", if (currentBowler.wickets > 0) CricketRed else TextPrimary)
                StatItem("Eco", "${"%.1f".format(currentBowler.economy)}")
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, valueColor: Color = TextPrimary) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, color = valueColor, style = MaterialTheme.typography.bodyMedium)
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
    }
}

// ── Current Over Panel ────────────────────────────────────────────────────────

@Composable
private fun CurrentOverPanel(balls: List<Ball>, completedOvers: Int) {
    val currentOverBalls = balls.filter { it.overNumber == completedOvers }
    if (currentOverBalls.isEmpty() && completedOvers == 0) return

    DarkScoreCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
        Text(
            "Over ${completedOvers + 1}",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = TextSecondary
        )
        Spacer(Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(currentOverBalls) { ball ->
                BallDot(ball)
            }
        }
    }
}

@Composable
private fun BallDot(ball: Ball) {
    val (label, bg, fg) = when {
        ball.isWicket                          -> Triple("W",         CricketRed,    Color.White)
        ball.extraType == ExtraType.WIDE       -> Triple("Wd",        Color(0xFFFF9800), Color.Black)
        ball.extraType == ExtraType.NO_BALL    -> Triple("Nb",        Color(0xFFFF5722), Color.White)
        ball.extraType == ExtraType.BYE        -> Triple("${ball.extras}b", Color(0xFF546E7A), Color.White)
        ball.extraType == ExtraType.LEG_BYE    -> Triple("${ball.extras}lb", Color(0xFF607D8B), Color.White)
        ball.runs == 0                         -> Triple("·",         DarkSurface2,  TextSecondary)
        ball.runs == 4                         -> Triple("4",         CricketBlue,   Color.White)
        ball.runs == 6                         -> Triple("6",         CricketPurple, Color.White)
        else                                   -> Triple("${ball.runs}", EmeraldContainer, EmeraldPrimary)
    }
    Box(
        modifier = Modifier
            .size(38.dp)
            .background(bg, CircleShape)
            .border(1.dp, fg.copy(alpha = 0.3f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = fg, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

// ── Scoring Buttons ───────────────────────────────────────────────────────────

@Composable
private fun ScoringButtons(
    activeBatsmen: List<BatsmanScore>,
    bowlingTeamPlayers: List<Player>,
    currentBowlerId: String,
    onRun: (Int) -> Unit,
    onExtra: (ExtraType, Int) -> Unit,
    onWicket: (WicketType, String?, List<String>) -> Unit,
    onUndo: () -> Unit
) {
    var showWicketDialog by remember { mutableStateOf(false) }
    var showLegByeDialog by remember { mutableStateOf(false) }
    var showCaughtDialog by remember { mutableStateOf(false) }
    var showStumpedDialog by remember { mutableStateOf(false) }
    var showRunOutBatsmanDialog by remember { mutableStateOf(false) }
    var showRunOutFielderDialog by remember { mutableStateOf(false) }
    var pendingRunOutDismissedId by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Run buttons
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(0, 1, 2, 3, 4, 6).forEach { runs ->
                val (bg, fg, border) = when (runs) {
                    4    -> Triple(CricketBlue.copy(alpha = 0.15f),    CricketBlue,    CricketBlue.copy(alpha = 0.5f))
                    6    -> Triple(CricketPurple.copy(alpha = 0.15f),  CricketPurple,  CricketPurple.copy(alpha = 0.5f))
                    0    -> Triple(DarkSurface2,                        TextSecondary,  OutlineColor)
                    else -> Triple(EmeraldContainer,                    EmeraldPrimary, EmeraldPrimary.copy(alpha = 0.4f))
                }
                RunButton(
                    label = if (runs == 0) "·" else "$runs",
                    bg = bg, fg = fg, borderColor = border,
                    modifier = Modifier.weight(1f).height(54.dp),
                    onClick = { onRun(runs) }
                )
            }
        }

        // Extras row
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(
                "Wide"  to { onExtra(ExtraType.WIDE, 0) },
                "No-B"  to { onExtra(ExtraType.NO_BALL, 0) },
                "Bye"   to { onExtra(ExtraType.BYE, 1) },
                "Leg-B" to { showLegByeDialog = true }
            ).forEach { (label, action) ->
                OutlinedButton(
                    onClick = action,
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, OutlineColor),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = TextSecondary,
                        containerColor = DarkSurface
                    )
                ) {
                    Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Wicket + Undo
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { showWicketDialog = true },
                modifier = Modifier.weight(2f).height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CricketRedDim,
                    contentColor = CricketRed
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, CricketRed.copy(alpha = 0.5f))
            ) {
                Text("🚨  WICKET", fontWeight = FontWeight.Black, fontSize = 14.sp, letterSpacing = 0.5.sp)
            }
            OutlinedButton(
                onClick = onUndo,
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, OutlineColor),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = TextSecondary,
                    containerColor = DarkSurface
                )
            ) {
                Text("↩ Undo", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }

    if (showWicketDialog) {
        WicketTypeDialog(
            onWicket = { type ->
                showWicketDialog = false
                when (type) {
                    WicketType.CAUGHT  -> showCaughtDialog = true
                    WicketType.STUMPED -> showStumpedDialog = true
                    WicketType.RUN_OUT -> showRunOutBatsmanDialog = true
                    else -> onWicket(type, null, emptyList())
                }
            },
            onDismiss = { showWicketDialog = false }
        )
    }
    if (showLegByeDialog) {
        LegByeRunDialog(
            onRuns = { runs ->
                onExtra(ExtraType.LEG_BYE, runs)
                showLegByeDialog = false
            },
            onDismiss = { showLegByeDialog = false }
        )
    }
    if (showCaughtDialog) {
        CaughtFielderDialog(
            bowlingTeamPlayers = bowlingTeamPlayers,
            currentBowlerId = currentBowlerId,
            onCaught = { catcherId ->
                onWicket(WicketType.CAUGHT, null, if (catcherId == null) emptyList() else listOf(catcherId))
                showCaughtDialog = false
            },
            onDismiss = { showCaughtDialog = false }
        )
    }
    if (showStumpedDialog) {
        StumpedFielderDialog(
            bowlingTeamPlayers = bowlingTeamPlayers,
            onStumped = { keeperId ->
                onWicket(WicketType.STUMPED, null, listOf(keeperId))
                showStumpedDialog = false
            },
            onDismiss = { showStumpedDialog = false }
        )
    }
    if (showRunOutBatsmanDialog) {
        RunOutBatsmanDialog(
            activeBatsmen = activeBatsmen,
            onDismissed = { playerId ->
                pendingRunOutDismissedId = playerId
                showRunOutBatsmanDialog = false
                showRunOutFielderDialog = true
            },
            onDismiss = { showRunOutBatsmanDialog = false }
        )
    }
    if (showRunOutFielderDialog) {
        RunOutFielderDialog(
            bowlingTeamPlayers = bowlingTeamPlayers,
            onConfirm = { fielderIds ->
                onWicket(WicketType.RUN_OUT, pendingRunOutDismissedId.takeIf { it.isNotEmpty() }, fielderIds)
                showRunOutFielderDialog = false
                pendingRunOutDismissedId = ""
            },
            onDismiss = { showRunOutFielderDialog = false }
        )
    }
}

@Composable
private fun RunButton(
    label: String,
    bg: Color,
    fg: Color,
    borderColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
    ) {
        TextButton(
            onClick = onClick,
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(label, fontWeight = FontWeight.Black, fontSize = 20.sp, color = fg)
        }
    }
}

// ── Dialogs ───────────────────────────────────────────────────────────────────

@Composable
private fun WicketTypeDialog(onWicket: (WicketType) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary,
        title = { Text("How Out? 🚨", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf(
                    WicketType.BOWLED    to "Bowled",
                    WicketType.CAUGHT    to "Caught",
                    WicketType.LBW       to "LBW",
                    WicketType.RUN_OUT   to "Run Out",
                    WicketType.STUMPED   to "Stumped",
                    WicketType.HIT_WICKET to "Hit Wicket"
                ).forEach { (type, label) ->
                    TextButton(
                        onClick = { onWicket(type) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkSurface2, RoundedCornerShape(8.dp))
                    ) {
                        Text(label, color = CricketRed, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        }
    )
}

@Composable
private fun LegByeRunDialog(onRuns: (Int) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        titleContentColor = TextPrimary,
        title = { Text("Leg Bye Runs", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("How many leg byes?", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(1, 2, 3, 4, 5, 6).forEach { runs ->
                        OutlinedButton(
                            onClick = { onRuns(runs) },
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, OutlineColor),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary, containerColor = DarkSurface2)
                        ) {
                            Text("$runs", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        }
    )
}

@Composable
private fun RunOutBatsmanDialog(
    activeBatsmen: List<BatsmanScore>,
    onDismissed: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        titleContentColor = TextPrimary,
        title = { Text("Run Out — Who's Out?", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                activeBatsmen.forEach { b ->
                    val endLabel = if (b.isOnStrike) "Striker End" else "Non-Striker End"
                    val endColor = if (b.isOnStrike) EmeraldPrimary else GoldPrimary
                    TextButton(
                        onClick = { onDismissed(b.player.id) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkSurface2, RoundedCornerShape(8.dp))
                    ) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(b.player.name, color = CricketRed, fontWeight = FontWeight.SemiBold)
                            Text(endLabel, color = endColor, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        }
    )
}

@Composable
private fun CaughtFielderDialog(
    bowlingTeamPlayers: List<Player>,
    currentBowlerId: String,
    onCaught: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        titleContentColor = TextPrimary,
        title = { Text("Caught By?", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                // c & b — bowler catches own ball
                val bowlerName = bowlingTeamPlayers.find { it.id == currentBowlerId }?.name ?: "Bowler"
                TextButton(
                    onClick = { onCaught(null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(EmeraldContainer, RoundedCornerShape(8.dp))
                ) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("c & b  $bowlerName", color = EmeraldPrimary, fontWeight = FontWeight.Bold)
                        Text("Bowler", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                    }
                }
                HorizontalDivider(color = DividerColor)
                bowlingTeamPlayers.forEach { p ->
                    TextButton(
                        onClick = { onCaught(p.id) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkSurface2, RoundedCornerShape(8.dp))
                    ) {
                        Text(p.name, color = TextPrimary, fontWeight = FontWeight.SemiBold, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        }
    )
}

@Composable
private fun StumpedFielderDialog(
    bowlingTeamPlayers: List<Player>,
    onStumped: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        titleContentColor = TextPrimary,
        title = { Text("Stumped By? (Keeper)", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                bowlingTeamPlayers.forEach { p ->
                    TextButton(
                        onClick = { onStumped(p.id) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkSurface2, RoundedCornerShape(8.dp))
                    ) {
                        Text(p.name, color = TextPrimary, fontWeight = FontWeight.SemiBold, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        }
    )
}

@Composable
private fun RunOutFielderDialog(
    bowlingTeamPlayers: List<Player>,
    onConfirm: (List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    val selected = remember { mutableStateListOf<String>() }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        titleContentColor = TextPrimary,
        title = {
            Column {
                Text("Fielder(s) Involved?", fontWeight = FontWeight.Bold)
                Text("Select 1 or 2 · e.g. Jadeja/Dhoni",
                    style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                bowlingTeamPlayers.forEach { p ->
                    val isSelected = p.id in selected
                    TextButton(
                        onClick = {
                            if (isSelected) {
                                selected.remove(p.id)
                            } else {
                                if (selected.size >= 2) selected.removeAt(0)
                                selected.add(p.id)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (isSelected) EmeraldContainer else DarkSurface2,
                                RoundedCornerShape(8.dp)
                            )
                    ) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(p.name,
                                color = if (isSelected) EmeraldPrimary else TextPrimary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                            if (isSelected) {
                                Text("✓", color = EmeraldPrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { onConfirm(emptyList()) }) {
                    Text("Skip", color = TextSecondary)
                }
                Button(
                    onClick = { onConfirm(selected.toList()) },
                    enabled = selected.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.Black)
                ) {
                    Text("Confirm", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        }
    )
}

@Composable
private fun PlayerSelectionDialog(
    title: String,
    players: List<Player>,
    onSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedId by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        titleContentColor = TextPrimary,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            DarkPlayerDropdown(
                label = title,
                players = players,
                selectedId = selectedId,
                onSelected = { selectedId = it }
            )
        },
        confirmButton = {
            Button(
                onClick = { if (selectedId.isNotEmpty()) onSelected(selectedId) },
                enabled = selectedId.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldPrimary,
                    contentColor = Color.Black
                )
            ) { Text("Confirm", fontWeight = FontWeight.Bold) }
        },
        dismissButton = null
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StartInnings2Dialog(
    match: Match,
    players: List<Player>,
    innings1Score: InningsScore?,
    onStart: (String, String, String) -> Unit
) {
    val battingTeam2Num = match.innings2BattingTeam
    val bowlingTeam2Num = if (battingTeam2Num == 1) 2 else 1
    val batters  = players.filter { it.team == battingTeam2Num }
    val bowlers  = players.filter { it.team == bowlingTeam2Num }

    var opener1 by remember { mutableStateOf("") }
    var opener2 by remember { mutableStateOf("") }
    var bowler  by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = {},
        containerColor = DarkSurface,
        titleContentColor = TextPrimary,
        title = { Text("🏏 Innings 1 Complete!", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                innings1Score?.let {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(EmeraldContainer, RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                "${it.battingTeamName}: ${it.totalRuns}/${it.wickets} (${it.oversDisplay})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                "Target: ${it.totalRuns + 1}",
                                color = GoldPrimary,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Text(
                    "Set up ${match.battingTeamName(2)}'s innings:",
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
                DarkPlayerDropdown("Striker",        batters, opener1, opener2) { opener1 = it }
                DarkPlayerDropdown("Non-Striker",     batters, opener2, opener1) { opener2 = it }
                DarkPlayerDropdown("Opening Bowler",  bowlers, bowler)           { bowler  = it }
            }
        },
        confirmButton = {
            Button(
                onClick = { onStart(opener1, opener2, bowler) },
                enabled = opener1.isNotEmpty() && opener2.isNotEmpty() && bowler.isNotEmpty() && opener1 != opener2,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.Black)
            ) { Text("Start Innings 2", fontWeight = FontWeight.Bold) }
        },
        dismissButton = null
    )
}

@Composable
private fun MatchCompleteDialog(result: String, onDone: () -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        containerColor = DarkSurface,
        titleContentColor = TextPrimary,
        title = { Text("🏆 Match Complete!", fontWeight = FontWeight.Bold) },
        text = {
            Text(
                result,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = GoldPrimary
            )
        },
        confirmButton = {
            Button(
                onClick = onDone,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.Black)
            ) { Text("View Summary", fontWeight = FontWeight.Bold) }
        },
        dismissButton = null
    )
}

// ── Add Player Dialog ─────────────────────────────────────────────────────────

@Composable
private fun AddPlayerDialog(
    team1Name: String,
    team2Name: String,
    onAdd: (name: String, team: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedTeam by remember { mutableStateOf(1) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        titleContentColor = TextPrimary,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.PersonAdd, contentDescription = null, tint = EmeraldPrimary)
                Text("Add Player", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {

                // Player name input
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Player Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = EmeraldPrimary,
                        unfocusedBorderColor = OutlineColor,
                        focusedLabelColor    = EmeraldPrimary,
                        unfocusedLabelColor  = TextSecondary,
                        cursorColor          = EmeraldPrimary,
                        focusedTextColor     = TextPrimary,
                        unfocusedTextColor   = TextPrimary
                    )
                )

                // Team selection toggle
                Text("Select Team", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(1 to team1Name, 2 to team2Name).forEach { (teamNum, teamName) ->
                        val selected = selectedTeam == teamNum
                        Button(
                            onClick = { selectedTeam = teamNum },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selected) EmeraldContainer else DarkSurface2,
                                contentColor   = if (selected) EmeraldPrimary   else TextSecondary
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (selected) EmeraldPrimary else OutlineColor
                            )
                        ) {
                            Text(
                                teamName,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick  = { onAdd(name, selectedTeam) },
                enabled  = name.isNotBlank(),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = EmeraldPrimary,
                    contentColor   = Color.Black
                )
            ) {
                Text("Add Player", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

// ── Helper ─────────────────────────────────────────────────────────────────────

@Composable
private fun DarkScoreCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, OutlineColor)
    ) {
        Column(modifier = Modifier.padding(12.dp), content = content)
    }
}
