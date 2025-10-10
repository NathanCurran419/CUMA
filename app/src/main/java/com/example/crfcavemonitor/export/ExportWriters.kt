// export/ExportWriters.kt

package com.example.crfcavemonitor.export

import android.content.ContentResolver
import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import java.io.FileNotFoundException
import java.text.SimpleDateFormat
import java.util.*
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint

// ────────────────────────────────────────────────────────────────────────────────
// BIO MONITORING CSV (one row per species)
// ────────────────────────────────────────────────────────────────────────────────

private val BIO_MONITORING_HEADERS = listOf(
    "SpNum",            // species id number
    "Common Name",      // speciesName in your payload
    "Number Observed",  // count
    "Comments"          // notes
)

fun buildBioMonitoringCsv(p: ExportPayload): String {
    val sb = StringBuilder()
    // header
    sb.append(BIO_MONITORING_HEADERS.joinToString(",") { it.csvEscape() }).append('\n')

    // rows
    for (r in p.bioRows) {
        val id    = (r["id"] ?: "").trim()
        val name  = (r["speciesName"] ?: "").trim()
        val count = (r["count"] ?: "").trim()
        val notes = (r["notes"] ?: "").trim()

        val row = listOf(id, name, count, notes)
        sb.append(row.joinToString(",") { it.csvEscape() }).append('\n')
    }
    return sb.toString()
}

// ────────────────────────────────────────────────────────────────────────────────
// Use Monitoring MCD Compatible CSV BUILDER + WRITERS
// ────────────────────────────────────────────────────────────────────────────────
private val MSS_USE_MONITORING_HEADERS = listOf(
    "Accession #",
    "Archaeological looting",
    "Area monitored",
    "Camping",
    "Cave",
    "Current Disturbance",
    "Date Entered",
    "District/Unit",
    "Fires",
    "Graffiti",
    "Monitor Date",
    "Monitored by",
    "New Record",
    "Organization",
    "Other Comments",
    "Other Considerations",
    "Owner",
    "Photo subjects",
    "Potential Disturbance",
    "Rationale",
    "Recommendations for Management",
    "Speleothem Vandalism",
    "Trash",
    "Visitation"
)

fun buildCsv(selection: ExportSelection, p: ExportPayload): String {
    return when (selection.csvFormat) {
        CsvFormat.MCD_USE_MONITORING -> buildUseMonitoringCsv(p)
        CsvFormat.BIO_MONITORING     -> buildBioMonitoringCsv(p)
    }
}

private fun buildUseMonitoringCsv(p: ExportPayload): String {
    val sb = StringBuilder()
    sb.append(MSS_USE_MONITORING_HEADERS.joinToString(",") { it.csvEscape() }).append('\n')
    sb.append(rowForMss(p).joinToString(",") { it.csvEscape() }).append('\n')
    return sb.toString()
}

/* -------------------------- Mapping helpers -------------------------- */
private fun rowForMss(p: ExportPayload): List<String> {
    val visit = p.visitRows.firstOrNull().orEmpty()
    val use   = p.useRows.firstOrNull().orEmpty()

    fun g(map: Map<String,String>, key: String) = map[key]?.trim().orEmpty()

    fun yn(v: String): String = when (v.trim().lowercase(Locale.US)) {
        "true","1","y","yes"   -> "Yes"
        "false","0","n","no"   -> "No"
        else                   -> v
    }

    fun toMMDDYYYYFromYMD(raw: String): String {
        if (raw.isBlank()) return ""
        return try {
            val inFmt  = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val outFmt = java.text.SimpleDateFormat("MM/dd/yyyy", Locale.US)
            outFmt.format(inFmt.parse(raw)!!)
        } catch (_: Throwable) { raw }
    }

    val dateEntered = java.text.SimpleDateFormat("MM/dd/yyyy", Locale.US).format(Date())

    // Photo subjects from photo captions
    val photoSubjects = p.photoRows
        .mapNotNull { it["caption"]?.trim() }
        .filter { it.isNotBlank() }
        .joinToString("; ")

    // Build "Other Comments" and append the location if present
    val otherComments = buildString {
        append(g(use, "otherComments"))
        val loc = g(visit, "location")
        if (loc.isNotBlank()) {
            if (isNotEmpty()) append("  ")
            append("[Location: ").append(loc).append("]")
        }
    }

    val districtUnit = g(visit, "unit")

    return listOf(
        /* Accession #                    */ g(visit, "mssAcc"),
        /* Archaeological looting         */ yn(g(use, "archaeologicalLooting")),
        /* Area monitored                 */ g(visit, "areaMonitored"),
        /* Camping                        */ yn(g(use, "camping")),
        /* Cave                           */ g(visit, "caveName"),
        /* Current Disturbance            */ g(use, "currentDisturbance"),
        /* Date Entered                   */ dateEntered,
        /* District/Unit                  */ districtUnit,
        /* Fires                          */ yn(g(use, "fires")),
        /* Graffiti                       */ yn(g(use, "graffiti")),
        /* Monitor Date                   */ toMMDDYYYYFromYMD(g(visit, "monitorDate")),
        /* Monitored by                   */ g(visit, "monitoredBy"),
        /* New Record                     */ "", // change to "" if you prefer blank
        /* Organization                   */ g(visit, "organization"),
        /* Other Comments                 */ otherComments,
        /* Other Considerations           */ g(use, "manageConsiderations"), // = Management Considerations
        /* Owner                          */ g(visit, "owner"),
        /* Photo subjects                 */ photoSubjects,
        /* Potential Disturbance          */ g(use, "potentialDisturbance"),
        /* Rationale                      */ g(visit, "rationale"),
        /* Recommendations for Management */ g(use, "recommendations"),
        /* Speleothem Vandalism           */ yn(g(use, "speleothemVandalism")),
        /* Trash                          */ yn(g(use, "litter")), // your field is "litter"
        /* Visitation                     */ g(use, "visitation")
    )
}

private fun String.csvEscape(): String {
    if (isEmpty()) return this
    val needsQuotes = any { it == ',' || it == '"' || it == '\n' || it == '\r' }
    return if (!needsQuotes) this else "\"" + replace("\"", "\"\"") + "\""
}

fun writeTextToUri(resolver: ContentResolver, uri: Uri, text: String) {
    resolver.openOutputStream(uri, "wt")!!.use { out ->
        out.write(text.toByteArray(Charsets.UTF_8))
        out.flush()
    }
}

fun writePdfToUri(resolver: ContentResolver, uri: Uri, doc: PdfDocument) {
    resolver.openOutputStream(uri, "w")!!.use { out ->
        doc.writeTo(out)
        out.flush()
    }
    doc.close()
}

// ────────────────────────────────────────────────────────────────────────────────
fun buildStyledPdf(
    context: Context,
    selection: ExportSelection,
    p: ExportPayload,
    resolver: ContentResolver
): PdfDocument {
    val doc = PdfDocument()

    // Page geometry (US Letter @72dpi)
    val PAGE_W = 612
    val PAGE_H = 792
    val MARGIN = 36f
    val CONTENT_W = PAGE_W - 2 * MARGIN

    // Paints
    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 18f; isFakeBoldText = true }
    val h1Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 16f; isFakeBoldText = true }
    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 12f }
    val smallPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 10f; color = Color.DKGRAY }
    val headerRule = Paint().apply { color = Color.LTGRAY; strokeWidth = 2f }
    val tableHeaderBg = Paint().apply { color = Color.rgb(240, 240, 240) }
    val tableBorder = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f; style = Paint.Style.STROKE }

    var pageNum = 0
    var page: PdfDocument.Page? = null
    var canvas: Canvas? = null
    var y = 0f

    fun newPage(): PdfDocument.Page {
        pageNum++
        val pi = PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, pageNum).create()
        val pg = doc.startPage(pi)
        y = MARGIN
        val c = pg.canvas
        c.drawText("CUMA Export – ${p.reportName}", MARGIN, y, titlePaint); y += 22f
        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())
        c.drawText("Generated: $dateStr", MARGIN, y, smallPaint); y += 8f
        c.drawLine(MARGIN, y, PAGE_W - MARGIN, y, headerRule); y += 16f
        page = pg
        canvas = pg.canvas
        return pg
    }

    fun finishPage() {
        page?.let { pg ->
            canvas?.drawText("Page $pageNum", PAGE_W - MARGIN - 60f, PAGE_H - 16f, smallPaint)
            doc.finishPage(pg)
        }
    }

    fun ensureSpace(needed: Float) {
        if (y + needed > PAGE_H - MARGIN - 24f) {
            finishPage()
            newPage()
        }
    }

    fun drawLabeledParagraph(label: String, value: String) {
        val labelPaint = Paint(textPaint).apply { isFakeBoldText = true }

        // Ensure at least a label line fits
        ensureSpace(lineHeight(labelPaint) + 6f)

        // Label
        canvas?.drawText(label, MARGIN, y - labelPaint.fontMetrics.ascent, labelPaint)
        y += lineHeight(labelPaint) + 2f

        // Wrapped paragraph across full content width
        val consumed = drawParagraph(
            canvas = canvas!!,
            text = value.ifBlank { " " },
            left = MARGIN,
            top = y,
            right = MARGIN + CONTENT_W,
            basePaint = textPaint
        )
        y += consumed + 10f
    }

    fun drawSectionTitle(title: String) {
        val titleLineHeight = (h1Paint.fontMetrics.descent - h1Paint.fontMetrics.ascent)
        val topSpacing = 12f          // space before the header block
        val betweenSpacing = 6f       // space between the top rule and title text
        val bottomSpacing = 8f        // space between title and bottom rule
        val afterBlockSpacing = 12f   // space after the header block before content

        // ensure the whole header block fits
        val needed = topSpacing + 1f + betweenSpacing + titleLineHeight + bottomSpacing + 1f + afterBlockSpacing
        ensureSpace(needed)

        // top margin
        y += topSpacing

        // top rule
        canvas?.drawLine(MARGIN, y, MARGIN + CONTENT_W, y, headerRule)
        y += betweenSpacing

        // title
        canvas?.drawText(title, MARGIN, y + (-h1Paint.fontMetrics.ascent), h1Paint)
        y += titleLineHeight + bottomSpacing

        // bottom rule
        canvas?.drawLine(MARGIN, y, MARGIN + CONTENT_W, y, headerRule)

        // spacing after the header
        y += afterBlockSpacing
    }

    fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        if (text.isBlank()) return listOf("")
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var current = StringBuilder()
        for (w in words) {
            val cand = if (current.isEmpty()) w else current.toString() + " " + w
            if (paint.measureText(cand) <= maxWidth) {
                if (current.isEmpty()) current.append(w) else current.append(" ").append(w)
            } else {
                lines.add(current.toString())
                current = StringBuilder(w)
            }
        }
        if (current.isNotEmpty()) lines.add(current.toString())
        return lines
    }

    fun drawKeyValueGrid(pairs: List<Pair<String, String>>) {
        val colW = CONTENT_W / 2f
        val keyPaint = Paint(textPaint).apply { isFakeBoldText = true }
        val maxKeyLabel = "Management Considerations:"
        val keyLabelW = (keyPaint.measureText(maxKeyLabel) + 8f).coerceAtMost(colW * 0.55f)

        var idx = 0
        while (idx < pairs.size) {
            // render up to 2 entries per row (left & right columns) and
            // advance y by the maximum height actually consumed
            var rowMaxH = 0f

            for (col in 0 until 2) {
                val i = idx + col
                if (i >= pairs.size) break

                val (k, v) = pairs[i]
                val xLeft = MARGIN + col * colW
                val xKeyRight = xLeft + keyLabelW
                val xValueRight = xLeft + colW

                // Make sure enough space for at least one line of text
                ensureSpace(lineHeight(textPaint) * 2f)

                // Draw key and value as wrapped paragraphs sharing the same top (y)
                val keyH = drawParagraph(
                    canvas = canvas!!,
                    text = "$k:",
                    left = xLeft,
                    top = y,
                    right = xKeyRight,
                    basePaint = keyPaint
                )

                val valueH = drawParagraph(
                    canvas = canvas!!,
                    text = v.ifBlank { " " },
                    left = xKeyRight + 6f,
                    top = y,
                    right = xValueRight,
                    basePaint = textPaint
                )

                rowMaxH = maxOf(rowMaxH, maxOf(keyH, valueH))
            }

            // Advance y by the tallest of the two columns, plus a little spacing
            y += rowMaxH + 6f
            idx += 2
        }

        // Small separation after the whole block
        y += 6f
    }

    fun drawTable(
        headers: List<String>,
        rows: List<List<String>>,
        colWeights: List<Float> = List(headers.size) { 1f },
        lineHeight: Float = 16f
    ) {
        if (headers.isEmpty()) return
        val totalW = CONTENT_W
        val weightsSum = colWeights.sum()
        val colWidths = headers.indices.map { i -> totalW * (colWeights[i] / weightsSum) }

        ensureSpace(24f)
        var x = MARGIN
        val headerTop = y
        val headerBottom = y + 20f
        for ((i, h) in headers.withIndex()) {
            val cw = colWidths[i]
            val rect = RectF(x, headerTop, x + cw, headerBottom)
            canvas?.drawRect(rect, tableHeaderBg)
            canvas?.drawRect(rect, tableBorder)
            canvas?.drawText(h, x + 6f, y + 14f, Paint(textPaint).apply { isFakeBoldText = true })
            x += cw
        }
        y = headerBottom

        for (row in rows) {
            // 1) Measure/wrap each cell to find how many lines it needs
            val wrappedPerCell = ArrayList<List<String>>(row.size)
            var maxLinesInRow = 1
            for ((i, cell) in row.withIndex()) {
                val cw = colWidths[i]
                val clipped = cell.replace("\n", " ").take(300)
                val lines = wrapText(clipped, textPaint, cw - 8f)
                wrappedPerCell.add(lines)
                if (lines.size > maxLinesInRow) maxLinesInRow = lines.size
            }

            // 2) Compute required height for this row (all columns)
            val rowNeeded = (lineHeight * maxLinesInRow) + 6f
            ensureSpace(rowNeeded)

            // 3) Draw row background/borders and the wrapped text for each cell
            x = MARGIN
            val top = y
            val bottom = y + rowNeeded
            for (i in row.indices) {
                val cw = colWidths[i]
                val rect = RectF(x, top, x + cw, bottom)
                canvas?.drawRect(rect, tableBorder)

                // Draw each wrapped line in the cell
                var localY = y + lineHeight
                for (ln in wrappedPerCell[i]) {
                    canvas?.drawText(ln, x + 4f, localY, textPaint)
                    localY += lineHeight
                }

                x += cw
            }

            // 4) Advance to next row
            y = bottom
        }
        y += 8f
    }

    fun drawPhotosGrid(items: List<Map<String, String>>) {
        if (items.isEmpty()) return
        val cols = 2
        val gap = 10f
        val cellW = (CONTENT_W - gap) / cols
        val imgH = 170f
        val capH = 34f
        val cellH = imgH + capH

        for ((index, row) in items.withIndex()) {
            val col = index % cols
            if (col == 0) ensureSpace(cellH + 8f)
            val x = MARGIN + col * (cellW + gap)
            val top = y
            val uriStr = row["uri"].orEmpty()
            val caption = row["caption"].orEmpty()
            val stamp = row["timestamp"].orEmpty()

            val bmp: Bitmap? = try {
                val uri = Uri.parse(uriStr)
                loadScaledBitmap(resolver, uri, targetW = cellW.toInt(), targetH = imgH.toInt())
            } catch (_: SecurityException) {
                null
            } catch (_: FileNotFoundException) {
                null
            } catch (_: OutOfMemoryError) {
                null
            } catch (_: Throwable) {
                null
            }

            if (bmp != null) {
                val rect = RectF(x, top, x + cellW, top + imgH)
                canvas?.drawBitmap(bmp, null, rect, null)
            } else {
                val rect = RectF(x, top, x + cellW, top + imgH)
                canvas?.drawRect(rect, tableBorder)
                canvas?.drawText("Image unavailable", x + 8f, top + imgH / 2, smallPaint)
            }

            val cy = top + imgH + 14f
            val cap = if (caption.isBlank()) "(no caption)" else caption
            val line1 = ellipsizeToWidth(cap, textPaint, cellW - 6f)
            canvas?.drawText(line1, x + 3f, cy, textPaint)
            canvas?.drawText(stamp, x + 3f, cy + 14f, smallPaint)

            if (col == cols - 1) y = top + cellH + 8f
        }
        if (items.size % 2 != 0) y += 8f
    }

    // ---- Document flow ----
    newPage()

    val includeAll = ExportSection.ALL in selection.sections

    if (includeAll || ExportSection.VISIT in selection.sections) {
        drawSectionTitle("Visit Details")

        val visit = p.visitRows.firstOrNull().orEmpty()
        val visitPairs = listOf(
            "Cave Name" to (visit["caveName"] ?: ""),
            "MSS #" to (visit["mssAcc"] ?: ""),
            "Owner" to (visit["owner"] ?: ""),
            "District or Unit" to (visit["unit"] ?: ""),
            "Monitor Date" to (visit["monitorDate"] ?: ""),
            "Organization" to (visit["organization"] ?: ""),
            "Monitored By" to (visit["monitoredBy"] ?: ""),
            "Area Monitored" to (visit["areaMonitored"] ?: ""),
            "Rationale" to (visit["rationale"] ?: ""),
            "Entrance Coordinates" to (visit["location"] ?: "")
        )
        drawKeyValueGrid(visitPairs)
    }

    if (includeAll || ExportSection.USE in selection.sections) {
        drawSectionTitle("Use / Human Impact")

        val use = p.useRows.firstOrNull().orEmpty()

        // Short items stay in the two-column grid:
        val gridPairs = listOf(
            "Visitation" to (use["visitation"] ?: ""),
            "Litter" to (use["litter"] ?: ""),
            "Speleothem Vandalism" to (use["speleothemVandalism"] ?: ""),
            "Graffiti" to (use["graffiti"] ?: ""),
            "Archaeological Looting" to (use["archaeologicalLooting"] ?: ""),
            "Fires" to (use["fires"] ?: ""),
            "Camping" to (use["camping"] ?: ""),
            "Current Disturbance" to (use["currentDisturbance"] ?: ""),
            "Potential Disturbance" to (use["potentialDisturbance"] ?: "")
        )
        drawKeyValueGrid(gridPairs)

        // Long-form fields get full width, below the grid:
        val management = use["manageConsiderations"].orEmpty()
        val recommendations = use["recommendations"].orEmpty()
        val other = use["otherComments"].orEmpty()

        if (management.isNotBlank()) drawLabeledParagraph("Management Considerations", management)
        if (recommendations.isNotBlank()) drawLabeledParagraph("Recommendations", recommendations)
        if (other.isNotBlank()) drawLabeledParagraph("Other Comments", other)
    }


    if ((includeAll || ExportSection.BIO in selection.sections) && p.bioRows.isNotEmpty()) {
        drawSectionTitle("Bio")
        val headers = listOf("SpNum","Species", "Count", "Notes")
        val rows = p.bioRows.map { r ->
            listOf(
                r["id"].orEmpty(),
                r["speciesName"].orEmpty(),
                r["count"].orEmpty(),
                r["notes"].orEmpty()
            )
        }
        val weights = listOf(0.6f, 1.6f, 0.6f, 1.2f)
        drawTable(headers, rows, weights, lineHeight = 16f)
    }

    if ((includeAll || ExportSection.PHOTOS in selection.sections) && p.photoRows.isNotEmpty()) {
        drawSectionTitle("Photos")
        drawPhotosGrid(p.photoRows)
    }

    finishPage()
    return doc
}

// ────────────────────────────────────────────────────────────────────────────────
// Helpers
// ────────────────────────────────────────────────────────────────────────────────

private fun lineHeight(paint: Paint): Float {
    val fm = paint.fontMetrics
    return (fm.descent - fm.ascent)
}

private fun drawParagraph(
    canvas: Canvas,
    text: CharSequence,
    left: Float,
    top: Float,
    right: Float,
    basePaint: Paint,
    alignment: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL,
    lineSpacingAdd: Float = 0f,
    lineSpacingMult: Float = 1f
): Float {
    val width = (right - left).toInt().coerceAtLeast(1)
    val tp = TextPaint(basePaint)

    val layout = if (Build.VERSION.SDK_INT >= 23) {
        StaticLayout.Builder.obtain(text, 0, text.length, tp, width)
            .setAlignment(alignment)
            .setIncludePad(false)
            .setLineSpacing(lineSpacingAdd, lineSpacingMult)
            .build()
    } else {
        @Suppress("DEPRECATION")
        StaticLayout(text, tp, width, alignment, lineSpacingMult, lineSpacingAdd, false)
    }

    canvas.save()
    canvas.translate(left, top)
    layout.draw(canvas)
    canvas.restore()

    return layout.height.toFloat()
}

private fun ellipsizeToWidth(text: String, paint: Paint, maxWidth: Float): String {
    if (paint.measureText(text) <= maxWidth) return text
    var low = 0
    var high = text.length
    while (low < high) {
        val mid = (low + high) / 2
        val candidate = text.substring(0, mid) + "…"
        if (paint.measureText(candidate) <= maxWidth) {
            low = mid + 1
        } else {
            high = mid
        }
    }
    return text.substring(0, maxOf(1, low - 1)) + "…"
}

private fun loadScaledBitmap(
    resolver: ContentResolver,
    uri: Uri,
    targetW: Int,
    targetH: Int
): Bitmap? {
    return try {
        // bounds
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        // sample size
        var inSample = 1
        var w = bounds.outWidth
        var h = bounds.outHeight
        while (w / 2 >= targetW && h / 2 >= targetH) {
            w /= 2; h /= 2; inSample *= 2
        }

        // decode scaled
        val opts = BitmapFactory.Options().apply { inSampleSize = inSample }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
    } catch (_: SecurityException) {
        null
    } catch (_: FileNotFoundException) {
        null
    } catch (_: OutOfMemoryError) {
        null
    } catch (_: Throwable) {
        null
    }
}