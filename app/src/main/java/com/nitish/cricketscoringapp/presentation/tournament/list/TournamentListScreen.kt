package com.nitish.cricketscoringapp.presentation.tournament.list

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.SportsCricket
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import com.nitish.cricketscoringapp.domain.model.Tournament
import com.nitish.cricketscoringapp.domain.model.TournamentStatus
import com.nitish.cricketscoringapp.ui.theme.CricketRed
import com.nitish.cricketscoringapp.ui.theme.DarkBg
import com.nitish.cricketscoringapp.ui.theme.DarkSurface
import com.nitish.cricketscoringapp.ui.theme.DarkSurface2
import com.nitish.cricketscoringapp.ui.theme.DividerColor
import com.nitish.cricketscoringapp.ui.theme.DoneGreen
import com.nitish.cricketscoringapp.ui.theme.EmeraldContainer
import com.nitish.cricketscoringapp.ui.theme.EmeraldPrimary
import com.nitish.cricketscoringapp.ui.theme.GoldContainer
import com.nitish.cricketscoringapp.ui.theme.GoldPrimary
import com.nitish.cricketscoringapp.ui.theme.LiveRed
import com.nitish.cricketscoringapp.ui.theme.OutlineColor
import com.nitish.cricketscoringapp.ui.theme.TextPrimary
import com.nitish.cricketscoringapp.ui.theme.TextSecondary
import com.nitish.cricketscoringapp.ui.theme.TextTertiary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentListScreen(
    onBack: () -> Unit,
    onCreateTournament: () -> Unit,
    onTournamentClick: (String) -> Unit,
    viewModel: TournamentListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var deleteTarget by remember { mutableStateOf<Tournament?>(null) }

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    brush = Brush.linearGradient(listOf(GoldPrimary, Color(0xFFE65100))),
                                    shape = RoundedCornerShape(9.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            "Tournaments",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = TextPrimary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextSecondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBg,
                    titleContentColor = TextPrimary
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateTournament,
                containerColor = GoldPrimary,
                contentColor = Color.Black,
                shape = RoundedCornerShape(16.dp),
                icon = { Icon(Icons.Default.Add, null, modifier = Modifier.size(20.dp)) },
                text = { Text("New Tournament", fontWeight = FontWeight.Bold, fontSize = 14.sp) }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = GoldPrimary)
                }
            }

            uiState.tournaments.isEmpty() -> {
                TournamentEmptyState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    onCreateTournament = onCreateTournament
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(DarkBg)
                        .padding(padding),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = 120.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.tournaments, key = { it.id }) { tournament ->
                        TournamentCard(
                            tournament = tournament,
                            onClick = { onTournamentClick(tournament.id) },
                            onDelete = { deleteTarget = tournament }
                        )
                    }
                }
            }
        }
    }

    deleteTarget?.let { t ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            containerColor = DarkSurface,
            titleContentColor = TextPrimary,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Delete, null, tint = CricketRed, modifier = Modifier.size(20.dp))
                    Text("Delete Tournament", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    "Delete \"${t.name}\"? This will remove all teams, players, and fixtures. This action cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteTournament(t.id)
                        deleteTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CricketRed)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

// ── Tournament Card ───────────────────────────────────────────────────────────

@Composable
private fun TournamentCard(
    tournament: Tournament,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val (statusColor, statusBg, statusLabel) = when (tournament.status) {
        TournamentStatus.UPCOMING  -> Triple(GoldPrimary, GoldContainer, "UPCOMING")
        TournamentStatus.ONGOING   -> Triple(LiveRed, Color(0x1FFF4444), "LIVE")
        TournamentStatus.COMPLETED -> Triple(DoneGreen, EmeraldContainer, "DONE")
        TournamentStatus.CANCELLED -> Triple(TextTertiary, DarkSurface2, "CANCELLED")
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurface)
            .border(1.dp, OutlineColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        // Left accent stripe
        Box(
            modifier = Modifier
                .width(3.dp)
                .matchParentSize()
                .background(
                    Brush.verticalGradient(listOf(statusColor, statusColor.copy(alpha = 0.2f)))
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 14.dp, end = 14.dp, bottom = 14.dp)
        ) {
            // Header: status badge + delete button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusPill(label = statusLabel, color = statusColor, bgColor = statusBg)
                    TypePill(label = tournament.tournamentType.label.substringBefore(" "))
                    FormatPill(label = tournament.matchFormat.label)
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = TextTertiary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Tournament name
            Text(
                tournament.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (tournament.organizerName.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    "by ${tournament.organizerName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = DividerColor, thickness = 0.5.dp)
            Spacer(Modifier.height(10.dp))

            // Footer: dates + teams
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = TextTertiary,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        formatDateRange(tournament.startDate, tournament.endDate),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.Group,
                        contentDescription = null,
                        tint = TextTertiary,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        "Max ${tournament.maxTeams} teams",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
                if (tournament.venue.isNotBlank()) {
                    Text(
                        tournament.venue,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }
            }
        }
    }
}

// ── Pill Labels ───────────────────────────────────────────────────────────────

@Composable
private fun StatusPill(label: String, color: Color, bgColor: Color) {
    Box(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(5.dp))
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(5.dp))
            .padding(horizontal = 7.dp, vertical = 2.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp,
            letterSpacing = 0.8.sp
        )
    }
}

@Composable
private fun TypePill(label: String) {
    Box(
        modifier = Modifier
            .background(DarkSurface2, RoundedCornerShape(5.dp))
            .border(1.dp, OutlineColor, RoundedCornerShape(5.dp))
            .padding(horizontal = 7.dp, vertical = 2.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            fontSize = 9.sp
        )
    }
}

@Composable
private fun FormatPill(label: String) {
    Box(
        modifier = Modifier
            .background(EmeraldContainer, RoundedCornerShape(5.dp))
            .border(1.dp, EmeraldPrimary.copy(alpha = 0.3f), RoundedCornerShape(5.dp))
            .padding(horizontal = 7.dp, vertical = 2.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = EmeraldPrimary,
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ── Empty State ───────────────────────────────────────────────────────────────

@Composable
private fun TournamentEmptyState(
    modifier: Modifier = Modifier,
    onCreateTournament: () -> Unit
) {
    Column(
        modifier = modifier.padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(GoldContainer, CircleShape)
                .border(2.dp, GoldPrimary.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = GoldPrimary,
                modifier = Modifier.size(52.dp)
            )
        }
        Spacer(Modifier.height(20.dp))
        Text(
            "No Tournaments Yet",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Create your first cricket tournament and manage teams, fixtures, and live scores — all in one place.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(28.dp))
        Button(
            onClick = onCreateTournament,
            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 28.dp, vertical = 12.dp)
        ) {
            Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Create Tournament", fontWeight = FontWeight.Bold)
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private val dateFormat = SimpleDateFormat("d MMM", Locale.getDefault())

private fun formatDateRange(start: Long, end: Long): String {
    val s = dateFormat.format(Date(start))
    val e = dateFormat.format(Date(end))
    return "$s – $e"
}
