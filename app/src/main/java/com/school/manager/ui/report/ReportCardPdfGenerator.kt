package com.school.manager.ui.report

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object ReportCardPdfGenerator {

    private const val PAGE_W = 595
    private const val PAGE_H = 842

    fun generate(context: Context, state: ReportCardUiState): File {
        val pdf = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, 1).create()
        val page = pdf.startPage(pageInfo)
        val canvas = page.canvas

        drawBackground(canvas)
        drawHeader(canvas, state)
        drawStudentInfo(canvas, state)
        drawMarksTable(canvas, state)
        drawAttendance(canvas, state)
        drawSummary(canvas, state)
        drawFooter(canvas, state)

        pdf.finishPage(page)

        val dir = File(context.getExternalFilesDir(null), "reports").apply { mkdirs() }
        val safeName = state.studentName.replace(" ", "_").ifBlank { "student" }
        val file = File(dir, "ReportCard_${safeName}_${System.currentTimeMillis()}.pdf")
        FileOutputStream(file).use { pdf.writeTo(it) }
        pdf.close()
        return file
    }

    fun share(context: Context, file: File) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Report Card"))
    }

    private fun drawBackground(canvas: Canvas) {
        canvas.drawRect(0f, 0f, PAGE_W.toFloat(), PAGE_H.toFloat(),
            Paint().apply { color = Color.rgb(245, 245, 245) })
    }

    private fun drawHeader(canvas: Canvas, state: ReportCardUiState) {
        canvas.drawRect(0f, 0f, PAGE_W.toFloat(), 90f,
            Paint().apply { color = Color.rgb(26, 35, 126) })
        canvas.drawText("School Manager", 40f, 45f,
            Paint().apply { color = Color.WHITE; textSize = 26f; isFakeBoldText = true; isAntiAlias = true })
        canvas.drawText("Official Academic Report Card", 40f, 68f,
            Paint().apply { color = Color.argb(200, 255, 255, 255); textSize = 12f; isAntiAlias = true })
        canvas.drawText("Generated: ${state.generatedOn}", PAGE_W - 180f, 45f,
            Paint().apply { color = Color.WHITE; textSize = 10f; isAntiAlias = true })
    }

    private fun drawStudentInfo(canvas: Canvas, state: ReportCardUiState) {
        val rect = Rect(30, 110, PAGE_W - 30, 220)
        canvas.drawRect(rect, Paint().apply { color = Color.WHITE })
        canvas.drawRect(rect, Paint().apply {
            color = Color.rgb(220, 220, 220); style = Paint.Style.STROKE; strokeWidth = 1.5f
        })

        val lbl = Paint().apply { color = Color.rgb(120, 120, 120); textSize = 10f; isAntiAlias = true }
        val val_ = Paint().apply { color = Color.rgb(20, 20, 20); textSize = 15f; isFakeBoldText = true; isAntiAlias = true }

        canvas.drawText("STUDENT NAME", 50f, 140f, lbl)
        canvas.drawText(state.studentName, 50f, 162f, val_)
        canvas.drawText("CLASS", 50f, 192f, lbl)
        canvas.drawText(state.className, 50f, 212f, val_)
        canvas.drawText("ROLL NO", 330f, 140f, lbl)
        canvas.drawText(state.rollNo, 330f, 162f, val_)
        canvas.drawText("EMAIL", 330f, 192f, lbl)
        canvas.drawText(state.studentEmail.take(28), 330f, 210f,
            Paint().apply { color = Color.rgb(20, 20, 20); textSize = 12f; isAntiAlias = true })
    }

    private fun drawMarksTable(canvas: Canvas, state: ReportCardUiState) {
        val startY = 250f
        val rowH = 26f
        val colX = floatArrayOf(45f, 280f, 340f, 410f, 480f)
        val headers = arrayOf("Subject", "Marks", "Total", "%", "Grade")

        canvas.drawRect(30f, startY, PAGE_W - 30f, startY + rowH,
            Paint().apply { color = Color.rgb(26, 35, 126) })

        val hdr = Paint().apply { color = Color.WHITE; textSize = 11f; isFakeBoldText = true; isAntiAlias = true }
        headers.forEachIndexed { i, h -> canvas.drawText(h, colX[i], startY + 17f, hdr) }

        val row = Paint().apply { color = Color.rgb(30, 30, 30); textSize = 11f; isAntiAlias = true }
        val alt = Paint().apply { color = Color.rgb(240, 242, 250) }

        var y = startY + rowH
        state.subjects.forEachIndexed { i, s ->
            if (i % 2 == 1) canvas.drawRect(30f, y, PAGE_W - 30f, y + rowH, alt)
            canvas.drawText(s.name.take(30), colX[0], y + 17f, row)
            canvas.drawText(s.marks.toString(), colX[1], y + 17f, row)
            canvas.drawText(s.total.toString(), colX[2], y + 17f, row)
            canvas.drawText("${s.percent}%", colX[3], y + 17f, row)
            canvas.drawText(s.grade, colX[4], y + 17f, row)
            y += rowH
        }

        canvas.drawRect(30f, startY, PAGE_W - 30f, y,
            Paint().apply { color = Color.rgb(200, 200, 200); style = Paint.Style.STROKE; strokeWidth = 1f })
    }

    private fun drawAttendance(canvas: Canvas, state: ReportCardUiState) {
        val y = 250f + 26f + state.subjects.size * 26f + 30f
        val rect = Rect(30, y.toInt(), PAGE_W - 30, (y + 80).toInt())
        canvas.drawRect(rect, Paint().apply { color = Color.WHITE })
        canvas.drawRect(rect, Paint().apply {
            color = Color.rgb(220, 220, 220); style = Paint.Style.STROKE; strokeWidth = 1.5f
        })

        canvas.drawText("Attendance Summary", 50f, y + 25f,
            Paint().apply { color = Color.rgb(26, 35, 126); textSize = 13f; isFakeBoldText = true; isAntiAlias = true })

        val lbl = Paint().apply { color = Color.rgb(120, 120, 120); textSize = 10f; isAntiAlias = true }
        val val_ = Paint().apply { color = Color.rgb(30, 30, 30); textSize = 14f; isFakeBoldText = true; isAntiAlias = true }

        canvas.drawText("PRESENT", 50f, y + 50f, lbl)
        canvas.drawText("${state.totalPresent}", 50f, y + 70f, val_)
        canvas.drawText("ABSENT", 180f, y + 50f, lbl)
        canvas.drawText("${state.totalAbsent}", 180f, y + 70f, val_)
        canvas.drawText("PERCENTAGE", 310f, y + 50f, lbl)
        canvas.drawText("${state.attendancePercent}%", 310f, y + 70f, val_)
    }

    private fun drawSummary(canvas: Canvas, state: ReportCardUiState) {
        val y = 250f + 26f + state.subjects.size * 26f + 130f
        canvas.drawRect(30f, y, PAGE_W - 30f, y + 60f,
            Paint().apply { color = Color.rgb(255, 193, 7) })

        canvas.drawText("OVERALL PERCENTAGE", 50f, y + 25f,
            Paint().apply { color = Color.rgb(60, 40, 0); textSize = 11f; isFakeBoldText = true; isAntiAlias = true })
        canvas.drawText("${state.overallPercent}%", 50f, y + 52f,
            Paint().apply { color = Color.rgb(30, 20, 0); textSize = 24f; isFakeBoldText = true; isAntiAlias = true })

        canvas.drawText("FINAL GRADE", PAGE_W - 180f, y + 25f,
            Paint().apply { color = Color.rgb(60, 40, 0); textSize = 11f; isFakeBoldText = true; isAntiAlias = true })
        canvas.drawText(state.overallGrade, PAGE_W - 180f, y + 54f,
            Paint().apply { color = Color.rgb(30, 20, 0); textSize = 26f; isFakeBoldText = true; isAntiAlias = true })
    }

    private fun drawFooter(canvas: Canvas, state: ReportCardUiState) {
        val y = PAGE_H - 60f
        canvas.drawLine(40f, y - 15f, PAGE_W - 40f, y - 15f,
            Paint().apply { color = Color.rgb(200, 200, 200); strokeWidth = 1f })

        val note = Paint().apply { color = Color.rgb(120, 120, 120); textSize = 9f; isAntiAlias = true }
        canvas.drawText(
            "This is a computer-generated document. For official use, please contact the school administration.",
            40f, y, note
        )
        canvas.drawText(
            "School Manager v1.0 • Generated on ${state.generatedOn}",
            40f, y + 14f, note
        )
    }
}
