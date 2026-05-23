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
import com.nitish.cricketscoringapp.ui.theme.*

private val EmeraldPrimary = Color(0xFF00C853)
private val GoldPrimary   = Color(0xFFFFD740)
private val BluePrimary   = Color(0xFF2979FF)
private val PurplePrimary = Color(0xFF9C27B0)
private val OrangePrimary = Color(0xFFFF6D00)

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
            "Free Hit: On every No Ball, the next delivery is a Free Hit — batsman cannot be dismissed (except run out, obstructing the field).",
            "Wide: Ball outside the tramlines — 1 extra run + delivery re-bowled.",
            "Beamer (head-high full toss): Instantly declared a No Ball; bowler gets a first and final warning."
        )),
        RuleSection("Batting Rules", "🏏", OrangePrimary, listOf(
            "Batsman must be ready within 90 seconds of the previous wicket falling.",
            "Each batting pair must score runs — passive play is still valid strategy in T20.",
            "A Free Hit delivery: batsman can only be dismissed by run out, obstructing the field, or handling the ball.",
            "Hit wicket: If a batsman knocks the stumps while playing a shot or setting off for a run.",
            "Timed Out: New batsman must be ready within 90 seconds — else given out."
        )),
        RuleSection("Dismissals — Ways to be Out", "❌", Color(0xFFFF4444), listOf(
            "Bowled: Ball delivered by bowler hits and dislodges the bails off the stumps directly.",
            "Caught: Fielder (including bowler or keeper) catches the ball before it touches the ground.",
            "LBW (Leg Before Wicket): Ball strikes the batsman's body (not bat) in line with stumps and would have hit them — umpire upholds appeal.",
            "Run Out: Batsman is outside the crease when the stumps are broken by the fielding side during a run attempt.",
            "Stumped: Wicket-keeper breaks the stumps with the ball while batsman is out of the crease and not attempting a run.",
            "Hit Wicket: Batsman knocks down their own stumps with bat or body while playing a shot or starting a run.",
            "Obstructing the Field: Batsman deliberately hinders a fielder with words or action (includes deliberately handling the ball).",
            "Hit the Ball Twice: Batsman deliberately strikes the ball a second time (other than to protect the wicket).",
            "Timed Out: Incoming batsman takes more than 90 seconds (T20) to be ready after a wicket falls.",
            "Handled the Ball: Now covered under 'Obstructing the Field' in modern laws (Law 37, 2017 Code)."
        )),
        RuleSection("Retired Hurt & Retired Out", "🏥", Color(0xFFFFAB00), listOf(
            "RETIRED HURT — NOT Out: Batsman leaves the field due to injury, illness, or emergency.",
            "Retired Hurt does NOT count as a wicket — scoreboard shows 'retired hurt'.",
            "Bowler gets NO credit for a Retired Hurt — wicket tally unchanged.",
            "The retired hurt batsman MAY return later in the SAME innings if fit.",
            "On return: the batsman resumes from where they left off (runs and balls count).",
            "If the innings ends before the batsman returns, they are recorded as 'not out'.",
            "RETIRED OUT — Out: Batsman retires without a valid reason or umpire's permission.",
            "Retired Out COUNTS as a wicket — scoreboard shows 'retired out'.",
            "Bowler gets NO credit (same convention as Run Out — no individual bowler responsible).",
            "The retired out batsman CANNOT return in the same innings under any circumstances."
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
        RuleSection("DLS Method", "🌧️", Color(0xFF8A9BB5), listOf(
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
            "A bowler can bowl a maximum of 10 consecutive overs before being rested.",
            "Deliberate beamer: No Ball declared; bowler gets a warning — second offence = removed from attack."
        )),
        RuleSection("Batting Rules", "🏏", OrangePrimary, listOf(
            "Batsman must be ready within 3 minutes of the previous wicket falling.",
            "Free Hit delivery (after No Ball): batsman can only be out run out, obstructing the field, or hit the ball twice.",
            "Hit Wicket: Batsman out if they dislodge their own stumps while batting.",
            "Timed Out: New batsman must be ready within 3 minutes — else given out."
        )),
        RuleSection("Dismissals — Ways to be Out", "❌", Color(0xFFFF4444), listOf(
            "Bowled: Ball delivered by bowler hits and dislodges the bails off the stumps directly.",
            "Caught: Fielder (including bowler or keeper) catches the ball before it touches the ground.",
            "LBW (Leg Before Wicket): Ball strikes the batsman's body in line with stumps and would have hit them — umpire decision or DRS.",
            "Run Out: Batsman is outside the crease when the stumps are broken by the fielding side during a run attempt.",
            "Stumped: Keeper breaks stumps while batsman is out of crease — not attempting a run.",
            "Hit Wicket: Batsman knocks down their own stumps with bat or body while playing a shot or starting a run.",
            "Obstructing the Field: Deliberately hindering a fielder with words or action.",
            "Hit the Ball Twice: Deliberately strikes the ball a second time (except to protect stumps).",
            "Timed Out: New batsman takes more than 3 minutes to be ready after a wicket falls.",
            "Handled the Ball: Now merged into 'Obstructing the Field' under Law 37 (2017 Cricket Laws)."
        )),
        RuleSection("Retired Hurt & Retired Out", "🏥", Color(0xFFFFAB00), listOf(
            "RETIRED HURT — NOT Out: Batsman leaves due to injury, illness, or medical emergency.",
            "Retired Hurt does NOT count as a wicket — bowler gets no credit.",
            "The player may return and bat again later in the SAME innings if declared fit.",
            "On return: existing runs and balls faced continue from where they left off.",
            "If innings ends before return, the batsman is recorded as 'not out'.",
            "RETIRED OUT — Out: Batsman retires without a valid reason or umpire approval.",
            "Retired Out COUNTS as a wicket — appears as 'retired out' in the scorecard.",
            "Bowler gets NO credit for a Retired Out (same rule as Run Out).",
            "Retired Out batsman CANNOT return in the same innings under any circumstances.",
            "Umpire's role: Umpire must be informed; reason determines Retired Hurt vs Retired Out."
        )),
        RuleSection("Fielding Rules", "🧤", PurplePrimary, listOf(
            "11 fielders including wicket-keeper.",
            "Regular substitutes may field but cannot bat, bowl, or keep wicket.",
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
        RuleSection("DLS & Interruptions", "🌧️", Color(0xFF8A9BB5), listOf(
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
            "No Ball: Front-foot overstepping, beamer above waist, ball bounces more than twice, etc.",
            "Free Hit does NOT apply in Test cricket — No Ball is penalized with 1 extra run only.",
            "Wide: More lenient than in limited-overs; judged as 'clearly out of reach' of the batsman.",
            "Bowler must not bowl more than 2 consecutive overs from the same end.",
            "Deliberate time-wasting by bowler attracts a 5-run penalty to the fielding side."
        )),
        RuleSection("Batting Rules", "🏏", OrangePrimary, listOf(
            "No mandatory batting order change — only dismissals force a new batsman.",
            "Night Watchman: Lower-order batsman sent in near close of play to protect recognized batsmen.",
            "Timed Out: New batsman must arrive within 3 minutes of the previous wicket falling.",
            "Hit Wicket: Out if batsman knocks down their own stumps while playing a stroke or setting off for a run.",
            "Penalty Runs: 5 runs awarded to batting side if ball hits a fielding-side helmet on the ground."
        )),
        RuleSection("Dismissals — Ways to be Out", "❌", Color(0xFFFF4444), listOf(
            "Bowled: Ball delivered by bowler hits and dislodges the bails off the stumps directly.",
            "Caught: Fielder (including bowler or keeper) catches the ball before touching the ground.",
            "LBW (Leg Before Wicket): Ball strikes batsman's body in line with stumps — DRS (Hawk-Eye) used to verify.",
            "Run Out: Batsman is out of crease when stumps are broken during a run — DRS can review close calls.",
            "Stumped: Keeper breaks stumps while batsman steps out of crease — not attempting a run.",
            "Hit Wicket: Batsman dislodges own stumps with bat or body while playing or starting a run.",
            "Obstructing the Field: Deliberately hinders a fielder with action or words (includes handling the ball).",
            "Hit the Ball Twice: Deliberately strikes ball a second time (except to prevent it hitting stumps).",
            "Timed Out: New batsman takes more than 3 minutes to be ready — extremely rare in Tests.",
            "Handled the Ball: Now merged under 'Obstructing the Field' (Law 37, 2017 MCC Code)."
        )),
        RuleSection("Retired Hurt & Retired Out", "🏥", Color(0xFFFFAB00), listOf(
            "RETIRED HURT — NOT Out: Batsman leaves due to injury, illness, or a genuine emergency.",
            "No wicket counted — Retired Hurt does not appear in the dismissal column as 'out'.",
            "Bowler gets ZERO credit — wicket tally and economy rate are both unaffected.",
            "The batsman MAY return at the fall of a wicket, with umpires' agreement, in the same innings.",
            "Strategy note: in Tests, a team may continue batting without the retired hurt player.",
            "If match ends before the player returns, they remain 'not out' in the final scorecard.",
            "RETIRED OUT — Out: Batsman leaves voluntarily without a valid reason or umpire permission.",
            "Counted as a wicket — 'retired out' appears in the scorecard dismissal column.",
            "Bowler gets NO credit (same convention as Run Out — no specific bowler is responsible).",
            "The player CANNOT return in the same innings — this is final and irrevocable.",
            "Umpire's responsibility: Record the reason; the umpire's judgment determines Hurt vs Out."
        )),
        RuleSection("Fielding Rules", "🧤", Color(0xFF8A9BB5), listOf(
            "No fielding circle restrictions — all 10 fielders may be placed anywhere.",
            "Leg-side restriction: Max 2 fielders behind square on the leg side at the moment of delivery.",
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
    val c = LocalAppColors.current
    val formats = listOf(t20Rules, odiRules, testRules)
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        containerColor = c.bg,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Cricket Rules", color = c.textPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Official Format Guide", color = c.textSecondary, fontSize = 12.sp)
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
        Column(Modifier.fillMaxSize().padding(padding)) {
            // Tab row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = c.surface,
                contentColor = c.textPrimary,
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
                                color = if (selectedTab == index) format.accentColor else c.textSecondary
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
    val c = LocalAppColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(c.surface)
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
    val c = LocalAppColors.current
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
            color = c.textPrimary,
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
                .background(c.outline)
        )
    }
}
