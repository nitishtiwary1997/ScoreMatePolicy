package com.nitish.cricketscoringapp.presentation.tournament.create

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nitish.cricketscoringapp.domain.model.BallType
import com.nitish.cricketscoringapp.domain.model.TournamentMatchFormat
import com.nitish.cricketscoringapp.domain.model.TournamentType
import com.nitish.cricketscoringapp.ui.theme.DarkBg
import com.nitish.cricketscoringapp.ui.theme.DarkSurface
import com.nitish.cricketscoringapp.ui.theme.DarkSurface2
import com.nitish.cricketscoringapp.ui.theme.DividerColor
import com.nitish.cricketscoringapp.ui.theme.EmeraldDark
import com.nitish.cricketscoringapp.ui.theme.EmeraldPrimary
import com.nitish.cricketscoringapp.ui.theme.GoldPrimary
import com.nitish.cricketscoringapp.ui.theme.OutlineColor
import com.nitish.cricketscoringapp.ui.theme.TextPrimary
import com.nitish.cricketscoringapp.ui.theme.TextSecondary
import com.nitish.cricketscoringapp.ui.theme.TextTertiary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val STEP_TITLES = listOf("Basic Info", "Match Settings", "Details")
private val STEP_ICONS  = listOf(Icons.Default.EmojiEvents, Icons.Default.Settings, Icons.Default.Info)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTournamentScreen(
    onTournamentCreated: (tournamentId: String) -> Unit,
    onBack: () -> Unit,
    viewModel: CreateTournamentViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Navigate away once tournament is created
    LaunchedEffect(state.createdTournamentId) {
        state.createdTournamentId?.let { onTournamentCreated(it) }
    }

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Create Tournament",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (state.currentStep == 0) onBack() else viewModel.prevStep()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        },
        bottomBar = {
            StepNavBar(
                currentStep  = state.currentStep,
                totalSteps   = 3,
                isLastStep   = state.currentStep == 2,
                isCreating   = state.isCreating,
                onBack       = { if (state.currentStep == 0) onBack() else viewModel.prevStep() },
                onNext       = viewModel::nextStep
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBg)
                .padding(padding)
        ) {
            StepIndicator(currentStep = state.currentStep, totalSteps = 3)

            // Error banner
            state.error?.let { err ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF4D1010))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(err, color = Color(0xFFFF6B6B), fontSize = 13.sp, modifier = Modifier.weight(1f))
                    TextButton(onClick = viewModel::clearError) { Text("OK", color = EmeraldPrimary) }
                }
            }

            AnimatedContent(
                targetState = state.currentStep,
                transitionSpec = {
                    if (targetState > initialState)
                        (slideInHorizontally { it } + fadeIn()) togetherWith (slideOutHorizontally { -it } + fadeOut())
                    else
                        (slideInHorizontally { -it } + fadeIn()) togetherWith (slideOutHorizontally { it } + fadeOut())
                },
                label = "step"
            ) { step ->
                when (step) {
                    0 -> Step1BasicInfo(state, viewModel)
                    1 -> Step2MatchSettings(state, viewModel)
                    2 -> Step3Details(state, viewModel)
                }
            }
        }
    }
}

// ── Step Indicator ─────────────────────────────────────────────────────────────

@Composable
private fun StepIndicator(currentStep: Int, totalSteps: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalSteps) { index ->
            val isDone    = index < currentStep
            val isCurrent = index == currentStep

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isDone    -> EmeraldPrimary
                            isCurrent -> GoldPrimary
                            else      -> DarkSurface2
                        }
                    )
                    .border(
                        width = if (isCurrent) 2.dp else 0.dp,
                        color = if (isCurrent) GoldPrimary else Color.Transparent,
                        shape = CircleShape
                    )
            ) {
                if (isDone) {
                    Icon(Icons.Default.Check, null, tint = Color.Black, modifier = Modifier.size(18.dp))
                } else {
                    Text(
                        text  = "${index + 1}",
                        color = if (isCurrent) Color.Black else TextTertiary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            if (index < totalSteps - 1) {
                HorizontalDivider(
                    modifier  = Modifier
                        .weight(1f)
                        .padding(horizontal = 6.dp),
                    thickness = 2.dp,
                    color     = if (index < currentStep) EmeraldPrimary else DividerColor
                )
            }
        }
    }

    Text(
        text     = STEP_TITLES[currentStep],
        color    = GoldPrimary,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface)
            .padding(bottom = 12.dp, start = 24.dp)
    )
}

// ── Step 1: Basic Info ─────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Step1BasicInfo(state: CreateTournamentState, vm: CreateTournamentViewModel) {
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker   by remember { mutableStateOf(false) }

    val startPickerState = rememberDatePickerState(initialSelectedDateMillis = state.startDate)
    val endPickerState   = rememberDatePickerState(initialSelectedDateMillis = state.endDate)

    if (showStartPicker) {
        DatePickerDialog(
            onDismissRequest = { showStartPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    startPickerState.selectedDateMillis?.let { vm.updateStartDate(it) }
                    showStartPicker = false
                }) { Text("OK", color = EmeraldPrimary) }
            },
            dismissButton = {
                TextButton(onClick = { showStartPicker = false }) { Text("Cancel", color = TextSecondary) }
            }
        ) { DatePicker(state = startPickerState) }
    }

    if (showEndPicker) {
        DatePickerDialog(
            onDismissRequest = { showEndPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    endPickerState.selectedDateMillis?.let { vm.updateEndDate(it) }
                    showEndPicker = false
                }) { Text("OK", color = EmeraldPrimary) }
            },
            dismissButton = {
                TextButton(onClick = { showEndPicker = false }) { Text("Cancel", color = TextSecondary) }
            }
        ) { DatePicker(state = endPickerState) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        FormTextField(
            value       = state.name,
            onValueChange = vm::updateName,
            label       = "Tournament Name *",
            icon        = Icons.Default.EmojiEvents,
            error       = state.nameError,
            capitalization = KeyboardCapitalization.Words
        )

        FormTextField(
            value       = state.organizerName,
            onValueChange = vm::updateOrganizerName,
            label       = "Organizer Name",
            icon        = Icons.Default.Person,
            capitalization = KeyboardCapitalization.Words
        )

        FormTextField(
            value       = state.organizerContact,
            onValueChange = vm::updateOrganizerContact,
            label       = "Contact Number",
            icon        = Icons.Default.Phone,
            keyboardType = KeyboardType.Phone
        )

        FormTextField(
            value       = state.venue,
            onValueChange = vm::updateVenue,
            label       = "Venue / Location",
            icon        = Icons.Default.LocationOn,
            capitalization = KeyboardCapitalization.Words
        )

        // Date row
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DateField(
                label     = "Start Date",
                dateMs    = state.startDate,
                error     = if (state.dateError != null) "" else null,
                modifier  = Modifier.weight(1f),
                onClick   = { showStartPicker = true }
            )
            DateField(
                label     = "End Date",
                dateMs    = state.endDate,
                error     = state.dateError,
                modifier  = Modifier.weight(1f),
                onClick   = { showEndPicker = true }
            )
        }

        // Public toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(DarkSurface2)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Public Tournament", color = TextPrimary, fontWeight = FontWeight.Medium)
                Text("Anyone can view this tournament", color = TextSecondary, fontSize = 12.sp)
            }
            Switch(
                checked  = state.isPublic,
                onCheckedChange = { vm.togglePublic() },
                colors   = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = EmeraldPrimary)
            )
        }

        Spacer(Modifier.height(80.dp))
    }
}

// ── Step 2: Match Settings ─────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Step2MatchSettings(state: CreateTournamentState, vm: CreateTournamentViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Tournament Type
        SettingsSection(title = "Tournament Format", icon = Icons.Default.EmojiEvents) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TournamentType.entries.forEach { type ->
                    SelectableChip(
                        label     = type.label,
                        selected  = state.tournamentType == type,
                        onClick   = { vm.updateTournamentType(type) }
                    )
                }
            }
        }

        // Match Format
        SettingsSection(title = "Match Format", icon = Icons.Default.SportsCricket) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TournamentMatchFormat.entries.forEach { fmt ->
                    SelectableChip(
                        label    = fmt.label,
                        selected = state.matchFormat == fmt,
                        onClick  = { vm.updateMatchFormat(fmt) }
                    )
                }
            }
            if (state.matchFormat == TournamentMatchFormat.CUSTOM) {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Overs: ", color = TextSecondary, fontSize = 13.sp)
                    Text("${state.customOvers}", color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(Modifier.width(12.dp))
                    Slider(
                        value        = state.customOvers.toFloat(),
                        onValueChange = { vm.updateCustomOvers(it.toInt()) },
                        valueRange   = 5f..50f,
                        steps        = 44,
                        modifier     = Modifier.weight(1f),
                        colors       = SliderDefaults.colors(
                            thumbColor       = GoldPrimary,
                            activeTrackColor = GoldPrimary
                        )
                    )
                }
            }
        }

        // Ball Type
        SettingsSection(title = "Ball Type", icon = Icons.Default.SportsCricket) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BallType.entries.forEach { type ->
                    SelectableChip(
                        label    = type.label,
                        selected = state.ballType == type,
                        onClick  = { vm.updateBallType(type) }
                    )
                }
            }
        }

        // Max Teams
        SettingsSection(title = "Maximum Teams", icon = Icons.Default.Groups) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(4, 6, 8, 12, 16).forEach { n ->
                    SelectableChip(
                        label    = "$n",
                        selected = state.maxTeams == n,
                        onClick  = { vm.updateMaxTeams(n) }
                    )
                }
            }
        }

        // Players Per Team
        SettingsSection(title = "Players Per Team", icon = Icons.Default.Person) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Players: ", color = TextSecondary, fontSize = 13.sp)
                Text("${state.playersPerTeam}", color = EmeraldPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.width(12.dp))
                Slider(
                    value         = state.playersPerTeam.toFloat(),
                    onValueChange = { vm.updatePlayersPerTeam(it.toInt()) },
                    valueRange    = 5f..11f,
                    steps         = 5,
                    modifier      = Modifier.weight(1f),
                    colors        = SliderDefaults.colors(
                        thumbColor       = EmeraldPrimary,
                        activeTrackColor = EmeraldPrimary
                    )
                )
            }
        }

        Spacer(Modifier.height(80.dp))
    }
}

// ── Step 3: Details ────────────────────────────────────────────────────────────

@Composable
private fun Step3Details(state: CreateTournamentState, vm: CreateTournamentViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "All fields below are optional — you can fill them later.",
            color    = TextSecondary,
            fontSize = 13.sp
        )

        FormTextField(
            value         = state.description,
            onValueChange = vm::updateDescription,
            label         = "Tournament Description",
            icon          = Icons.Default.Info,
            minLines      = 3,
            maxLines      = 5
        )

        FormTextField(
            value         = state.rules,
            onValueChange = vm::updateRules,
            label         = "Rules & Regulations",
            icon          = Icons.Default.Settings,
            minLines      = 3,
            maxLines      = 6
        )

        FormTextField(
            value         = state.entryFeeText,
            onValueChange = vm::updateEntryFee,
            label         = "Entry Fee (₹)",
            icon          = Icons.Default.EmojiEvents,
            keyboardType  = KeyboardType.Decimal
        )

        FormTextField(
            value         = state.prizeDetails,
            onValueChange = vm::updatePrizeDetails,
            label         = "Prize Details",
            icon          = Icons.Default.EmojiEvents,
            minLines      = 2,
            maxLines      = 4
        )

        Spacer(Modifier.height(80.dp))
    }
}

// ── Bottom Navigation Bar ─────────────────────────────────────────────────────

@Composable
private fun StepNavBar(
    currentStep: Int,
    totalSteps: Int,
    isLastStep: Boolean,
    isCreating: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = TextSecondary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(4.dp))
            Text(if (currentStep == 0) "Cancel" else "Back", color = TextSecondary)
        }

        Text(
            "${currentStep + 1} / $totalSteps",
            color = TextTertiary,
            fontSize = 13.sp
        )

        Button(
            onClick  = onNext,
            enabled  = !isCreating,
            colors   = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
            shape    = RoundedCornerShape(12.dp)
        ) {
            if (isCreating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = Color.Black
                )
            } else if (isLastStep) {
                Icon(Icons.Default.Check, null, tint = Color.Black, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Create Tournament", color = Color.Black, fontWeight = FontWeight.Bold)
            } else {
                Text("Next", color = Color.Black, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(4.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color.Black, modifier = Modifier.size(18.dp))
            }
        }
    }
}

// ── Reusable Components ───────────────────────────────────────────────────────

@Composable
private fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    error: String? = null,
    minLines: Int = 1,
    maxLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.None
) {
    OutlinedTextField(
        value         = value,
        onValueChange = onValueChange,
        label         = { Text(label, color = TextSecondary, fontSize = 13.sp) },
        leadingIcon   = { Icon(icon, null, tint = EmeraldPrimary, modifier = Modifier.size(20.dp)) },
        isError       = error != null,
        supportingText = error?.let { { Text(it, color = Color(0xFFFF6B6B), fontSize = 12.sp) } },
        minLines      = minLines,
        maxLines      = maxLines,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, capitalization = capitalization),
        modifier      = Modifier.fillMaxWidth(),
        shape         = RoundedCornerShape(12.dp),
        colors        = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = EmeraldPrimary,
            unfocusedBorderColor = OutlineColor,
            focusedTextColor     = TextPrimary,
            unfocusedTextColor   = TextPrimary,
            cursorColor          = EmeraldPrimary,
            focusedContainerColor   = DarkSurface2,
            unfocusedContainerColor = DarkSurface2
        )
    )
}

@Composable
private fun DateField(
    label: String,
    dateMs: Long,
    error: String?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val fmt = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    Column(modifier = modifier) {
        Text(label, color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(bottom = 4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(DarkSurface2)
                .border(
                    width = 1.dp,
                    color = if (error != null) Color(0xFFFF6B6B) else OutlineColor,
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.CalendarMonth, null, tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(fmt.format(Date(dateMs)), color = TextPrimary, fontSize = 14.sp)
        }
        error?.let {
            if (it.isNotBlank()) Text(it, color = Color(0xFFFF6B6B), fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp, start = 4.dp))
        }
    }
}

@Composable
private fun SelectableChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick  = onClick,
        label    = { Text(label, fontSize = 13.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal) },
        leadingIcon = if (selected) {
            { Icon(Icons.Default.Check, null, modifier = Modifier.size(14.dp)) }
        } else null,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor    = EmeraldDark,
            selectedLabelColor        = EmeraldPrimary,
            selectedLeadingIconColor  = EmeraldPrimary,
            containerColor            = DarkSurface2,
            labelColor                = TextSecondary
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled              = true,
            selected             = selected,
            selectedBorderColor  = EmeraldPrimary,
            borderColor          = OutlineColor,
            borderWidth          = 1.dp,
            selectedBorderWidth  = 1.5.dp
        )
    )
}

@Composable
private fun SettingsSection(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(title, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        }
        content()
    }
}
