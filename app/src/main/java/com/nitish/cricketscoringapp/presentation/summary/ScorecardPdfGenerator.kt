package com.nitish.cricketscoringapp.presentation.summary

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import java.io.File
import java.io.FileOutputStream

// ── Page / layout constants ────────────────────────────────────────────
private const val PW = 595         // A4 width  (pts)
private const val PH = 842         // A4 height (pts)
private const val M  = 24f         // margin
private const val CW = PW - M * 2  // content width = 547

// Batting column right-edges
private val BAT_NAME_END = M + 220f
private val BAT_R_END    = BAT_NAME_END + 52f
private val BAT_B_END    = BAT_R_END    + 52f
private val BAT_4_END    = BAT_B_END    + 52f
private val BAT_6_END    = BAT_4_END    + 52f
private val BAT_SR_END   = M + CW

// Bowling column right-edges
private val BWL_NAME_END = M + 220f
private val BWL_O_END    = BWL_NAME_END + 55f
private val BWL_M_END    = BWL_O_END    + 55f
private val BWL_R_END    = BWL_M_END    + 55f
private val BWL_W_END    = BWL_R_END    + 55f
private val BWL_ER_END   = M + CW

// Fall of wickets column right-edges
private val FOW_NAME_END  = M + 295f
private val FOW_SCORE_END = M + 430f
private val FOW_OVER_END  = M + CW

// Colors
private val GREEN_DARK  = Color.rgb(27,  94,  32)
private val GREEN_LIGHT = Color.rgb(232, 245, 233)
private val RESULT_BG   = Color.rgb(255, 248, 225)
private val DIVIDER     = Color.rgb(180, 180, 180)
private val GRAY_TEXT   = Color.rgb(100, 100, 100)
private val TITLE_GREEN = Color.rgb(27,  94,  32)

// ── Page context ───────────────────────────────────────────────────────
private class PageContext(val doc: PdfDocument) {
    private var page: PdfDocument.Page? = null
    var cv: Canvas? = null
    var y = M
    private var num = 1

    fun newPage() {
        page?.let { doc.finishPage(it) }
        val info = PdfDocument.PageInfo.Builder(PW, PH, num++).create()
        page = doc.startPage(info)
        cv   = page!!.canvas
        y    = M
    }

    fun finish() { page?.let { doc.finishPage(it) } }

    fun need(h: Float) { if (y + h > PH - M) newPage() }
    fun down(h: Float) { y += h }
}

// ── Paint factory ──────────────────────────────────────────────────────
private fun mkPaint(
    color: Int         = Color.BLACK,
    size:  Float       = 10f,
    bold:  Boolean     = false,
    align: Paint.Align = Paint.Align.LEFT
) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    this.color = color
    textSize   = size
    typeface   = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
    textAlign  = align
}

// Text baseline centred in a row
private fun bl(rowTop: Float, rowH: Float, textSize: Float) =
    rowTop + (rowH + textSize * 0.72f) / 2f

private fun PageContext.fillRect(x: Float, top: Float, w: Float, h: Float, color: Int) =
    cv?.drawRect(x, top, x + w, top + h,
        Paint().apply { this.color = color; style = Paint.Style.FILL })

private fun PageContext.hline(yy: Float = y, color: Int = DIVIDER) =
    cv?.drawLine(M, yy, M + CW, yy,
        Paint().apply { this.color = color; strokeWidth = 0.6f })

private fun PageContext.drawText(text: String, x: Float, rowTop: Float, rowH: Float, paint: Paint) =
    cv?.drawText(text, x, bl(rowTop, rowH, paint.textSize), paint)

// ── Public entry point ─────────────────────────────────────────────────
object ScorecardPdfGenerator {

    fun generate(context: Context, state: MatchSummaryUiState): File {
        val doc = PdfDocument()
        val pc  = PageContext(doc)
        pc.newPage()

        drawMatchHeader(pc, state)
        state.innings1Score?.let { drawInnings(pc, it) }
        state.innings2Score?.let {
            if (it.batsmen.isNotEmpty() || it.totalRuns > 0) drawInnings(pc, it)
        }
        drawFooter(pc)
        pc.finish()

        val file = File(context.cacheDir, "scorecard.pdf")
        FileOutputStream(file).use { doc.writeTo(it) }
        doc.close()
        return file
    }

    // ── Match header (title + result) ──────────────────────────────────
    private fun drawMatchHeader(pc: PageContext, state: MatchSummaryUiState) {
        val title = state.match?.let { "${it.team1Name} v/s ${it.team2Name}" } ?: "Cricket Scorecard"

        pc.need(64f)
        pc.fillRect(M, pc.y, CW, 36f, Color.WHITE)
        pc.drawText(title, PW / 2f, pc.y, 36f,
            mkPaint(TITLE_GREEN, 20f, bold = true, align = Paint.Align.CENTER))
        pc.hline(pc.y + 36f, Color.rgb(200, 200, 200))
        pc.down(38f)

        pc.fillRect(M, pc.y, CW, 22f, RESULT_BG)
        pc.drawText(state.result, M + 8f, pc.y, 22f, mkPaint(Color.rgb(120, 0, 0), 11f))
        pc.down(26f)
    }

    // ── One innings ────────────────────────────────────────────────────
    private fun drawInnings(pc: PageContext, score: com.nitish.cricketscoringapp.domain.model.InningsScore) {
        drawTeamBanner(pc, score)
        drawBatting(pc, score)
        drawBowling(pc, score)
        if (score.fallOfWickets.isNotEmpty()) drawFow(pc, score)
        pc.down(8f)
    }

    // ── Green team banner ──────────────────────────────────────────────
    private fun drawTeamBanner(pc: PageContext, score: com.nitish.cricketscoringapp.domain.model.InningsScore) {
        pc.need(28f)
        val h = 26f
        pc.fillRect(M, pc.y, CW, h, GREEN_DARK)
        pc.drawText(score.battingTeamName, M + 8f, pc.y, h,
            mkPaint(Color.WHITE, 13f, bold = true))
        val s = "${score.totalRuns}-${score.wickets} (${score.oversDisplay})"
        pc.drawText(s, M + CW - 8f, pc.y, h,
            mkPaint(Color.WHITE, 12f, bold = true, align = Paint.Align.RIGHT))
        pc.down(h)
    }

    // ── Batting table ──────────────────────────────────────────────────
    private fun drawBatting(pc: PageContext, score: com.nitish.cricketscoringapp.domain.model.InningsScore) {
        // Column header row
        pc.need(18f)
        val hh = 18f
        pc.fillRect(M, pc.y, CW, hh, GREEN_DARK)
        pc.drawText("Batsman", M + 4f, pc.y, hh, mkPaint(Color.WHITE, 10f, bold = true))
        val rp = mkPaint(Color.WHITE, 10f, bold = true, align = Paint.Align.RIGHT)
        for ((t, x) in listOf("R" to BAT_R_END, "B" to BAT_B_END, "4s" to BAT_4_END, "6s" to BAT_6_END, "SR" to BAT_SR_END))
            pc.drawText(t, x - 4f, pc.y, hh, rp)
        pc.down(hh)

        // Batsman rows
        val batsmen = score.batsmen.filter { it.balls > 0 || it.isOut }
        batsmen.forEachIndexed { i, b ->
            val dh = 13f
            pc.need(18f + dh)
            val bg = if (i % 2 == 0) Color.WHITE else GREEN_LIGHT
            pc.fillRect(M, pc.y, CW, 18f, bg)
            pc.drawText(b.player.name, M + 4f, pc.y, 18f, mkPaint(Color.BLACK, 10f))
            val np = mkPaint(Color.BLACK, 10f, align = Paint.Align.RIGHT)
            pc.drawText("${b.runs}",                     BAT_R_END  - 4f, pc.y, 18f, np)
            pc.drawText("${b.balls}",                    BAT_B_END  - 4f, pc.y, 18f, np)
            pc.drawText("${b.fours}",                    BAT_4_END  - 4f, pc.y, 18f, np)
            pc.drawText("${b.sixes}",                    BAT_6_END  - 4f, pc.y, 18f, np)
            pc.drawText("%.2f".format(b.strikeRate),     BAT_SR_END - 4f, pc.y, 18f, np)
            pc.down(18f)

            // Dismissal / not out sub-row
            pc.fillRect(M, pc.y, CW, dh, bg)
            pc.drawText(b.dismissalInfo ?: "not out", M + 14f, pc.y, dh, mkPaint(GRAY_TEXT, 9f))
            pc.down(dh)
            pc.hline()
        }

        // Extras
        pc.need(18f)
        pc.fillRect(M, pc.y, CW, 18f, Color.WHITE)
        pc.drawText("Extras", M + 4f, pc.y, 18f, mkPaint(Color.BLACK, 10f, bold = true))
        val ext = "(${score.extras}) ${score.byes} B, ${score.legByes} LB, ${score.wides} WD, ${score.noBalls} NB"
        pc.drawText(ext, M + CW - 4f, pc.y, 18f,
            mkPaint(GRAY_TEXT, 9.5f, align = Paint.Align.RIGHT))
        pc.down(18f)
        pc.hline()

        // Total
        pc.need(22f)
        pc.fillRect(M, pc.y, CW, 22f, GREEN_LIGHT)
        pc.drawText("Total", M + 4f, pc.y, 22f, mkPaint(Color.BLACK, 11f, bold = true))
        val tot = "${score.totalRuns}-${score.wickets} (${score.oversDisplay})  RR: ${"%.2f".format(score.runRate)}"
        pc.drawText(tot, M + CW - 4f, pc.y, 22f,
            mkPaint(Color.BLACK, 10f, bold = true, align = Paint.Align.RIGHT))
        pc.down(22f)
        pc.hline(color = GREEN_DARK)
        pc.down(8f)
    }

    // ── Bowling table ──────────────────────────────────────────────────
    private fun drawBowling(pc: PageContext, score: com.nitish.cricketscoringapp.domain.model.InningsScore) {
        val bowlers = score.bowlers.filter { it.totalLegalBalls > 0 }
        if (bowlers.isEmpty()) return

        pc.need(18f)
        val hh = 18f
        pc.fillRect(M, pc.y, CW, hh, GREEN_DARK)
        pc.drawText("Bowler", M + 4f, pc.y, hh, mkPaint(Color.WHITE, 10f, bold = true))
        val rp = mkPaint(Color.WHITE, 10f, bold = true, align = Paint.Align.RIGHT)
        for ((t, x) in listOf("O" to BWL_O_END, "M" to BWL_M_END, "R" to BWL_R_END, "W" to BWL_W_END, "ER" to BWL_ER_END))
            pc.drawText(t, x - 4f, pc.y, hh, rp)
        pc.down(hh)

        bowlers.forEachIndexed { i, b ->
            pc.need(18f)
            val bg = if (i % 2 == 0) Color.WHITE else GREEN_LIGHT
            pc.fillRect(M, pc.y, CW, 18f, bg)
            pc.drawText(b.player.name, M + 4f, pc.y, 18f, mkPaint(Color.BLACK, 10f))
            val np = mkPaint(Color.BLACK, 10f, align = Paint.Align.RIGHT)
            pc.drawText(b.oversDisplay,           BWL_O_END  - 4f, pc.y, 18f, np)
            pc.drawText("${b.maidens}",           BWL_M_END  - 4f, pc.y, 18f, np)
            pc.drawText("${b.runs}",              BWL_R_END  - 4f, pc.y, 18f, np)
            pc.drawText("${b.wickets}",           BWL_W_END  - 4f, pc.y, 18f, np)
            pc.drawText("%.2f".format(b.economy), BWL_ER_END - 4f, pc.y, 18f, np)
            pc.down(18f)
            pc.hline()
        }
        pc.down(8f)
    }

    // ── Fall of wickets table ──────────────────────────────────────────
    private fun drawFow(pc: PageContext, score: com.nitish.cricketscoringapp.domain.model.InningsScore) {
        pc.need(18f)
        val hh = 18f
        pc.fillRect(M, pc.y, CW, hh, GREEN_DARK)
        pc.drawText("Fall of Wickets", M + 4f, pc.y, hh, mkPaint(Color.WHITE, 10f, bold = true))
        val hp = mkPaint(Color.WHITE, 10f, bold = true, align = Paint.Align.RIGHT)
        pc.drawText("Score", FOW_SCORE_END - 4f, pc.y, hh, hp)
        pc.drawText("Over",  FOW_OVER_END  - 4f, pc.y, hh, hp)
        pc.down(hh)

        score.fallOfWickets.forEachIndexed { i, fow ->
            val rowH = 17f
            val subH = 12f
            pc.need(rowH + subH)
            val bg = if (i % 2 == 0) Color.WHITE else GREEN_LIGHT

            // Main row: player name, score, over
            pc.fillRect(M, pc.y, CW, rowH, bg)
            pc.drawText(fow.playerName, M + 4f, pc.y, rowH, mkPaint(Color.BLACK, 10f, bold = true))
            val rp = mkPaint(Color.BLACK, 10f, align = Paint.Align.RIGHT)
            pc.drawText("${fow.score}/${fow.wicketNumber}", FOW_SCORE_END - 4f, pc.y, rowH, rp)
            pc.drawText(fow.overDisplay,                    FOW_OVER_END  - 4f, pc.y, rowH, rp)
            pc.down(rowH)

            // Sub-row: dismissal info
            pc.fillRect(M, pc.y, CW, subH, bg)
            pc.drawText(fow.dismissalInfo, M + 14f, pc.y, subH, mkPaint(GRAY_TEXT, 8.5f))
            pc.down(subH)

            pc.hline()
        }
        pc.down(8f)
    }

    // ── Footer ─────────────────────────────────────────────────────────
    private fun drawFooter(pc: PageContext) {
        pc.need(20f)
        pc.down(10f)
        pc.drawText(
            "Generated by Cricket Scoring App",
            PW / 2f, pc.y, 12f,
            mkPaint(Color.LTGRAY, 9f, align = Paint.Align.CENTER)
        )
        pc.down(12f)
    }
}
