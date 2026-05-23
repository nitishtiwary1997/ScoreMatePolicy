package com.nitish.cricketscoringapp.presentation.tournament.bracket

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nitish.cricketscoringapp.domain.model.Fixture
import com.nitish.cricketscoringapp.domain.model.FixtureStage
import com.nitish.cricketscoringapp.domain.model.FixtureStatus
import com.nitish.cricketscoringapp.domain.model.TournamentTeam
import com.nitish.cricketscoringapp.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Locale

private val BluePrimary  = Color(0xFF2979FF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KnockoutBracketScreen(
    onBack: () -> Unit,
    viewModel: KnockoutBracketViewModel = hiltViewModel()
) {
    val c = LocalAppColors.current
    val uiState by viewModel.uiState.collectAsState()
    val extra   by viewModel.extra.collectAsState()
    val snackbarState = remember { SnackbarHostState() }
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(extra.second) {
        extra.second?.let {
            snackbarState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { viewModel.resolveKnockout(it) }
                    showDatePicker = false
                }) { Text("Generate", color = EmeraldPrimary) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = c.textSecondary)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        containerColor = c.bg,
        snackbarHost = { SnackbarHost(snackbarState) },
        topBar = {
            TopAppBar(
                title = { Text("Knockout Bracket", color = c.textPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = c.textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = c.surface)
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Loading...", color = c.textSecondary)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Champion banner
            uiState.bracket?.champion?.let { champ ->
                ChampionBanner(champ)
            }

            // Resolve button for LEAGUE_PLUS_KNOCKOUT
            if (uiState.canResolve) {
                ResolveQualificationCard(
                    isResolving = extra.first,
                    qualifiedCount = uiState.qualifiedCount,
                    onResolve = { showDatePicker = true }
                )
            }

            val bracket = uiState.bracket
            val teamsMap = uiState.teamsMap

            if (!uiState.hasKnockoutFixtures) {
                BracketEmptyState(canResolve = uiState.canResolve, groupComplete = uiState.groupComplete)
            } else {
                // Quarter Finals
                if (bracket?.quarterFinals?.isNotEmpty() == true) {
                    BracketStageSection(
                        title = "Quarter Finals",
                        stageColor = BluePrimary,
                        fixtures = bracket.quarterFinals,
                        teamsMap = teamsMap
                    )
                }

                // Semi Finals
                if (bracket?.semiFinals?.isNotEmpty() == true) {
                    BracketStageSection(
                        title = "Semi Finals",
                        stageColor = GoldPrimary,
                        fixtures = bracket.semiFinals,
                        teamsMap = teamsMap
                    )
                }

                // 3rd Place Playoff
                bracket?.thirdPlacePlayoff?.let { playoff ->
                    BracketStageSection(
                        title = "3rd Place Playoff",
                        stageColor = c.textSecondary,
                        fixtures = listOf(playoff),
                        teamsMap = teamsMap
                    )
                }

                // Final
                bracket?.final?.let { final ->
                    BracketStageSection(
                        title = "Final",
                        stageColor = EmeraldPrimary,
                        fixtures = listOf(final),
                        teamsMap = teamsMap,
                        isFinal = true
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ChampionBanner(champion: TournamentTeam) {
    val c = LocalAppColors.current
    val infiniteTransition = rememberInfiniteTransition(label = "champion")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f, targetValue = 1f, label = "glow",
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse)
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.horizontalGradient(listOf(Color(0xFF7B5E00), Color(0xFFB8860B), Color(0xFF7B5E00))))
            .border(1.dp, GoldPrimary.copy(alpha = glowAlpha), RoundedCornerShape(16.dp))
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(40.dp))
            Text("CHAMPION", color = GoldPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Text(champion.name, color = c.textPrimary, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
            if (champion.shortName.isNotBlank()) {
                Text(champion.shortName, color = GoldPrimary.copy(alpha = 0.8f), fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun ResolveQualificationCard(
    isResolving: Boolean,
    qualifiedCount: Int,
    onResolve: () -> Unit
) {
    val c = LocalAppColors.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(c.surface2)
            .border(1.dp, EmeraldPrimary.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Stars, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(20.dp))
                Text("Group Stage Complete", color = c.textPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Text(
                "$qualifiedCount teams qualified for knockout stage.",
                color = c.textSecondary, fontSize = 13.sp
            )
            Spacer(Modifier.height(4.dp))
            Button(
                onClick = onResolve,
                enabled = !isResolving,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    if (isResolving) "Generating..." else "Generate Knockout Bracket",
                    color = Color.Black, fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun BracketStageSection(
    title: String,
    stageColor: Color,
    fixtures: List<Fixture>,
    teamsMap: Map<String, TournamentTeam>,
    isFinal: Boolean = false
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Stage header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(Modifier.width(4.dp).height(20.dp).clip(RoundedCornerShape(2.dp)).background(stageColor))
            Text(
                title,
                color = stageColor,
                fontSize = if (isFinal) 18.sp else 15.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = if (isFinal) 1.sp else 0.sp
            )
        }
        fixtures.forEach { fixture ->
            BracketMatchCard(fixture = fixture, teamsMap = teamsMap, stageColor = stageColor)
        }
    }
}

@Composable
private fun BracketMatchCard(
    fixture: Fixture,
    teamsMap: Map<String, TournamentTeam>,
    stageColor: Color
) {
    val c = LocalAppColors.current
    val team1 = teamsMap[fixture.team1Id]
    val team2 = teamsMap[fixture.team2Id]
    val isLive = fixture.status == FixtureStatus.LIVE
    val isCompleted = fixture.status == FixtureStatus.COMPLETED

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(c.surface)
            .border(
                width = if (isLive) 1.dp else 0.5.dp,
                color = if (isLive) LiveRed else c.outline,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Status row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusChip(fixture.status)
                Text(
                    formatMatchDate(fixture.scheduledAt),
                    color = c.textSecondary,
                    fontSize = 11.sp
                )
            }

            // Teams row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Team 1
                BracketTeamColumn(
                    team = team1,
                    isWinner = isCompleted && fixture.winnerId == fixture.team1Id,
                    modifier = Modifier.weight(1f)
                )
                // VS divider
                Column(
                    modifier = Modifier.width(40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("VS", color = c.textSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                // Team 2
                BracketTeamColumn(
                    team = team2,
                    isWinner = isCompleted && fixture.winnerId == fixture.team2Id,
                    modifier = Modifier.weight(1f),
                    alignEnd = true
                )
            }

            // Result summary
            if (fixture.resultSummary.isNotBlank()) {
                Text(
                    fixture.resultSummary,
                    color = EmeraldPrimary,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // TBD text when teams not yet determined
            if (team1 == null || team2 == null) {
                Text(
                    "Teams to be determined after qualification",
                    color = c.textSecondary,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun BracketTeamColumn(
    team: TournamentTeam?,
    isWinner: Boolean,
    modifier: Modifier = Modifier,
    alignEnd: Boolean = false
) {
    val c = LocalAppColors.current
    val alignment = if (alignEnd) Alignment.End else Alignment.Start
    Column(modifier = modifier, horizontalAlignment = alignment, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        // Avatar
        val initials = team?.name?.take(2)?.uppercase() ?: "TBD"
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (isWinner) EmeraldPrimary.copy(alpha = 0.2f) else c.surface2)
                .border(1.dp, if (isWinner) EmeraldPrimary else c.outline, CircleShape)
                .align(alignment),
            contentAlignment = Alignment.Center
        ) {
            Text(initials, color = if (isWinner) EmeraldPrimary else c.textSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Text(
            text = team?.name ?: "TBD",
            color = if (isWinner) EmeraldPrimary else c.textPrimary,
            fontSize = 12.sp,
            fontWeight = if (isWinner) FontWeight.Bold else FontWeight.Normal,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = if (alignEnd) TextAlign.End else TextAlign.Start
        )
        if (team?.shortName?.isNotBlank() == true) {
            Text(team.shortName, color = c.textSecondary, fontSize = 10.sp)
        }
    }
}

@Composable
private fun StatusChip(status: FixtureStatus) {
    val c = LocalAppColors.current
    val (bg, fg) = when (status) {
        FixtureStatus.LIVE      -> LiveRed.copy(alpha = 0.15f) to LiveRed
        FixtureStatus.COMPLETED -> EmeraldPrimary.copy(alpha = 0.15f) to EmeraldPrimary
        FixtureStatus.UPCOMING  -> c.outline to c.textSecondary
        else                    -> c.surface2 to c.textSecondary
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bg)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            if (status == FixtureStatus.LIVE) {
                Box(Modifier.size(6.dp).clip(CircleShape).background(LiveRed))
            }
            Text(status.label, color = fg, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun BracketEmptyState(canResolve: Boolean, groupComplete: Boolean) {
    val c = LocalAppColors.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(c.surface)
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(
                Icons.Default.EmojiEvents, contentDescription = null,
                tint = c.textSecondary, modifier = Modifier.size(48.dp)
            )
            Text(
                "Knockout Bracket",
                color = c.textPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp
            )
            Text(
                when {
                    canResolve     -> "Group stage is complete. Generate the bracket above."
                    groupComplete  -> "Knockout stage has not been set up yet."
                    else           -> "Knockout fixtures will appear here once the group stage completes."
                },
                color = c.textSecondary, fontSize = 13.sp, textAlign = TextAlign.Center
            )
        }
    }
}

private fun formatMatchDate(millis: Long): String {
    return try {
        SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(java.util.Date(millis))
    } catch (e: Exception) { "" }
}
