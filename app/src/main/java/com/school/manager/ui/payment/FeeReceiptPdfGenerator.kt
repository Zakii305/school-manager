package com.school.manager.ui.payment

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

data class ReceiptData(
    val receiptNo: String,
    val studentName: String,
    val studentEmail: String,
    val className: String,
    val description: String,
    val amount: Double,
    val paidAt: Long,
    val transactionId: String
)

object FeeReceiptPdfGenerator {

    private const val PAGE_W = 595
    private const val PAGE_H = 842

    fun generate(context: Context, data: ReceiptData): File {
        val pdf = PdfDocument()
        val page = pdf.startPage(PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, 1).create())
        val canvas = page.canvas

        drawBackground(canvas)
        drawHeader(canvas, data)
        drawReceiptMeta(canvas, data)
        drawStudentBlock(canvas, data)
        drawAmountBlock(canvas, data)
        drawFooter(canvas, data)

        pdf.finishPage(page)

        val dir = File(context.getExternalFilesDir(null), "receipts").apply { mkdirs() }
        val safeName = data.studentName.replace(" ", "_").ifBlank { "student" }
        val file = File(dir, "FeeReceipt_${data.receiptNo}_${safeName}.pdf")
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
        context.startActivity(Intent.createChooser(intent, "Share Receipt"))
    }

    private fun drawBackground(canvas: Canvas) {
        canvas.drawRect(0f, 0f, PAGE_W.toFloat(), PAGE_H.toFloat(),
            Paint().apply { color = Color.rgb(245, 245, 245) })
    }

    private fun drawHeader(canvas: Canvas, data: ReceiptData) {
        canvas.drawRect(0f, 0f, PAGE_W.toFloat(), 120f,
            Paint().apply { color = Color.rgb(74, 20, 140) }) // violet
        canvas.drawText("School Manager", 40f, 50f,
            Paint().apply { color = Color.WHITE; textSize = 28f
                isFakeBoldText = true; isAntiAlias = true })
        canvas.drawText("Official Fee Receipt", 40f, 78f,
            Paint().apply { color = Color.argb(220, 255, 255, 255); textSize = 13f
                isAntiAlias = true })
        canvas.drawText("PAID", PAGE_W - 120f, 55f,
            Paint().apply { color = Color.rgb(255, 193, 7); textSize = 24f
                isFakeBoldText = true; isAntiAlias = true })
        canvas.drawText("RECEIPT #${data.receiptNo}", 40f, 100f,
            Paint().apply { color = Color.WHITE; textSize = 11f; isAntiAlias = true })
    }

    private fun drawReceiptMeta(canvas: Canvas, data: ReceiptData) {
        val rect = Rect(30, 150, PAGE_W - 30, 260)
        canvas.drawRect(rect, Paint().apply { color = Color.WHITE })
        canvas.drawRect(rect, Paint().apply {
            color = Color.rgb(220, 220, 220); style = Paint.Style.STROKE; strokeWidth = 1.5f
        })

        val lbl = Paint().apply { color = Color.rgb(120, 120, 120); textSize = 10f; isAntiAlias = true }
        val val_ = Paint().apply { color = Color.rgb(20, 20, 20); textSize = 14f
            isFakeBoldText = true; isAntiAlias = true }

        canvas.drawText("RECEIPT NO", 50f, 180f, lbl)
        canvas.drawText(data.receiptNo, 50f, 202f, val_)

        canvas.drawText("DATE", 330f, 180f, lbl)
        canvas.drawText(
            SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US).format(Date(data.paidAt)),
            330f, 202f,
            Paint().apply { color = Color.rgb(20, 20, 20); textSize = 12f
                isFakeBoldText = true; isAntiAlias = true }
        )

        canvas.drawText("TRANSACTION ID", 50f, 238f, lbl)
        canvas.drawText(data.transactionId, 50f, 254f,
            Paint().apply { color = Color.rgb(20, 20, 20); textSize = 11f; isAntiAlias = true })
    }

    private fun drawStudentBlock(canvas: Canvas, data: ReceiptData) {
        val rect = Rect(30, 285, PAGE_W - 30, 400)
        canvas.drawRect(rect, Paint().apply { color = Color.WHITE })
        canvas.drawRect(rect, Paint().apply {
            color = Color.rgb(220, 220, 220); style = Paint.Style.STROKE; strokeWidth = 1.5f
        })

        canvas.drawText("STUDENT DETAILS", 50f, 310f,
            Paint().apply { color = Color.rgb(74, 20, 140); textSize = 12f
                isFakeBoldText = true; isAntiAlias = true })

        val lbl = Paint().apply { color = Color.rgb(120, 120, 120); textSize = 10f; isAntiAlias = true }
        val val_ = Paint().apply { color = Color.rgb(20, 20, 20); textSize = 14f
            isFakeBoldText = true; isAntiAlias = true }

        canvas.drawText("NAME", 50f, 340f, lbl)
        canvas.drawText(data.studentName, 50f, 362f, val_)
        canvas.drawText("CLASS", 50f, 388f, lbl)
        canvas.drawText(data.className, 50f, 404f,
            Paint().apply { color = Color.rgb(20, 20, 20); textSize = 14f
                isFakeBoldText = true; isAntiAlias = true })
        canvas.drawText("EMAIL", 330f, 340f, lbl)
        canvas.drawText(data.studentEmail.take(30), 330f, 362f,
            Paint().apply { color = Color.rgb(20, 20, 20); textSize = 11f; isAntiAlias = true })
    }

    private fun drawAmountBlock(canvas: Canvas, data: ReceiptData) {
        val rect = Rect(30, 425, PAGE_W - 30, 545)
        canvas.drawRect(rect, Paint().apply { color = Color.WHITE })
        canvas.drawRect(rect, Paint().apply {
            color = Color.rgb(220, 220, 220); style = Paint.Style.STROKE; strokeWidth = 1.5f
        })

        canvas.drawText("DESCRIPTION", 50f, 450f,
            Paint().apply { color = Color.rgb(120, 120, 120); textSize = 10f; isAntiAlias = true })
        canvas.drawText(data.description, 50f, 475f,
            Paint().apply { color = Color.rgb(20, 20, 20); textSize = 14f; isAntiAlias = true })

        canvas.drawText("AMOUNT PAID", 50f, 515f,
            Paint().apply { color = Color.rgb(120, 120, 120); textSize = 10f; isAntiAlias = true })
        canvas.drawText("Rs ${data.amount.toInt()}", 50f, 538f,
            Paint().apply { color = Color.rgb(74, 20, 140); textSize = 26f
                isFakeBoldText = true; isAntiAlias = true })

        // Paid badge
        canvas.drawRect(PAGE_W - 175f, 500f, PAGE_W - 50f, 540f,
            Paint().apply { color = Color.rgb(76, 175, 80) })
        canvas.drawText("PAID", PAGE_W - 145f, 527f,
            Paint().apply { color = Color.WHITE; textSize = 20f
                isFakeBoldText = true; isAntiAlias = true })
    }

    private fun drawFooter(canvas: Canvas, data: ReceiptData) {
        val y = PAGE_H - 80f
        canvas.drawLine(40f, y - 20f, PAGE_W - 40f, y - 20f,
            Paint().apply { color = Color.rgb(200, 200, 200); strokeWidth = 1f })
        canvas.drawText(
            "This is a computer-generated receipt. For official enquiries, contact the school office.",
            40f, y,
            Paint().apply { color = Color.rgb(120, 120, 120); textSize = 9f; isAntiAlias = true }
        )
        canvas.drawText(
            "School Manager v1.0  •  ${SimpleDateFormat("dd MMM yyyy HH:mm", Locale.US).format(Date())}",
            40f, y + 16f,
            Paint().apply { color = Color.rgb(120, 120, 120); textSize = 9f; isAntiAlias = true }
        )
    }
}
