package com.school.manager.ui.exams

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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class HallTicketData(
    val ticketNo: String,
    val studentName: String,
    val studentEmail: String,
    val className: String,
    val rollNo: String,
    val examName: String,
    val examDate: Long,
    val subjects: List<String>,
    val venue: String,
    val startTime: String,
    val endTime: String
)

object HallTicketPdfGenerator {

    private const val PAGE_W = 595
    private const val PAGE_H = 842

    fun generate(context: Context, data: HallTicketData): File {
        val pdf = PdfDocument()
        val page = pdf.startPage(PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, 1).create())
        val canvas = page.canvas

        canvas.drawRect(0f, 0f, PAGE_W.toFloat(), PAGE_H.toFloat(),
            Paint().apply { color = Color.rgb(245, 245, 245) })

        // Header band
        canvas.drawRect(0f, 0f, PAGE_W.toFloat(), 130f,
            Paint().apply { color = Color.rgb(74, 20, 140) })
        canvas.drawText("School Manager", 40f, 55f,
            Paint().apply { color = Color.WHITE; textSize = 30f
                isFakeBoldText = true; isAntiAlias = true })
        canvas.drawText("EXAMINATION HALL TICKET", 40f, 90f,
            Paint().apply { color = Color.rgb(255, 193, 7); textSize = 14f
                isFakeBoldText = true; isAntiAlias = true })
        canvas.drawText("Ticket #${data.ticketNo}", 40f, 118f,
            Paint().apply { color = Color.argb(220, 255, 255, 255); textSize = 11f
                isAntiAlias = true })

        // Student card
        canvas.drawRect(Rect(30, 160, PAGE_W - 30, 340),
            Paint().apply { color = Color.WHITE })
        canvas.drawRect(Rect(30, 160, PAGE_W - 30, 340),
            Paint().apply { color = Color.rgb(220, 220, 220)
                style = Paint.Style.STROKE; strokeWidth = 1.5f })

        val lbl = Paint().apply { color = Color.rgb(120, 120, 120); textSize = 10f; isAntiAlias = true }
        val val_ = Paint().apply { color = Color.rgb(20, 20, 20); textSize = 15f
            isFakeBoldText = true; isAntiAlias = true }

        canvas.drawText("STUDENT NAME", 50f, 190f, lbl)
        canvas.drawText(data.studentName, 50f, 212f, val_)
        canvas.drawText("ROLL NO", 50f, 245f, lbl)
        canvas.drawText(data.rollNo, 50f, 267f, val_)
        canvas.drawText("CLASS", 50f, 300f, lbl)
        canvas.drawText(data.className, 50f, 322f, val_)
        canvas.drawText("EMAIL", 330f, 190f, lbl)
        canvas.drawText(data.studentEmail.take(28), 330f, 212f,
            Paint().apply { color = Color.rgb(20, 20, 20); textSize = 12f; isAntiAlias = true })
        canvas.drawText("EXAM", 330f, 245f, lbl)
        canvas.drawText(data.examName, 330f, 267f, val_)
        canvas.drawText("DATE", 330f, 300f, lbl)
        canvas.drawText(
            SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date(data.examDate)),
            330f, 322f, val_)

        // Subjects table
        val startY = 380f
        val rowH = 26f
        canvas.drawText("SUBJECTS", 40f, startY - 10f,
            Paint().apply { color = Color.rgb(74, 20, 140); textSize = 12f
                isFakeBoldText = true; isAntiAlias = true })

        canvas.drawRect(30f, startY, PAGE_W - 30f, startY + rowH,
            Paint().apply { color = Color.rgb(74, 20, 140) })
        canvas.drawText("#", 40f, startY + 17f,
            Paint().apply { color = Color.WHITE; textSize = 11f
                isFakeBoldText = true; isAntiAlias = true })
        canvas.drawText("Subject", 80f, startY + 17f,
            Paint().apply { color = Color.WHITE; textSize = 11f
                isFakeBoldText = true; isAntiAlias = true })

        val rowP = Paint().apply { color = Color.rgb(30, 30, 30); textSize = 11f; isAntiAlias = true }
        val alt = Paint().apply { color = Color.rgb(240, 242, 250) }
        var y = startY + rowH
        data.subjects.forEachIndexed { i, s ->
            if (i % 2 == 1) canvas.drawRect(30f, y, PAGE_W - 30f, y + rowH, alt)
            canvas.drawText("${i + 1}", 40f, y + 17f, rowP)
            canvas.drawText(s, 80f, y + 17f, rowP)
            y += rowH
        }
        canvas.drawRect(30f, startY, PAGE_W - 30f, y,
            Paint().apply { color = Color.rgb(200, 200, 200)
                style = Paint.Style.STROKE; strokeWidth = 1f })

        // Instructions
        val insY = y + 40f
        canvas.drawText("INSTRUCTIONS", 40f, insY,
            Paint().apply { color = Color.rgb(74, 20, 140); textSize = 12f
                isFakeBoldText = true; isAntiAlias = true })
        val ins = Paint().apply { color = Color.rgb(60, 60, 60); textSize = 10f; isAntiAlias = true }
        listOf(
            "1. Report to the exam venue 30 minutes before start time.",
            "2. Bring this hall ticket and a valid student ID.",
            "3. Mobile phones and electronic devices are strictly prohibited.",
            "4. Venue: ${data.venue}  •  Time: ${data.startTime} – ${data.endTime}",
            "5. Malpractice will result in disqualification."
        ).forEachIndexed { i, line ->
            canvas.drawText(line, 40f, insY + 20f + i * 16f, ins)
        }

        // Footer
        val fy = PAGE_H - 60f
        canvas.drawLine(40f, fy - 15f, PAGE_W - 40f, fy - 15f,
            Paint().apply { color = Color.rgb(200, 200, 200); strokeWidth = 1f })
        canvas.drawText(
            "Keep this ticket safe. Present it at the exam hall entrance.",
            40f, fy,
            Paint().apply { color = Color.rgb(120, 120, 120); textSize = 9f; isAntiAlias = true })

        pdf.finishPage(page)

        val dir = File(context.getExternalFilesDir(null), "halltickets").apply { mkdirs() }
        val safeName = data.studentName.replace(" ", "_").ifBlank { "student" }
        val file = File(dir, "HallTicket_${data.ticketNo}_${safeName}.pdf")
        FileOutputStream(file).use { pdf.writeTo(it) }
        pdf.close()
        return file
    }

    fun share(context: Context, file: File) {
        val uri: Uri = FileProvider.getUriForFile(
            context, "${context.packageName}.fileprovider", file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Hall Ticket"))
    }
}
