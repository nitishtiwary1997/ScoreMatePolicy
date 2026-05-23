package com.nitish.cricketscoringapp.presentation.livescore

import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.material.icons.filled.SportsCricket
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nitish.cricketscoringapp.domain.model.LiveScoreSnapshot
import com.nitish.cricketscoringapp.ui.theme.EmeraldPrimary
import com.nitish.cricketscoringapp.ui.theme.GoldPrimary
import com.nitish.cricketscoringapp.ui.theme.LiveRed
import com.nitish.cricketscoringapp.ui.theme.LocalAppColors

private val BluePrimary   = Color(0xFF2979FF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveScoreScreen(
    onBack: () -> Unit,
    viewModel: LiveScoreViewModel = hiltViewModel()
) {
    val c = LocalAppColors.current
    val snapshot by viewModel.snapshot.collectAsState()

    Scaffold(
        containerColor = c.bg,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        LivePulseDot()
                        Text("Live Score", color = c.textPrimary, fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = c.textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = c.surface)
            )
        }
    ) { padding ->
        when {
            snapshot == null -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Default.SportsCricket, contentDescription = null, tint = c.textSecondary, modifier = Modifier.size(48.dp))
                        Text("Waiting for live score...", color = c.textSecondary, fontSize = 15.sp)
                        Text("Data will appear once the match starts.", color = c.textSecondary.copy(alpha = 0.6f), fontSize = 12.sp)
                    }
                }
            }
            else -> {
                val s = snapshot!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ScoreHeaderCard(s)
                    if (s.currentInnings == 2 && s.target > 0) {
                        TargetInfoCard(s)
                    }
                    BattingCard(s)
                    BowlingCard(s)
                    if (s.lastBallDesc.isNotBlank()) {
                        LastBallCard(s.lastBallDesc)
                    }
                    RunRateCard(s)
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun LivePulseDot() {
    val transition = rememberInfiniteTransition(label = "pulse")
    val alpha by transition.animateFloat(
        initialValue = 0.3f, targetValue = 1f, label = "alpha",
        animationSpec = infiniteRepeatable(tween(700, easing = LinearEasing), RepeatMode.Reverse)
    )
    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(LiveRed)
            .alpha(alpha)
    )
}

@Composable
private fun ScoreHeaderCard(s: LiveScoreSnapshot) {
    val c = LocalAppColors.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(listOf(Color(0xFF1A2B1A), c.surface))
            )
            .border(1.dp, EmeraldPrimary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(20.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Team names
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(s.team1Name, color = c.textPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.weight(1f))
                Text("vs", color = c.textSecondary, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 8.dp))
                Text(s.team2Name, color = c.textPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
            }

            HorizontalDivider(thickness = 0.5.dp, color = c.outline)

            // Innings 1 score
            InningsScoreLine(
                label = "1st: ${s.inn1BattingTeam}",
                score = "${s.inn1Runs}/${s.inn1Wickets}",
                overs = s.inn1Overs,
                isActive = s.currentInnings == 1
            )

            // Innings 2 score (if started)
            if (s.currentInnings == 2 || s.inn2Runs > 0 || s.inn2Wickets > 0) {
                InningsScoreLine(
                    label = "2nd: ${s.inn2BattingTeam}",
                    score = "${s.inn2Runs}/${s.inn2Wickets}",
                    overs = s.inn2Overs,
                    isActive = s.currentInnings == 2
                )
            }
        }
    }
}

@Composable
private fun InningsScoreLine(label: String, score: String, overs: String, isActive: Boolean) {
    val c = LocalAppColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = if (isActive) c.textPrimary else c.textSecondary, fontSize = 13.sp)
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                score,
                color = if (isActive) EmeraldPrimary else c.textSecondary,
                fontSize = if (isActive) 28.sp else 18.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text("($overs ov)", color = c.textSecondary, fontSize = 12.sp)
        }
    }
}

@Composable
private fun TargetInfoCard(s: LiveScoreSnapshot) {
    val c = LocalAppColors.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(GoldPrimary.copy(alpha = 0.08f))
            .border(1.dp, GoldPrimary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            TargetInfoCell("Target", "${s.target}")
            TargetInfoCell("Need", "${s.requiredRuns} off ${s.requiredBalls}b")
            TargetInfoCell("Req RR", s.requiredRunRate)
        }
    }
}

@Composable
private fun TargetInfoCell(label: String, value: String) {
    val c = LocalAppColors.current
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, color = c.textSecondary, fontSize = 11.sp)
        Text(value, color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
private fun BattingCard(s: LiveScoreSnapshot) {
    val c = LocalAppColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(c.surface)
    ) {
        SectionHeader("Batting", EmeraldPrimary)
        // Header row
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Batter", color = c.textSecondary, fontSize = 11.sp, modifier = Modifier.weight(1f))
            listOf("R", "B", "4s", "6s", "SR").forEach {
                Text(it, color = c.textSecondary, fontSize = 11.sp, modifier = Modifier.width(30.dp), textAlign = TextAlign.End)
            }
        }
        HorizontalDivider(thickness = 0.5.dp, color = c.outline)

        if (s.strikerName.isNotBlank()) {
            BatterRow(
                name = s.strikerName,
                runs = s.strikerRuns,
                balls = s.strikerBalls,
                fours = s.strikerFours,
                sixes = s.strikerSixes,
                sr = s.strikerSR,
                onStrike = true
            )
        }
        if (s.nonStrikerName.isNotBlank()) {
            BatterRow(
                name = s.nonStrikerName,
                runs = s.nonStrikerRuns,
                balls = s.nonStrikerBalls,
                fours = 0,
                sixes = 0,
                sr = "",
                onStrike = false
            )
        }
        if (s.strikerName.isBlank() && s.nonStrikerName.isBlank()) {
            Text("  —", color = c.textSecondary, fontSize = 13.sp, modifier = Modifier.padding(16.dp))
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun BatterRow(name: String, runs: Int, balls: Int, fours: Int, sixes: Int, sr: String, onStrike: Boolean) {
    val c = LocalAppColors.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            if (onStrike) {
                Box(Modifier.size(6.dp).clip(CircleShape).background(EmeraldPrimary))
            }
            Text(
                name,
                color = if (onStrike) c.textPrimary else c.textSecondary,
                fontSize = 13.sp,
                fontWeight = if (onStrike) FontWeight.Bold else FontWeight.Normal
            )
        }
        listOf("$runs", "$balls", if (onStrike) "$fours" else "-", if (onStrike) "$sixes" else "-", sr.ifBlank { "-" }).forEach { v ->
            Text(v, color = if (onStrike) EmeraldPrimary else c.textSecondary, fontSize = 13.sp, modifier = Modifier.width(30.dp), textAlign = TextAlign.End,
                fontWeight = if (onStrike) FontWeight.Bold else FontWeight.Normal)
        }
    }
}

@Composable
private fun BowlingCard(s: LiveScoreSnapshot) {
    val c = LocalAppColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(c.surface)
    ) {
        SectionHeader("Bowling", BluePrimary)
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Bowler", color = c.textSecondary, fontSize = 11.sp, modifier = Modifier.weight(1f))
            listOf("O", "R", "W", "Econ").forEach {
                Text(it, color = c.textSecondary, fontSize = 11.sp, modifier = Modifier.width(36.dp), textAlign = TextAlign.End)
            }
        }
        HorizontalDivider(thickness = 0.5.dp, color = c.outline)
        if (s.bowlerName.isNotBlank()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(6.dp).clip(CircleShape).background(BluePrimary))
                    Text(s.bowlerName, color = c.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                listOf(s.bowlerOvers, "${s.bowlerRuns}", "${s.bowlerWickets}", s.bowlerEcon).forEach { v ->
                    Text(v, color = BluePrimary, fontSize = 13.sp, modifier = Modifier.width(36.dp), textAlign = TextAlign.End, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            Text("  —", color = c.textSecondary, fontSize = 13.sp, modifier = Modifier.padding(16.dp))
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun LastBallCard(desc: String) {
    val c = LocalAppColors.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(c.surface2)
            .border(1.dp, c.outline, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Last ball:", color = c.textSecondary, fontSize = 12.sp)
            Text(desc, color = c.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun RunRateCard(s: LiveScoreSnapshot) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        RunRateCell("Current RR", s.currentRunRate, EmeraldPrimary, Modifier.weight(1f))
        if (s.currentInnings == 2 && s.requiredRunRate != "-") {
            RunRateCell("Required RR", s.requiredRunRate, GoldPrimary, Modifier.weight(1f))
        }
    }
}

@Composable
private fun RunRateCell(label: String, value: String, color: Color, modifier: Modifier) {
    val c = LocalAppColors.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(c.surface)
            .padding(14.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, color = c.textSecondary, fontSize = 11.sp)
            Text(value, color = color, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun SectionHeader(title: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color.copy(alpha = 0.08f))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(Modifier.width(3.dp).height(14.dp).clip(RoundedCornerShape(1.5.dp)).background(color))
        Text(title, color = color, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}
