package com.nitish.cricketscoringapp.presentation.rules

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DarkBg        = Color(0xFF0A0E14)
private val DarkSurface   = Color(0xFF131920)
private val DarkSurface2  = Color(0xFF1A2230)
private val EmeraldPrimary = Color(0xFF00C853)
private val GoldPrimary   = Color(0xFFFFD740)
private val BluePrimary   = Color(0xFF2979FF)
private val PurplePrimary = Color(0xFF9C27B0)
private val OrangePrimary = Color(0xFFFF6D00)
private val TextPrimary   = Color(0xFFF0F4FF)
private val TextSecondary = Color(0xFF8A9BB5)
private val OutlineColor  = Color(0xFF2A3A52)

// ── Data model ─────────────────────────────────────────────────────────────

private data class RuleSection(val title: String, val icon: String, val color: Color, val rules: List<String>)
private data class FormatRules(val name: String, val tagline: String, val accentColor: Color, val sections: List<RuleSection>)

// ── Rules content ───────────────────────────────────────────────────────────

private val t20Rules = FormatRules(
    name = "T20",
    tagline = "Twenty20 — Fast, Furious & Explosive",
    accentColor = EmeraldPrimary,
    sections = listOf(
        RuleSection("Format Basics", "📋", EmeraldPrimary, listOf(
            "Each team faces exactly 20 overs (120 balls) per innings.",
            "2 innings per match — one per team.",
            "Innings ends when 10 wickets fall OR 20 overs are completed.",
            "Typical match duration: ~3 hours.",
            "Result determined by highest run total."
        )),
        RuleSection("Power Play", "⚡", GoldPrimary, listOf(
            "Overs 1–6: Mandatory Power Play.",
            "During Power Play, maximum 2 fielders allowed outside the 30-yard circle.",
            "At least 2 fielders must be inside the 15-yard inner circle.",
            "Overs 7–20: Up to 5 fielders allowed outside the 30-yard circle.",
            "Minimum 4 fielders (plus wicket-keeper) must always be inside the 30-yard circle."
        )),
        RuleSection("Bowling Rules", "🏏", BluePrimary, listOf(
            "Maximum 4 overs per bowler — no bowler can exceed this.",
            "No Ball: Bowled above waist height (full toss), front foot overstepping, etc.",
            "Free Hit: On every No Ball, the next delivery is a Free Hit — batsman cannot be dismissed (except run out, handled out, obstructing the field).",
            "Wide: Ball outside the tramlines — 1 extra run + delivery re-bowled.",
            "Beamer (head-high full toss): Instantly declared a No Ball; bowler gets a first and final warning."
        )),
        RuleSection("Batting Rules", "🏏", OrangePrimary, listOf(
            "Batsman must be ready within 90 seconds of the previous wicket falling.",
            "Retired Hurt: Batsman may retire due to injury and return later in the same innings.",
            "Retired Out: Batsman retires without a valid reason — treated as OUT, cannot return.",
            "Obstructing the field, handling the ball, or hitting the ball twice are dismissal modes.",
            "Hit wicket: If a batsman knocks the stumps while playing a shot or setting off for a run."
        )),
        RuleSection("Fielding Rules", "🧤", PurplePrimary, listOf(
            "11 fielders on the field at all times, including the wicket-keeper.",
            "No fielder may field with a helmet placed on the ground.",
            "Fielders must not use artificial devices to field the ball.",
            "A catch off a No Ball does not dismiss the batsman.",
            "Substitutes may field but cannot bat, bowl, or keep wicket."
        )),
        RuleSection("Tie & Super Over", "⚔️", GoldPrimary, listOf(
            "If scores are tied at the end of regulation play, a Super Over is bowled.",
            "Super Over: Each team faces 1 over (max 6 balls); 2 wickets = innings over.",
            "Highest score in the Super Over wins the match.",
            "If the Super Over is also tied, another Super Over is played until a result.",
            "In some knockout tournaments: boundary count tiebreaker may apply."
        )),
        RuleSection("DLS Method", "🌧️", TextSecondary, listOf(
            "Applied when overs are lost due to rain, bad light or other interruptions.",
            "Duckworth-Lewis-Stern (DLS) calculates a revised target based on overs remaining and wickets in hand.",
            "Minimum 5 overs must be bowled for a result to stand.",
            "If target is revised, it is communicated by the third umpire via scoreboard."
        ))
    )
)

private val odiRules = FormatRules(
    name = "One Day",
    tagline = "ODI — 50 Overs of Strategic Cricket",
    accentColor = BluePrimary,
    sections = listOf(
        RuleSection("Format Basics", "📋", BluePrimary, listOf(
            "Each team faces exactly 50 overs (300 balls) per innings.",
            "2 innings per match — one per team.",
            "Innings ends when 10 wickets fall OR 50 overs are completed.",
            "Typical match duration: ~8 hours (day) or pink-ball day-night.",
            "Colored clothing and white ball used; two new balls (one from each end)."
        )),
        RuleSection("Power Play Phases", "⚡", GoldPrimary, listOf(
            "Power Play 1 (Overs 1–10): Mandatory — max 2 fielders outside 30-yard circle.",
            "Power Play 2 (Overs 11–40): Max 4 fielders outside 30-yard circle.",
            "Power Play 3 / Death (Overs 41–50): Max 5 fielders outside 30-yard circle.",
            "At least 4 fielders (plus keeper) must always be inside the 30-yard circle.",
            "Batting and bowling Power Plays (optional 5-over blocks) are no longer in use under current ICC rules."
        )),
        RuleSection("Bowling Rules", "🏏", EmeraldPrimary, listOf(
            "Maximum 10 overs per bowler.",
            "Two new balls used — one from each end, both from the start of the match.",
            "No Ball results in a Free Hit on the very next delivery.",
            "Wide: 1 extra run + re-bowl. Wides are judged more strictly than in Tests.",
            "A bowler can bowl a maximum of 5 consecutive overs before being rested.",
            "Bowler may not bowl consecutive overs."
        )),
        RuleSection("Batting Rules", "🏏", OrangePrimary, listOf(
            "Batsman must be ready within 3 minutes of the previous wicket falling.",
            "Retired Hurt: Permitted; batsman may return later in the same innings.",
            "Retired Out: Counted as a wicket; batsman cannot return.",
            "Handled Ball and Obstructing the Field are valid dismissal modes.",
            "Time Out: New batsman must be ready within 3 min; else dismissed."
        )),
        RuleSection("Fielding Rules", "🧤", PurplePrimary, listOf(
            "11 fielders including wicket-keeper.",
            "Fielding substitutes (Super-subs concept discontinued) — regular substitutes may field only.",
            "No fielder may stand or crouch behind the wicket-keeper.",
            "Fielder inside the 30-yard circle counts toward Power Play restrictions.",
            "Ball becomes soft after ~34 overs; reverse swing becomes possible."
        )),
        RuleSection("Tie & Super Over", "⚔️", GoldPrimary, listOf(
            "If scores are level after both innings, a Super Over decides the match.",
            "Super Over: 1 over per side; highest score wins.",
            "If Super Over is also tied, another Super Over is played (ICC knockout rules) or boundary count applied (some bilateral series).",
            "Bowl-out may be used in rare domestic-format scenarios."
        )),
        RuleSection("DLS & Interruptions", "🌧️", TextSecondary, listOf(
            "DLS method applied for rain/bad light interruptions.",
            "Minimum 20 overs per side must be bowled for a valid result.",
            "If interruption before start: minimum overs may be further reduced.",
            "Reserve Day (knockout matches): Full match replayed on the reserve day if weather permits."
        ))
    )
)

private val testRules = FormatRules(
    name = "Test",
    tagline = "Test Cricket — The Ultimate Examination",
    accentColor = GoldPrimary,
    sections = listOf(
        RuleSection("Format Basics", "📋", GoldPrimary, listOf(
            "Played over a maximum of 5 days (6 hours of play per day).",
            "Target: 90 overs per day.",
            "4 innings total — each team bats twice.",
            "White clothing and red ball (or pink ball in Day-Night Tests).",
            "A match can end in a Win, Draw, or (rarely) a Tie."
        )),
        RuleSection("Innings & Declaration", "📢", EmeraldPrimary, listOf(
            "Innings ends when 10 wickets fall, time runs out, or the captain declares.",
            "Declaration: Batting captain can close the innings at any time to set a target.",
            "Teams often declare to leave enough time to bowl the opposition out.",
            "Forfeiture: A team may forfeit an innings (very rare, usually by agreement).",
            "A result requires at least one complete innings by each team."
        )),
        RuleSection("Follow-On Rule", "🔄", OrangePrimary, listOf(
            "If the first-innings lead is ≥200 runs, the leading team may enforce the Follow-On.",
            "The trailing team must bat again immediately instead of the leading team.",
            "Thresholds: 5-day match = 200 runs | 3-day = 150 | 2-day = 100 | 1-day = 75.",
            "The follow-on is optional — the fielding captain decides.",
            "Teams sometimes choose not to enforce the follow-on to rest their bowlers."
        )),
        RuleSection("New Ball", "🔴", BluePrimary, listOf(
            "A new ball is taken at the start of each innings.",
            "After 80 overs, the bowling team may request a second new ball.",
            "The old ball can be kept if the bowling side prefers.",
            "Ball condition is crucial: reverse swing with the old ball, conventional swing with the new.",
            "Fielding side may not artificially alter the condition of the ball (ball-tampering = penalty)."
        )),
        RuleSection("Bowling Rules", "🏏", PurplePrimary, listOf(
            "No limit on the number of overs any bowler may bowl.",
            "No ball: Front-foot overstepping, beamer above waist, etc.",
            "Free Hit does NOT apply in Test cricket.",
            "Wide: More lenient than in limited-overs cricket; judged as clearly out of reach.",
            "Bowler may not bowl more than 2 consecutive overs from the same end.",
            "Deliberate time-wasting attracts a 5-run penalty."
        )),
        RuleSection("Batting Rules", "🏏", OrangePrimary, listOf(
            "No mandatory batting order change — only dismissals force a new batsman.",
            "Retired Hurt: Allowed; batsman may return in the same innings.",
            "Retired Out: Counts as a wicket; batsman cannot return.",
            "Handled Ball, Obstructing the Field, Hit the Ball Twice: Valid dismissals.",
            "Timed Out: New batsman must arrive within 3 minutes of the previous wicket.",
            "Night watchman: Lower-order batsman sent in near close of play to protect recognized batsmen."
        )),
        RuleSection("Fielding Rules", "🧤", TextSecondary, listOf(
            "No fielding restrictions — all 10 fielders may be placed anywhere.",
            "Leg-side fielding restriction: Max 2 fielders behind square on the leg side.",
            "Helmet placed on the ground by the fielding side = 5 penalty runs if ball touches it.",
            "Substitutes may field but cannot bat, bowl, or keep wicket (unless concussion sub).",
            "Concussion Substitute: Like-for-like replacement allowed with umpires' approval."
        )),
        RuleSection("Draw, Tie & Bad Light", "🌑", GoldPrimary, listOf(
            "Draw: Time runs out without a definitive result — both teams may have played well.",
            "Tie: Both teams finish with equal scores after all 4 innings (extremely rare).",
            "Bad Light: Umpires offer the batting side the option to go off — their decision.",
            "Rain: Lost overs are not compensated with extra time unless agreed. Match can be drawn.",
            "Day-Night Tests: Pink ball used; extra time can be added for overs lost to bad light."
        )),
        RuleSection("Umpires & DRS", "👁️", BluePrimary, listOf(
            "2 on-field umpires + 1 third umpire (TV umpire) + 1 fourth umpire.",
            "DRS (Decision Review System): Each team gets 3 unsuccessful reviews per innings.",
            "Reviews can challenge Caught, LBW, Bowled, Run Out, and Stumping decisions.",
            "Ball-Tracking (Hawk-Eye), Snicko, Hot Spot, and Ultra-Edge used for reviews.",
            "Umpire's Call: If 50% of ball is hitting stumps on LBW, on-field decision stands.",
            "DRS not compulsory in bilateral series — both boards must agree to use it."
        ))
    )
)

// ── Screen ──────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CricketRulesScreen(onBack: () -> Unit) {
    val formats = listOf(t20Rules, odiRules, testRules)
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Cricket Rules", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Official Format Guide", color = TextSecondary, fontSize = 12.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            // Tab row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = DarkSurface,
                contentColor = TextPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = formats[selectedTab].accentColor,
                        height = 3.dp
                    )
                }
            ) {
                formats.forEachIndexed { index, format ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                format.name,
                                fontWeight = if (selectedTab == index) FontWeight.ExtraBold else FontWeight.Normal,
                                fontSize = 14.sp,
                                color = if (selectedTab == index) format.accentColor else TextSecondary
                            )
                        }
                    )
                }
            }

            // Content
            FormatRulesContent(format = formats[selectedTab])
        }
    }
}

@Composable
private fun FormatRulesContent(format: FormatRules) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Tagline header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(format.accentColor.copy(alpha = 0.1f))
                .border(1.dp, format.accentColor.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Text(format.tagline, color = format.accentColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        // Sections
        format.sections.forEach { section ->
            RuleSectionCard(section)
        }

        Spacer(Modifier.height(72.dp)) // space for the FAB
    }
}

@Composable
private fun RuleSectionCard(section: RuleSection) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
    ) {
        // Section header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(section.color.copy(alpha = 0.1f))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(section.icon, fontSize = 16.sp)
            Text(section.title, color = section.color, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }

        // Rules
        section.rules.forEachIndexed { index, rule ->
            RuleRow(
                number = index + 1,
                rule = rule,
                accentColor = section.color,
                isLast = index == section.rules.lastIndex
            )
        }
    }
}

@Composable
private fun RuleRow(number: Int, rule: String, accentColor: Color, isLast: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Number badge
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text("$number", color = accentColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        Text(
            rule,
            color = TextPrimary,
            fontSize = 13.sp,
            lineHeight = 19.sp,
            modifier = Modifier.weight(1f)
        )
    }
    if (!isLast) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .padding(horizontal = 14.dp)
                .background(OutlineColor)
        )
    }
}
