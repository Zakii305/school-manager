package com.school.manager.ui.idcard

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

data class IdCardData(
    val studentName: String,
    val admNo: String,
    val className: String,
    val section: String,
    val fatherName: String,
    val contact: String,
    val bloodGroup: String,
    val address: String,
    val schoolName: String,
    val session: String
)

object IdCardPdfGenerator {

    // Card size: 320 x 500 pt (portrait, ID card ratio)
    private const val CARD_W = 320
    private const val CARD_H = 500

    fun generate(context: Context, data: IdCardData): File {
        val pdf = PdfDocument()
        val page = pdf.startPage(PdfDocument.PageInfo.Builder(CARD_W, CARD_H, 1).create())
        val c = page.canvas

        // ── Card background ──
        c.drawRect(0f, 0f, CARD_W.toFloat(), CARD_H.toFloat(),
            Paint().apply { color = Color.rgb(245, 245, 250) })

        // ── Violet header band ──
        c.drawRect(0f, 0f, CARD_W.toFloat(), 90f,
            Paint().apply { color = Color.rgb(74, 20, 140) })

        // School name
        c.drawText(data.schoolName.uppercase(), 16f, 32f,
            Paint().apply { color = Color.WHITE; textSize = 14f
                isFakeBoldText = true; isAntiAlias = true })
        c.drawText("STUDENT IDENTITY CARD", 16f, 50f,
            Paint().apply { color = Color.rgb(255, 193, 7); textSize = 9f
                isFakeBoldText = true; isAntiAlias = true })
        c.drawText("Session ${data.session}", 16f, 72f,
            Paint().apply { color = Color.argb(200, 255, 255, 255); textSize = 9f
                isAntiAlias = true })

        // ── Photo placeholder ──
        val photoRect = RectF(20f, 110f, 130f, 240f)
        c.drawRoundRect(photoRect, 8f, 8f,
            Paint().apply { color = Color.WHITE; style = Paint.Style.FILL })
        c.drawRoundRect(photoRect, 8f, 8f,
            Paint().apply { color = Color.rgb(200, 200, 200)
                style = Paint.Style.STROKE; strokeWidth = 1f })
        c.drawText("PHOTO", 55f, 180f,
            Paint().apply { color = Color.rgb(150, 150, 150); textSize = 11f
                isAntiAlias = true })

        // ── Student details (right of photo) ──
        val label = Paint().apply { color = Color.rgb(120, 120, 120)
            textSize = 8f; isAntiAlias = true }
        val value = Paint().apply { color = Color.rgb(20, 20, 20)
            textSize = 11f; isFakeBoldText = true; isAntiAlias = true }

        c.drawText("NAME", 145f, 125f, label)
        c.drawText(data.studentName.take(20), 145f, 140f, value)

        c.drawText("ADMISSION NO", 145f, 165f, label)
        c.drawText(data.admNo, 145f, 180f, value)

        c.drawText("CLASS", 145f, 205f, label)
        c.drawText("${data.className} - ${data.section}", 145f, 220f, value)

        c.drawText("BLOOD GROUP", 145f, 245f, label)
        c.drawText(data.bloodGroup.ifBlank { "—" }, 145f, 260f, value)

        // ── Divider ──
        c.drawLine(20f, 290f, CARD_W - 20f, 290f,
            Paint().apply { color = Color.rgb(220, 220, 220); strokeWidth = 1f })

        // ── Parent / Contact info ──
        c.drawText("FATHER NAME", 20f, 315f, label)
        c.drawText(data.fatherName.take(28), 20f, 332f, value)

        c.drawText("CONTACT", 20f, 355f, label)
        c.drawText(data.contact.ifBlank { "—" }, 20f, 372f, value)

        c.drawText("ADDRESS", 20f, 395f, label)
        c.drawText(data.address.take(40), 20f, 412f,
            Paint().apply { color = Color.rgb(60, 60, 60); textSize = 10f
                isAntiAlias = true })

        // ── QR code placeholder (bottom-right) ──
        val qrRect = RectF(230f, 400f, 300f, 470f)
        c.drawRect(qrRect, Paint().apply { color = Color.WHITE })
        c.drawRect(qrRect, Paint().apply { color = Color.rgb(74, 20, 140)
            style = Paint.Style.STROKE; strokeWidth = 1.5f })

        // Simple 7x7 pattern to represent a QR code
        val cellSize = 10f
        val gridX = 230f + 2f
        val gridY = 400f + 2f
        val pattern = arrayOf(
            intArrayOf(1,1,1,1,1,1,1),
            intArrayOf(1,0,1,0,1,0,1),
            intArrayOf(1,1,1,1,1,1,1),
            intArrayOf(1,0,1,1,0,1,1),
            intArrayOf(1,1,0,1,1,0,1),
            intArrayOf(1,0,1,0,1,1,1),
            intArrayOf(1,1,1,1,1,1,1)
        )
        for (row in 0..6) {
            for (col in 0..6) {
                if (pattern[row][col] == 1) {
                    c.drawRect(
                        gridX + col * cellSize,
                        gridY + row * cellSize,
                        gridX + (col + 1) * cellSize,
                        gridY + (row + 1) * cellSize,
                        Paint().apply { color = Color.rgb(20, 20, 40) }
                    )
                }
            }
        }

        // ── Footer ──
        c.drawRect(0f, CARD_H - 22f, CARD_W.toFloat(), CARD_H.toFloat(),
            Paint().apply { color = Color.rgb(74, 20, 140) })
        c.drawText("Valid for current academic session",
            16f, CARD_H - 8f,
            Paint().apply { color = Color.WHITE; textSize = 8f; isAntiAlias = true })

        pdf.finishPage(page)

        val dir = File(context.getExternalFilesDir(null), "idcards").apply { mkdirs() }
        val safeName = data.studentName.replace(" ", "_").ifBlank { "student" }
        val file = File(dir, "IDCard_${data.admNo}_$safeName.pdf")
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
        context.startActivity(Intent.createChooser(intent, "Share ID Card"))
    }
}
